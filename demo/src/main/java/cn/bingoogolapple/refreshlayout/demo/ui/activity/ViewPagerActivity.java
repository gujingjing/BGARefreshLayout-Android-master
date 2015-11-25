package cn.bingoogolapple.refreshlayout.demo.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.bgaindicator.BGAFixedIndicator;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.demo.R;
import cn.bingoogolapple.refreshlayout.demo.model.BannerModel;
import cn.bingoogolapple.refreshlayout.demo.ui.fragment.StickyNavListViewFragment;
import cn.bingoogolapple.refreshlayout.demo.ui.fragment.StickyNavRecyclerViewFragment;
import cn.bingoogolapple.refreshlayout.demo.ui.fragment.StickyNavScrollViewFragment;
import cn.bingoogolapple.refreshlayout.demo.ui.fragment.StickyNavWebViewFragment;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class ViewPagerActivity extends BaseActivity implements BGARefreshLayout.BGARefreshLayoutDelegate {
    private BGARefreshLayout mRefreshLayout;
    private BGABanner mBanner;
    private BGAFixedIndicator mIndicator;
    private ViewPager mContentVp;

    private Fragment[] mFragments;
    private String[] mTitles;
    private StickyNavRecyclerViewFragment mRecyclerViewFragment;
    private StickyNavListViewFragment mListViewFragment;
    private StickyNavScrollViewFragment mScrollViewFragment;
    private StickyNavWebViewFragment mWebViewFragment;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_viewpager);
        mRefreshLayout = getViewById(R.id.refreshLayout);
        mBanner = getViewById(R.id.banner);
        mIndicator = getViewById(R.id.indicator);
        mContentVp = getViewById(R.id.vp_viewpager_content);
    }

    @Override
    protected void setListener() {
        mRefreshLayout.setDelegate(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mRefreshLayout.setRefreshViewHolder(new BGANormalRefreshViewHolder(mApp, true));

        initBanner();

        mFragments = new Fragment[4];
        mFragments[0] = mRecyclerViewFragment = new StickyNavRecyclerViewFragment();
        mFragments[1] = mListViewFragment = new StickyNavListViewFragment();
        mFragments[2] = mScrollViewFragment = new StickyNavScrollViewFragment();
        mFragments[3] = mWebViewFragment = new StickyNavWebViewFragment();

        mTitles = new String[4];
        mTitles[0] = "RecyclerView";
        mTitles[1] = "ListView";
        mTitles[2] = "ScrollView";
        mTitles[3] = "WebView";
        mContentVp.setAdapter(new ContentViewPagerAdapter(getSupportFragmentManager()));
        mIndicator.initData(0, mContentVp);
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
                    Glide.with(ViewPagerActivity.this).load(bannerModel.imgs.get(i)).placeholder(R.mipmap.holder).error(R.mipmap.holder).dontAnimate().thumbnail(0.1f).into((ImageView) views.get(i));
                }
                mBanner.setTips(bannerModel.tips);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        switch (mContentVp.getCurrentItem()) {
            case 0:
                mRecyclerViewFragment.onBGARefreshLayoutBeginRefreshing(refreshLayout);
                break;
            case 1:
                mListViewFragment.onBGARefreshLayoutBeginRefreshing(refreshLayout);
                break;
            case 2:
                mScrollViewFragment.onBGARefreshLayoutBeginRefreshing(refreshLayout);
                break;
            case 3:
                mWebViewFragment.onBGARefreshLayoutBeginRefreshing(refreshLayout);
                break;
        }
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        switch (mContentVp.getCurrentItem()) {
            case 0:
                return mRecyclerViewFragment.onBGARefreshLayoutBeginLoadingMore(refreshLayout);
            case 1:
                return mListViewFragment.onBGARefreshLayoutBeginLoadingMore(refreshLayout);
            case 2:
                return mScrollViewFragment.onBGARefreshLayoutBeginLoadingMore(refreshLayout);
            case 3:
                return mWebViewFragment.onBGARefreshLayoutBeginLoadingMore(refreshLayout);
            default:
                return false;
        }
    }

    public void endRefreshing() {
        mRefreshLayout.endRefreshing();
    }

    public void endLoadingMore() {
        mRefreshLayout.endLoadingMore();
    }

    class ContentViewPagerAdapter extends FragmentPagerAdapter {

        public ContentViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

}