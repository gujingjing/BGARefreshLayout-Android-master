package cn.bingoogolapple.refreshlayout.demo.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildClickListener;
import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildLongClickListener;
import cn.bingoogolapple.androidcommon.adapter.BGAOnRVItemClickListener;
import cn.bingoogolapple.androidcommon.adapter.BGAOnRVItemLongClickListener;
import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.demo.R;
import cn.bingoogolapple.refreshlayout.demo.adapter.SwipeRecyclerViewAdapter;
import cn.bingoogolapple.refreshlayout.demo.model.BannerModel;
import cn.bingoogolapple.refreshlayout.demo.model.RefreshModel;
import cn.bingoogolapple.refreshlayout.demo.widget.Divider;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class SwipeRecyclerViewActivity extends BaseActivity implements BGAOnRVItemClickListener, BGAOnRVItemLongClickListener, BGAOnItemChildClickListener, BGAOnItemChildLongClickListener, BGARefreshLayout.BGARefreshLayoutDelegate {
    private BGARefreshLayout mRefreshLayout;
    private BGABanner mBanner;
    private RecyclerView mDataRv;
    private SwipeRecyclerViewAdapter mAdapter;
    private int mNewPageNumber = 0;
    private int mMorePageNumber = 0;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_recyclerview);
        mRefreshLayout = getViewById(R.id.refreshLayout);
        mBanner = getViewById(R.id.banner);
        mDataRv = getViewById(R.id.data);
    }

    @Override
    protected void setListener() {
        mRefreshLayout.setDelegate(this);

        mAdapter = new SwipeRecyclerViewAdapter(mDataRv);
        mAdapter.setOnRVItemClickListener(this);
        mAdapter.setOnRVItemLongClickListener(this);
        mAdapter.setOnItemChildClickListener(this);
        mAdapter.setOnItemChildLongClickListener(this);

        mDataRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    mAdapter.closeOpenedSwipeItemLayoutWithAnim();
                }
            }
        });

        findViewById(R.id.retweet).setOnClickListener(this);
        findViewById(R.id.comment).setOnClickListener(this);
        findViewById(R.id.praise).setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mRefreshLayout.setRefreshViewHolder(new BGANormalRefreshViewHolder(mApp, true));

        initBanner();

        mDataRv.addItemDecoration(new Divider(this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDataRv.setLayoutManager(linearLayoutManager);

        mDataRv.setAdapter(mAdapter);

        mEngine.loadInitDatas().enqueue(new Callback<List<RefreshModel>>() {
            @Override
            public void onResponse(Response<List<RefreshModel>> response, Retrofit retrofit) {
                mAdapter.setDatas(response.body());
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private void initBanner() {
        final List<View> views = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            views.add(View.inflate(this, R.layout.view_image, null));
        }
        mBanner.setViews(views);
        mEngine.getBannerModel().enqueue(new Callback<BannerModel>() {
            @Override
            public void onResponse(Response<BannerModel> response, Retrofit retrofit) {
                BannerModel bannerModel = response.body();
                for (int i = 0; i < views.size(); i++) {
                    Glide.with(SwipeRecyclerViewActivity.this).load(bannerModel.imgs.get(i)).placeholder(R.mipmap.holder).error(R.mipmap.holder).dontAnimate().thumbnail(0.1f).into((ImageView) views.get(i));
                }
                mBanner.setTips(bannerModel.tips);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    @Override
    public void onItemChildClick(ViewGroup viewGroup, View childView, int position) {
        if (childView.getId() == R.id.tv_item_swipe_delete) {
            mAdapter.removeItem(position);
        }
    }

    @Override
    public boolean onItemChildLongClick(ViewGroup viewGroup, View childView, int position) {
        if (childView.getId() == R.id.tv_item_swipe_delete) {
            showToast("长按了删除 " + mAdapter.getItem(position).title);
            return true;
        }
        return false;
    }

    @Override
    public void onRVItemClick(ViewGroup viewGroup, View itemView, int position) {
        showToast("点击了条目 " + mAdapter.getItem(position).title);
    }

    @Override
    public boolean onRVItemLongClick(ViewGroup viewGroup, View itemView, int position) {
        showToast("长按了条目 " + mAdapter.getItem(position).title);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.retweet) {
            showToast("点击了转发");
        } else if (v.getId() == R.id.comment) {
            showToast("点击了评论");
        } else if (v.getId() == R.id.praise) {
            showToast("点击了赞");
        }
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        mNewPageNumber++;
        if (mNewPageNumber > 4) {
            mRefreshLayout.endRefreshing();
            showToast("没有最新数据了");
            return;
        }
        mEngine.loadNewData(mNewPageNumber).enqueue(new Callback<List<RefreshModel>>() {
            @Override
            public void onResponse(Response<List<RefreshModel>> response, Retrofit retrofit) {
                mRefreshLayout.endRefreshing();
                mAdapter.addNewDatas(response.body());
                mDataRv.smoothScrollToPosition(0);
            }

            @Override
            public void onFailure(Throwable t) {
                mRefreshLayout.endRefreshing();
            }
        });
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        mMorePageNumber++;
        if (mMorePageNumber > 5) {
            mRefreshLayout.endLoadingMore();
            showToast("没有更多数据了");
            return false;
        }
        mEngine.loadMoreData(mMorePageNumber).enqueue(new Callback<List<RefreshModel>>() {
            @Override
            public void onResponse(Response<List<RefreshModel>> response, Retrofit retrofit) {
                mRefreshLayout.endLoadingMore();
                mAdapter.addMoreDatas(response.body());
            }

            @Override
            public void onFailure(Throwable t) {
                mRefreshLayout.endLoadingMore();
            }
        });
        return true;
    }

}