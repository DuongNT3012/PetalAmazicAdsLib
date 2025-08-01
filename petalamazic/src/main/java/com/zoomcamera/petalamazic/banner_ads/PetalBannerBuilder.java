package com.zoomcamera.petalamazic.banner_ads;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.zoomcamera.petalamazic.R;
import com.zoomcamera.petalamazic.callback.BannerAdsCallback;
import com.huawei.hms.ads.BannerAdSize;

import java.util.ArrayList;
import java.util.List;

public class PetalBannerBuilder {
    private final FrameLayout frContainer;
    public View shimmerBanner;
    private List<String> listIdAdMain = new ArrayList<>();
    private BannerAdsCallback callBack = new BannerAdsCallback();
    private BannerAdSize bannerAdSize = BannerAdSize.BANNER_SIZE_360_57;
    private int timeRefresh = 60;

    public PetalBannerBuilder(Activity activity, FrameLayout frContainer) {
        this.frContainer = frContainer;
        shimmerBanner = LayoutInflater.from(activity).inflate(R.layout.layout_shimmer_banner, null);
        if (frContainer != null) {
            frContainer.addView(shimmerBanner);
        }
    }

    public BannerAdSize getBannerAdSize() {
        return bannerAdSize;
    }

    public void setBannerAdSize(BannerAdSize bannerAdSize) {
        this.bannerAdSize = bannerAdSize;
    }

    public int getTimeRefresh() {
        return timeRefresh;
    }

    public void setTimeRefresh(int timeRefresh) {
        this.timeRefresh = timeRefresh;
    }

    public FrameLayout getFrContainer() {
        return frContainer;
    }

    public List<String> getListIdAdMain() {
        return listIdAdMain;
    }

    public void setListIdAdMain(List<String> listIdAdMain) {
        this.listIdAdMain.clear();
        this.listIdAdMain.addAll(listIdAdMain);
    }

    public BannerAdsCallback getCallBack() {
        return callBack;
    }

    public void setCallBack(BannerAdsCallback callBack) {
        this.callBack = callBack;
    }
}
