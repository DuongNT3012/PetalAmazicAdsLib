package com.amazic.petalamazic.native_ads;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amazic.petalamazic.petal.Petal;
import com.huawei.hms.ads.nativead.NativeAd;

public class PetalNativeManager implements LifecycleEventObserver {
    private static final String TAG = "NativeManager";
    private final PetalNativeBuilder builder;
    private final AppCompatActivity currentActivity;
    private final LifecycleOwner lifecycleOwner;
    private final String remoteKeyMain;
    private boolean isAlwaysReloadOnResume = false;
    private long intervalReloadNative = 0;
    private boolean isStop = false;
    private CountDownTimer countDownTimer;
    private NativeAd myNativeAdMain;
    private Handler handlerTimeoutCallNative = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    public PetalNativeManager(AppCompatActivity currentActivity, PetalNativeBuilder builder, LifecycleOwner lifecycleOwner, String remoteKeyMain) {
        this.currentActivity = currentActivity;
        this.builder = builder;
        this.remoteKeyMain = remoteKeyMain;
        this.lifecycleOwner = lifecycleOwner;
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
                    countDownTimer.start();
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
                if (myNativeAdMain != null) {
                    myNativeAdMain.destroy();
                }
                Log.d(TAG, "onStateChanged: ON_DESTROY");
                this.lifecycleOwner.getLifecycle().removeObserver(this);
                break;
        }
    }

    private void reloadAdNow() {
        if (myNativeAdMain != null) {
            myNativeAdMain.destroy();
        }
        myNativeAdMain = Petal.getInstance().loadNativeAds(currentActivity, builder.getListIdAdMain(), builder, builder.getMaxRequest(), remoteKeyMain);
    }

    public void setIntervalReloadNative(long intervalReloadNative) {
        if (intervalReloadNative > 0) {
            this.intervalReloadNative = intervalReloadNative;
            countDownTimer = new CountDownTimer(this.intervalReloadNative, 1000) {
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

    public void cancelAutoReloadNative() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void startReloadNative() {
        if (countDownTimer != null && this.lifecycleOwner.getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
            Log.d(TAG, "startReloadNative: ");
            countDownTimer.cancel();
            countDownTimer.start();
        }
    }

    public void setAlwaysReloadOnResume(boolean isAlwaysReloadOnResume) {
        this.isAlwaysReloadOnResume = isAlwaysReloadOnResume;
    }
}
