package com.amazic.petalamazic.native_ads;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.amazic.petalamazic.callback.NativeAdsCallback;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.huawei.hms.ads.nativead.NativeView;

import java.util.ArrayList;
import java.util.List;

public class PetalNativeBuilder {
    private static final String TAG = "PetalNativeBuilder";
    private NativeAdsCallback callback = new NativeAdsCallback();
    private List<String> listIdAdMain = new ArrayList<>();
    private final FrameLayout flAd;
    private final int layoutNative;
    private final int layoutShimmerNative;
    private ShimmerFrameLayout shimmerFrameLayout;
    private NativeView nativeView;
    private int maxRequest = 1;

    public PetalNativeBuilder(Context context, FrameLayout flAd, int layoutNative, int layoutShimmerNative) {
        this.flAd = flAd;
        this.layoutNative = layoutNative;
        this.layoutShimmerNative = layoutShimmerNative;

        flAd.removeAllViews();
        nativeView = (NativeView) LayoutInflater.from(context).inflate(layoutNative, null);
        flAd.addView(nativeView);
        shimmerFrameLayout = (ShimmerFrameLayout) LayoutInflater.from(context).inflate(layoutShimmerNative, null);
        flAd.addView(shimmerFrameLayout);
    }

    public int getLayoutNative() {
        return layoutNative;
    }

    public FrameLayout getFlAd() {
        return flAd;
    }

    public int getMaxRequest() {
        return maxRequest;
    }

    public void setMaxRequest(int maxRequest) {
        this.maxRequest = maxRequest;
    }

    public ShimmerFrameLayout getShimmerFrameLayout() {
        return shimmerFrameLayout;
    }

    public void setShimmerFrameLayout(ShimmerFrameLayout shimmerFrameLayout) {
        this.shimmerFrameLayout = shimmerFrameLayout;
    }

    public NativeAdsCallback getCallback() {
        return callback;
    }

    public void setCallback(NativeAdsCallback callback) {
        this.callback = callback;
    }

    public List<String> getListIdAdMain() {
        return listIdAdMain;
    }

    public void setListIdAdMain(List<String> listIdAdMain) {
        this.listIdAdMain.clear();
        this.listIdAdMain.addAll(listIdAdMain);
    }
}
