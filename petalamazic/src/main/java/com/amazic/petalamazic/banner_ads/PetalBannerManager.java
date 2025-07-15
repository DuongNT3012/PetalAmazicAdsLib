package com.amazic.petalamazic.banner_ads;

import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amazic.petalamazic.petal.Petal;
import com.huawei.hms.ads.banner.BannerView;

public class PetalBannerManager implements LifecycleEventObserver {
    private static final String TAG = "PetalBannerManager";
    private final AppCompatActivity currentActivity;
    private final PetalBannerBuilder builder;
    private final LifecycleOwner lifecycleOwner;
    private boolean isAlwaysReloadOnResume = false;
    private long intervalReloadBanner = 0;
    private boolean isStop = false;
    private CountDownTimer countDownTimer;
    private final String remoteKey;
    private BannerView bannerView;

    public PetalBannerManager(AppCompatActivity currentActivity, PetalBannerBuilder builder, LifecycleOwner lifecycleOwner, String remoteKey) {
        this.currentActivity = currentActivity;
        this.builder = builder;
        this.lifecycleOwner = lifecycleOwner;
        this.remoteKey = remoteKey;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                Log.d(TAG, "onStateChanged: ON_CREATE");
                reloadAdNow();
                break;
            case ON_RESUME:
                if (countDownTimer != null && isStop) {
                    startReloadBanner();
                }
                String valueLog = isStop + " && " + isAlwaysReloadOnResume;
                Log.d(TAG, "onStateChanged: resume\n" + valueLog);
                if (isStop && isAlwaysReloadOnResume) {
                    reloadAdNow();
                }
                isStop = false;
                break;
            case ON_PAUSE:
                Log.d(TAG, "onStateChanged: ON_PAUSE");
                isStop = true;
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                break;
            case ON_DESTROY:
                Log.d(TAG, "onStateChanged: ON_DESTROY");
                if (bannerView != null) {
                    bannerView.destroy();
                }
                if (builder.getFrContainer() != null) {
                    builder.getFrContainer().removeAllViews();
                }
                this.lifecycleOwner.getLifecycle().removeObserver(this);
                break;
        }
    }

    private void reloadAdNow() {
        if (!Petal.getInstance().checkCondition(currentActivity, remoteKey)) {
            if (builder.getFrContainer() != null) {
                builder.getFrContainer().removeAllViews();
            }
            return;
        }
        loadMainBanner();
    }

    private void loadMainBanner() {
        if (currentActivity != null) {
            if (bannerView != null) {
                bannerView.destroy();
            }
            bannerView = Petal.getInstance().loadBannerAds(currentActivity, builder.getListIdAdMain(), remoteKey, builder);
            if (bannerView != null) {
                builder.getFrContainer().removeAllViews();
                builder.getFrContainer().addView(bannerView);
            }
        }
    }

    public void setAlwaysReloadOnResume(boolean isAlwaysReloadOnResume) {
        this.isAlwaysReloadOnResume = isAlwaysReloadOnResume;
    }

    private void startReloadBanner() {
        if (countDownTimer != null && this.lifecycleOwner.getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
            Log.d(TAG, "startReloadBanner: ");
            countDownTimer.cancel();
            countDownTimer.start();
        }
    }

    public void setIntervalReloadBanner(long intervalReloadBanner) {
        if (intervalReloadBanner > 0) {
            this.intervalReloadBanner = intervalReloadBanner;
            countDownTimer = new CountDownTimer(this.intervalReloadBanner, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    reloadAdNow();
                }
            };
        }
    }

    public void cancelAutoReloadBanner() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void resumeAutoReloadBanner() {
        if (countDownTimer != null) {
            countDownTimer.start();
        }
    }
}
