package com.zoomcamera.petalamazic.petal;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.AutoPlayNetType;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.ads.VideoConfiguration;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.ads.nativead.DislikeAdListener;
import com.huawei.hms.ads.nativead.MediaView;
import com.huawei.hms.ads.nativead.NativeAd;
import com.huawei.hms.ads.nativead.NativeAdLoader;
import com.huawei.hms.ads.nativead.NativeView;
import com.huawei.hms.ads.reward.Reward;
import com.huawei.hms.ads.reward.RewardAd;
import com.huawei.hms.ads.reward.RewardAdLoadListener;
import com.huawei.hms.ads.reward.RewardAdStatusListener;
import com.huawei.hms.ads.splash.SplashAdDisplayListener;
import com.huawei.hms.ads.splash.SplashView;
import com.zoomcamera.petalamazic.R;
import com.zoomcamera.petalamazic.banner_ads.PetalBannerBuilder;
import com.zoomcamera.petalamazic.callback.InterAdsCallback;
import com.zoomcamera.petalamazic.callback.NativeAdsCallback;
import com.zoomcamera.petalamazic.callback.RewardAdsCallback;
import com.zoomcamera.petalamazic.callback.SplashAdsCallback;
import com.zoomcamera.petalamazic.dialog.LoadingAdsDialog;
import com.zoomcamera.petalamazic.native_ads.PetalNativeBuilder;
import com.zoomcamera.petalamazic.utils.NetworkUtil;
import com.zoomcamera.petalamazic.utils.RemoteConfigHelper;

import java.util.ArrayList;
import java.util.List;

public class Petal {
    private String TAG = "Petal";
    private static Petal INSTANCE;
    private Handler handlerTimeoutSplash = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private static final int AD_TIMEOUT = 10000;
    private boolean isShowAllAds = true;
    private LoadingAdsDialog loadingAdsDialog;
    public ArrayList<Integer> listAnimationDialogRaw = new ArrayList<>();
    private boolean isCustomAnimationDialog = false;
    private boolean isLoadInterSplashIdTimeout = false;

    public static Petal getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Petal();
        }
        return INSTANCE;
    }

    //SPLASH ADS
    private boolean isLoadingSplashAds = false;

    public void loadSplashAds(AppCompatActivity activity, SplashView splashView, List<String> listIdSplashAds, SplashAdsCallback splashAdsCallback, String remoteKey) {
        if (!isLoadingSplashAds) {
            isLoadingSplashAds = true;
            ArrayList<String> listIdInterTemp = new ArrayList<>(listIdSplashAds);

            //Set timeout ads splash x(s) if cannot load
            runnable = () -> {
                if (splashAdsCallback != null) {
                    Log.d(TAG, "SPLASH ADS: loadSplashAds: timeout");
                    isLoadInterSplashIdTimeout = true;
                    splashAdsCallback.onNextAction();
                }
                if (handlerTimeoutSplash != null) {
                    handlerTimeoutSplash = null;
                }
            };
            if (handlerTimeoutSplash != null) {
                handlerTimeoutSplash.postDelayed(runnable, AD_TIMEOUT);
            }

            //Check condition
            if (!NetworkUtil.isNetworkActive(activity) || listIdInterTemp.isEmpty() || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(activity, remoteKey)) {
                Log.d(TAG, "SPLASH ADS: Check condition. RemoteKey:" + remoteKey + ". Network:" + NetworkUtil.isNetworkActive(activity) + "_IdEmpty:" + listIdInterTemp.isEmpty() + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(activity, remoteKey));
                if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
                    loadingAdsDialog.dismiss();
                }
                if (handlerTimeoutSplash != null && runnable != null) {
                    handlerTimeoutSplash.removeCallbacks(runnable);
                    handlerTimeoutSplash.removeCallbacksAndMessages(null);
                    handlerTimeoutSplash = null;
                }
                splashAdsCallback.onNextAction();
                return;
            }

            loadingAdsDialog = new LoadingAdsDialog(activity);
            if (!loadingAdsDialog.isShowing()) {
                loadingAdsDialog.show();
            }
            AdParam.Builder builder = new AdParam.Builder();
            AdParam adParam = builder.build();
            SplashView.SplashAdLoadListener splashAdLoadListener = new SplashView.SplashAdLoadListener() {
                @Override
                public void onAdLoaded() {
                    // Called when an ad is loaded successfully.
                    Log.i(TAG, "SPLASH ADS: onAdLoaded: " + remoteKey);
                    splashAdsCallback.onAdLoaded();
                    isLoadingSplashAds = false;
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Called when an ad fails to be loaded. The app home screen is then displayed.
                    Log.e(TAG, "SPLASH ADS: onAdFailedToLoad: code " + errorCode + " " + remoteKey);
                    splashAdsCallback.onAdFailedToLoad(errorCode);
                    isLoadingSplashAds = false;
                    if (!listIdInterTemp.isEmpty()) {
                        listIdInterTemp.remove(0);
                    }
                    if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
                        loadingAdsDialog.dismiss();
                    }
                    //loadSplashAds(activity, splashView, listIdInterTemp, splashAdsCallback, remoteKey);
                }

                @Override
                public void onAdDismissed() {
                    // Called when an ad has been displayed. The app home screen is then displayed.
                    Log.d(TAG, "SPLASH ADS: onAdDismissed: " + remoteKey);
                    splashAdsCallback.onAdDismissed();
                    splashAdsCallback.onNextAction();
                }
            };
            String slotId;
            // Lock the screen orientation on the device. Your app will automatically adapt to the screen orientation.
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            // Set the default slogan and the splash ad unit ID based on the screen orientation on the device.
            //splashView.setSloganResId(R.drawable.default_slogan);
            slotId = listIdInterTemp.get(0);
            // Obtain SplashView.
            // Set the audio focus type for a video splash ad.
            splashView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);
            // Load the ad.
            splashView.load(slotId, orientation, adParam, splashAdLoadListener);

            showSplashAds(splashView, splashAdsCallback, remoteKey);
        }
    }

    public void showSplashAds(SplashView splashView, SplashAdsCallback splashAdsCallback, String remoteKey) {
        if (!isLoadInterSplashIdTimeout) {
            SplashAdDisplayListener adDisplayListener = new SplashAdDisplayListener() {
                @Override
                public void onAdShowed() {
                    // Called when an ad is displayed.
                    Log.d(TAG, "SPLASH ADS: onAdShowed: " + remoteKey);
                    if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
                        loadingAdsDialog.dismiss();
                    }
                    if (handlerTimeoutSplash != null && runnable != null) {
                        handlerTimeoutSplash.removeCallbacks(runnable);
                        handlerTimeoutSplash.removeCallbacksAndMessages(null);
                        handlerTimeoutSplash = null;
                    }
                    splashAdsCallback.onAdShowed();
                }

                @Override
                public void onAdClick() {
                    // Called when an ad is clicked.
                    Log.d(TAG, "SPLASH ADS: onAdClick: " + remoteKey);
                    splashAdsCallback.onAdClick();
                }
            };
            splashView.setAdDisplayListener(adDisplayListener);
        }
    }
    //END SPLASH ADS

    //BANNER ADS
    public BannerView loadBannerAds(Context context, List<String> listIdBanner, String remoteKey, PetalBannerBuilder builder) {
        ArrayList<String> listIdBannerTemp = new ArrayList<>(listIdBanner);
        if (!NetworkUtil.isNetworkActive(context) || listIdBannerTemp.isEmpty() || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(context, remoteKey)) {
            Log.d(TAG, "BANNER ADS: Check condition. RemoteKey:" + remoteKey + ". Network:" + NetworkUtil.isNetworkActive(context) + "_IdEmpty:" + listIdBannerTemp.isEmpty() + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(context, remoteKey));
            builder.getCallBack().onNextAction();
            return null;
        }
        BannerView bannerView = new BannerView(context);
        bannerView.setAdId(listIdBannerTemp.get(0)); //"testw6vs28auh3"
        bannerView.setBannerAdSize(builder.getBannerAdSize());
        bannerView.setBannerRefresh(builder.getTimeRefresh());
        AdParam adParam = new AdParam.Builder().build();
        bannerView.loadAd(adParam);
        AdListener adListener = new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.i(TAG, "BANNER ADS: onAdLoaded: " + remoteKey);
                builder.getCallBack().onAdLoaded();
            }

            @Override
            public void onAdFailed(int i) {
                super.onAdFailed(i);
                Log.e(TAG, "BANNER ADS: onAdFailed: " + i + " " + remoteKey);
                builder.getCallBack().onAdFailed(i);
                if (!listIdBannerTemp.isEmpty()) {
                    listIdBannerTemp.remove(0);
                }
                loadBannerAds(context, listIdBannerTemp, remoteKey, builder);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d(TAG, "BANNER ADS: onAdImpression: " + remoteKey);
                builder.getCallBack().onAdImpression();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG, "BANNER ADS: onAdClicked: " + remoteKey);
                builder.getCallBack().onAdClicked();
            }

            @Override
            public void onAdLeave() {
                super.onAdLeave();
                Log.d(TAG, "BANNER ADS: onAdLeave: " + remoteKey);
                builder.getCallBack().onAdLeave();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG, "BANNER ADS: onAdOpened: " + remoteKey);
                builder.getCallBack().onAdOpened();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "BANNER ADS: onAdClosed: " + remoteKey);
                builder.getCallBack().onAdClosed();
            }
        };
        bannerView.setAdListener(adListener);
        return bannerView;
    }
    //END BANNER ADS

    //NATIVE ADS
    private NativeAd myNativeAds;

    public NativeAd loadNativeAds(Context context, List<String> listIdNative, PetalNativeBuilder nativeBuilder, int maxRequest, String remoteKey) {
        ArrayList<String> listIdNativeTemp = new ArrayList<>(listIdNative);
        //Check condition
        if (!NetworkUtil.isNetworkActive(context) || listIdNativeTemp.isEmpty() || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(context, remoteKey)) {
            Log.d(TAG, "NATIVE: Check condition. RemoteKey:" + remoteKey + "_Network:" + NetworkUtil.isNetworkActive(context) + "_IdEmpty:" + listIdNativeTemp.isEmpty() + "_UMP:" + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(context, remoteKey));
            nativeBuilder.getCallback().onAdFailed(-1);
            nativeBuilder.getFlAd().removeAllViews();
            return null;
        }
        NativeAdLoader.Builder builder = new NativeAdLoader.Builder(context, listIdNativeTemp.get(0));
        NativeAdLoader nativeAdLoader = builder.setNativeAdLoadedListener(new NativeAd.NativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                // Called each time an ad is successfully loaded.
                Log.i(TAG, "NATIVE: onNativeAdLoaded: " + remoteKey);
                nativeBuilder.getCallback().onNativeAdLoaded(nativeAd);

                VideoConfiguration videoConfiguration = new VideoConfiguration.Builder()
                        .setStartMuted(false)
                        .setAutoPlayNetwork(AutoPlayNetType.BOTH_WIFI_AND_DATA)
                        .build();
                nativeAd.setVideoConfiguration(videoConfiguration);

                // Obtain NativeView.
                final NativeView nativeView = (NativeView) LayoutInflater.from(context).inflate(nativeBuilder.getLayoutNative(), null);
                initNativeAdView(nativeAd, nativeView);
                // Add NativeView to the UI.
                nativeBuilder.getFlAd().removeAllViews();
                nativeBuilder.getFlAd().addView(nativeView);
                nativeAd.setDislikeAdListener(new DislikeAdListener() {
                    @Override
                    public void onAdDisliked() {
                        // Called when a user hides an ad, to remove the ad layout from the UI.
                        nativeBuilder.getFlAd().removeView(nativeView);
                    }
                });
                myNativeAds = nativeAd;
            }
        }).setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Called when all ads are successfully returned.
                Log.i(TAG, "NATIVE: onAdLoaded: " + remoteKey);
                nativeBuilder.getCallback().onAdLoaded();

            }

            @Override
            public void onAdFailed(int errorCode) {
                // Called when ads fail to be loaded.
                Log.e(TAG, "NATIVE: onAdFailed: " + errorCode + " " + remoteKey);
                nativeBuilder.getCallback().onAdFailed(errorCode);
                if (!listIdNativeTemp.isEmpty()) {
                    listIdNativeTemp.remove(0);
                }
                loadNativeAds(context, listIdNativeTemp, nativeBuilder, maxRequest, remoteKey);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG, "NATIVE: onAdClicked: " + remoteKey);
                nativeBuilder.getCallback().onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d(TAG, "NATIVE: onAdImpression: " + remoteKey);
                nativeBuilder.getCallback().onAdImpression();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG, "NATIVE: onAdOpened: " + remoteKey);
                nativeBuilder.getCallback().onAdOpened();
            }

            @Override
            public void onAdLeave() {
                super.onAdLeave();
                Log.d(TAG, "NATIVE: onAdLeave: " + remoteKey);
                nativeBuilder.getCallback().onAdLeave();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "NATIVE: onAdClosed: " + remoteKey);
                nativeBuilder.getCallback().onAdClosed();
            }
        }).build();
        /*AdParam.Builder builder = new AdParam.Builder();
        // (Optional) Set the parameters for a real-time bidding ad unit.
        BiddingParam biddingParam = new BiddingParam();
        String slotId = "testy63txaom86";
        builder.addBiddingParamMap(slotId, biddingParam);
        builder.setTMax(500);
        builder.setCur("Currency code list");*/
        nativeAdLoader.loadAds(new AdParam.Builder().build(), maxRequest);
        return myNativeAds;
    }

    public void loadNativeAds(Context context, List<String> listIdNative, NativeAdsCallback nativeAdsCallback, String remoteKey) {
        ArrayList<String> listIdNativeTemp = new ArrayList<>(listIdNative);
        //Check condition
        if (!NetworkUtil.isNetworkActive(context) || listIdNativeTemp.isEmpty() || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(context, remoteKey)) {
            Log.d(TAG, "NATIVE: Check condition. RemoteKey:" + remoteKey + "_Network:" + NetworkUtil.isNetworkActive(context) + "_IdEmpty:" + listIdNativeTemp.isEmpty() + "_UMP:" + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(context, remoteKey));
            nativeAdsCallback.onAdFailed(-1);
            return;
        }
        NativeAdLoader.Builder builder = new NativeAdLoader.Builder(context, listIdNativeTemp.get(0));
        NativeAdLoader nativeAdLoader = builder.setNativeAdLoadedListener(new NativeAd.NativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                // Called each time an ad is successfully loaded.
                Log.i(TAG, "NATIVE: onNativeAdLoaded: " + remoteKey);
                nativeAdsCallback.onNativeAdLoaded(nativeAd);

                VideoConfiguration videoConfiguration = new VideoConfiguration.Builder()
                        .setStartMuted(false)
                        .setAutoPlayNetwork(AutoPlayNetType.BOTH_WIFI_AND_DATA)
                        .build();
                nativeAd.setVideoConfiguration(videoConfiguration);
            }
        }).setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Called when all ads are successfully returned.
                Log.i(TAG, "NATIVE: onAdLoaded: " + remoteKey);
                nativeAdsCallback.onAdLoaded();

            }

            @Override
            public void onAdFailed(int errorCode) {
                // Called when ads fail to be loaded.
                Log.e(TAG, "NATIVE: onAdFailed: " + errorCode + " " + remoteKey);
                nativeAdsCallback.onAdFailed(errorCode);
                if (!listIdNativeTemp.isEmpty()) {
                    listIdNativeTemp.remove(0);
                }
                loadNativeAds(context, listIdNativeTemp, nativeAdsCallback, remoteKey);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG, "NATIVE: onAdClicked: " + remoteKey);
                nativeAdsCallback.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d(TAG, "NATIVE: onAdImpression: " + remoteKey);
                nativeAdsCallback.onAdImpression();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG, "NATIVE: onAdOpened: " + remoteKey);
                nativeAdsCallback.onAdOpened();
            }

            @Override
            public void onAdLeave() {
                super.onAdLeave();
                Log.d(TAG, "NATIVE: onAdLeave: " + remoteKey);
                nativeAdsCallback.onAdLeave();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "NATIVE: onAdClosed: " + remoteKey);
                nativeAdsCallback.onAdClosed();
            }
        }).build();
        /*AdParam.Builder builder = new AdParam.Builder();
        // (Optional) Set the parameters for a real-time bidding ad unit.
        BiddingParam biddingParam = new BiddingParam();
        String slotId = "testy63txaom86";
        builder.addBiddingParamMap(slotId, biddingParam);
        builder.setTMax(500);
        builder.setCur("Currency code list");*/
        nativeAdLoader.loadAds(new AdParam.Builder().build(), 1);
    }

    public void initNativeAdView(NativeAd nativeAd, NativeView nativeView) {
        // Register and populate the title view.
        nativeView.setTitleView(nativeView.findViewById(R.id.ad_title));
        ((TextView) nativeView.getTitleView()).setText(nativeAd.getTitle());
        // Register and populate the multimedia view.
        nativeView.setMediaView((MediaView) nativeView.findViewById(R.id.ad_media));
        nativeView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        // Register and populate other asset views.
        nativeView.setAdSourceView(nativeView.findViewById(R.id.ad_source));
        nativeView.setCallToActionView(nativeView.findViewById(R.id.ad_call_to_action));
        if (null != nativeAd.getAdSource()) {
            ((TextView) nativeView.getAdSourceView()).setText(nativeAd.getAdSource());
        }
        nativeView.getAdSourceView()
                .setVisibility(null != nativeAd.getAdSource() ? View.VISIBLE : View.INVISIBLE);
        if (null != nativeAd.getCallToAction()) {
            ((Button) nativeView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        nativeView.getCallToActionView()
                .setVisibility(null != nativeAd.getCallToAction() ? View.VISIBLE : View.INVISIBLE);

        // Register the native ad object.
        nativeView.setNativeAd(nativeAd);
        Log.d(TAG, "NATIVE: initNativeAdView.");
    }
    //END NATIVE ADS

    //REWARD ADS
    public RewardAd loadRewardAd(AppCompatActivity activity, List<String> listIdReward, RewardAdsCallback callback, String remoteKey) {
        ArrayList<String> listIdRewardTemp = new ArrayList<>(listIdReward);
        //Check condition
        if (!NetworkUtil.isNetworkActive(activity) || listIdRewardTemp.isEmpty() || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(activity, remoteKey)) {
            Log.d(TAG, "REWARD: Check condition. RemoteKey:" + remoteKey + "_Network:" + NetworkUtil.isNetworkActive(activity) + "_IdEmpty:" + listIdRewardTemp.isEmpty() + "_UMP:" + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(activity, remoteKey));
            callback.onRewardAdFailedToLoad(-1);
            return null;
        }

        RewardAd rewardAd = new RewardAd(activity, listIdRewardTemp.get(0));

        RewardAdLoadListener listener = new RewardAdLoadListener() {
            @Override
            public void onRewardedLoaded() {
                // Rewarded ad loaded successfully.
                Log.i(TAG, "REWARD: onRewardedLoaded: " + remoteKey);
                callback.onRewardedLoaded();
            }

            @Override
            public void onRewardAdFailedToLoad(int errorCode) {
                // Failed to load the rewarded ad.
                Log.e(TAG, "REWARD: onRewardAdFailedToLoad: " + errorCode + " " + remoteKey);
                callback.onRewardAdFailedToLoad(errorCode);
                if (!listIdRewardTemp.isEmpty()) {
                    listIdRewardTemp.remove(0);
                }
                loadRewardAd(activity, listIdRewardTemp, callback, remoteKey);
            }
        };
        AdParam.Builder builder = new AdParam.Builder();
        // (Optional) Set the parameters for a real-time bidding ad unit.
        /*BiddingParam biddingParam = new BiddingParam();
        String slotId = "testx9dtjwj8hp";
        builder.addBiddingParamMap(slotId, biddingParam);
        builder.setTMax(500);
        builder.setCur("Currency code list");*/
        rewardAd.loadAd(builder.build(), listener);
        return rewardAd;
    }

    public void rewardAdShow(AppCompatActivity activity, RewardAd rewardAd, RewardAdsCallback callback, String remoteKey) {
        //Check condition
        if (!NetworkUtil.isNetworkActive(activity) || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(activity, remoteKey)) {
            Log.d(TAG, "REWARD: Check condition. RemoteKey:" + remoteKey + "_Network:" + NetworkUtil.isNetworkActive(activity) + "_UMP:" + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(activity, remoteKey));
            callback.onRewardAdFailedToLoad(-1);
            callback.onNextAction();
            return;
        }
        if (rewardAd == null || !rewardAd.isLoaded()) {
            Log.d(TAG, "REWARD: The rewarded ad wasn't ready yet.");
            callback.onRewardAdFailedToShow(-1);
            callback.onNextAction();
            return;
        }
        loadingAdsDialog = new LoadingAdsDialog(activity);
        if (!loadingAdsDialog.isShowing() && !activity.isDestroyed()) {
            loadingAdsDialog.show();
        }
        // Construct a VideoConfiguration object.
        VideoConfiguration.Builder builder = new VideoConfiguration.Builder();
        // Set the type of the network allowed for automatic video playback. The corresponding constants are AutoPlayNetType#WIFI_ONLY (allowed only on Wi-Fi) and BOTH_WIFI_AND_DATA (allowed on both Wi-Fi and mobile data networks).
        builder.setAutoPlayNetwork(AutoPlayNetType.BOTH_WIFI_AND_DATA);
        // true: play video ads in mute mode; false: play video ads in unmute mode
        builder.setStartMuted(true);
        // Apply VideoConfiguration to video ads.
        rewardAd.setVideoConfiguration(builder.build());

        rewardAd.show(activity, new RewardAdStatusListener() {
            @Override
            public void onRewardAdOpened() {
                // Rewarded ad opened.
                Log.i(TAG, "REWARD: onRewardAdOpened: " + remoteKey);
                callback.onRewardAdOpened();
            }

            @Override
            public void onRewardAdFailedToShow(int errorCode) {
                // Failed to display the rewarded ad.
                Log.e(TAG, "REWARD: onRewardAdFailedToShow: " + errorCode + " " + remoteKey);
                callback.onRewardAdFailedToShow(errorCode);
                callback.onNextAction();
                if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
                    loadingAdsDialog.dismiss();
                }
            }

            @Override
            public void onRewardAdClosed() {
                // Rewarded ad closed.
                Log.d(TAG, "REWARD: onRewardAdClosed: " + remoteKey);
                callback.onRewardAdClosed();
                callback.onNextAction();
                if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
                    loadingAdsDialog.dismiss();
                }
            }

            @Override
            public void onRewarded(Reward reward) {
                // Provide a reward when reward conditions are met.
                Log.d(TAG, "REWARD: onRewarded: " + remoteKey);
                callback.onRewarded(reward);
            }
        });
    }
    //END REWARD ADS

    //INTER ADS
    public InterstitialAd loadInterAds(AppCompatActivity activity, List<String> listIdInter, InterAdsCallback callback, String remoteKey) {
        ArrayList<String> listIdInterTemp = new ArrayList<>(listIdInter);
        //Check condition
        if (!NetworkUtil.isNetworkActive(activity) || listIdInterTemp.isEmpty() || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(activity, remoteKey)) {
            Log.d(TAG, "INTER: Check condition. RemoteKey:" + remoteKey + "_Network:" + NetworkUtil.isNetworkActive(activity) + "_IdEmpty:" + listIdInterTemp.isEmpty() + "_UMP:" + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(activity, remoteKey));
            callback.onAdFailed(-1);
            callback.onNextAction();
            return null;
        }
        InterstitialAd interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdId(listIdInterTemp.get(0));
        // Load an interstitial ad.
        AdParam.Builder builder = new AdParam.Builder();
        AdParam adParam = builder.build();
        // (Optional) Set the parameters for a real-time bidding ad unit.
        /*BiddingParam biddingParam = new BiddingParam();
        String slotId = "testb4znbuh3n2";
        builder.addBiddingParamMap(slotId, biddingParam);
        builder.setTMax(500);
        builder.setCur("Currency code list");*/
        AdListener adListener = new AdListener() {
            @Override
            public void onAdLoaded() {
                // Called when an ad is loaded successfully.
                Log.i(TAG, "INTER: onAdLoaded: " + remoteKey);
                callback.onAdLoaded();
            }

            @Override
            public void onAdFailed(int errorCode) {
                // Called when an ad fails to be loaded.
                Log.e(TAG, "INTER: onAdFailed: " + errorCode + " " + remoteKey);
                callback.onAdFailed(errorCode);
                if (!listIdInterTemp.isEmpty()) {
                    listIdInterTemp.remove(0);
                }
                loadAndShowInterAds(activity, listIdInterTemp, callback, remoteKey);
            }

            @Override
            public void onAdClosed() {
                // Called when an ad is closed.
                Log.d(TAG, "INTER: onAdClosed: " + remoteKey);
                callback.onAdClosed();
                callback.onNextAction();
            }

            @Override
            public void onAdClicked() {
                // Called when an ad is clicked.
                Log.d(TAG, "INTER: onAdClicked: " + remoteKey);
                callback.onAdClicked();
            }

            @Override
            public void onAdLeave() {
                // Called when an ad leaves an app.
                Log.d(TAG, "INTER: onAdLeave: " + remoteKey);
                callback.onAdLeave();
            }

            @Override
            public void onAdOpened() {
                // Called when an ad is opened.
                Log.d(TAG, "INTER: onAdOpened: " + remoteKey);
                callback.onAdOpened();
                if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
                    loadingAdsDialog.dismiss();
                }
            }
        };
        interstitialAd.setAdListener(adListener);
        interstitialAd.loadAd(adParam);
        return interstitialAd;
    }

    public InterstitialAd loadAndShowInterAds(AppCompatActivity activity, List<String> listIdInter, InterAdsCallback callback, String remoteKey) {
        ArrayList<String> listIdInterTemp = new ArrayList<>(listIdInter);
        //Check condition
        if (!NetworkUtil.isNetworkActive(activity) || listIdInterTemp.isEmpty() || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(activity, remoteKey)) {
            Log.d(TAG, "INTER: Check condition. RemoteKey:" + remoteKey + "_Network:" + NetworkUtil.isNetworkActive(activity) + "_IdEmpty:" + listIdInterTemp.isEmpty() + "_UMP:" + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(activity, remoteKey));
            if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
                loadingAdsDialog.dismiss();
            }
            callback.onAdFailed(-1);
            callback.onNextAction();
            return null;
        }
        loadingAdsDialog = new LoadingAdsDialog(activity);
        if (!loadingAdsDialog.isShowing() && !activity.isDestroyed()) {
            loadingAdsDialog.show();
        }
        InterstitialAd interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdId(listIdInterTemp.get(0));
        // Load an interstitial ad.
        AdParam.Builder builder = new AdParam.Builder();
        AdParam adParam = builder.build();
        // (Optional) Set the parameters for a real-time bidding ad unit.
        /*BiddingParam biddingParam = new BiddingParam();
        String slotId = "testb4znbuh3n2";
        builder.addBiddingParamMap(slotId, biddingParam);
        builder.setTMax(500);
        builder.setCur("Currency code list");*/
        AdListener adListener = new AdListener() {
            @Override
            public void onAdLoaded() {
                // Called when an ad is loaded successfully.
                Log.i(TAG, "INTER: onAdLoaded: " + remoteKey);
                callback.onAdLoaded();
                showInterAdsLoadAndShow(activity, interstitialAd, callback, remoteKey);
            }

            @Override
            public void onAdFailed(int errorCode) {
                // Called when an ad fails to be loaded.
                Log.e(TAG, "INTER: onAdFailed: " + errorCode + " " + remoteKey);
                callback.onAdFailed(errorCode);
                if (!listIdInterTemp.isEmpty()) {
                    listIdInterTemp.remove(0);
                }
                loadAndShowInterAds(activity, listIdInterTemp, callback, remoteKey);
            }

            @Override
            public void onAdClosed() {
                // Called when an ad is closed.
                Log.d(TAG, "INTER: onAdClosed: " + remoteKey);
                callback.onAdClosed();
                callback.onNextAction();
                if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
                    loadingAdsDialog.dismiss();
                }
            }

            @Override
            public void onAdClicked() {
                // Called when an ad is clicked.
                Log.d(TAG, "INTER: onAdClicked: " + remoteKey);
                callback.onAdClicked();
            }

            @Override
            public void onAdLeave() {
                // Called when an ad leaves an app.
                Log.d(TAG, "INTER: onAdLeave: " + remoteKey);
                callback.onAdLeave();
            }

            @Override
            public void onAdOpened() {
                // Called when an ad is opened.
                Log.d(TAG, "INTER: onAdOpened: " + remoteKey);
                callback.onAdOpened();
            }
        };
        interstitialAd.setAdListener(adListener);
        interstitialAd.loadAd(adParam);
        return interstitialAd;
    }

    public void showInterAds(AppCompatActivity activity, InterstitialAd interstitialAd, InterAdsCallback callback, String remoteKey) {
        //Check condition
        if (!NetworkUtil.isNetworkActive(activity) || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(activity, remoteKey)) {
            Log.d(TAG, "INTER: Check condition. RemoteKey:" + remoteKey + "_Network:" + NetworkUtil.isNetworkActive(activity) + "_UMP:" + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(activity, remoteKey));
            callback.onAdFailed(-1);
            callback.onNextAction();
            return;
        }
        if (interstitialAd == null || !interstitialAd.isLoaded()) {
            Log.d(TAG, "INTER: The rewarded ad wasn't ready yet.");
            callback.onAdFailed(-1);
            callback.onNextAction();
            return;
        }
        loadingAdsDialog = new LoadingAdsDialog(activity);
        if (!loadingAdsDialog.isShowing() && !activity.isDestroyed()) {
            loadingAdsDialog.show();
        }
        interstitialAd.show(activity);
    }

    public void showInterAdsLoadAndShow(AppCompatActivity activity, InterstitialAd interstitialAd, InterAdsCallback callback, String remoteKey) {
        //Check condition
        if (!NetworkUtil.isNetworkActive(activity) || !isShowAllAds || !RemoteConfigHelper.getInstance().get_config(activity, remoteKey)) {
            Log.d(TAG, "INTER: Check condition. RemoteKey:" + remoteKey + "_Network:" + NetworkUtil.isNetworkActive(activity) + "_UMP:" + "_ShowAllAds:" + isShowAllAds + "_RemoteConfig:" + RemoteConfigHelper.getInstance().get_config(activity, remoteKey));
            callback.onAdFailed(-1);
            callback.onNextAction();
            return;
        }
        if (interstitialAd == null || !interstitialAd.isLoaded()) {
            Log.d(TAG, "INTER: The rewarded ad wasn't ready yet.");
            callback.onAdFailed(-1);
            callback.onNextAction();
            return;
        }
        if (loadingAdsDialog != null && loadingAdsDialog.isShowing()) {
            loadingAdsDialog.dismiss();
        }
        interstitialAd.show(activity);
    }

    //END INTER ADS
    private int getScreenOrientation(Context context) {
        Configuration config = context.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    public boolean checkCondition(Context context, String remoteKey) {
        Log.d(TAG, "checkCondition: Network_" + NetworkUtil.isNetworkActive(context) + "_showAllAds_" + isShowAllAds + "_RemoteConfig_" + RemoteConfigHelper.getInstance().get_config(context, remoteKey));
        return NetworkUtil.isNetworkActive(context) && isShowAllAds && RemoteConfigHelper.getInstance().get_config(context, remoteKey);
    }

    public void setShowAllAds(boolean isShowAllAds) {
        this.isShowAllAds = isShowAllAds;
    }

    public boolean getShowAllAds() {
        return isShowAllAds;
    }

    public void setCustomAnimationDialog(ArrayList<Integer> listAnimationDialogRaw) {
        this.isCustomAnimationDialog = true;
        this.listAnimationDialogRaw.clear();
        this.listAnimationDialogRaw.addAll(listAnimationDialogRaw);
    }

    public boolean isCustomAnimationDialog() {
        return isCustomAnimationDialog;
    }

    public void setCustomAnimationDialog(boolean customAnimationDialog) {
        isCustomAnimationDialog = customAnimationDialog;
    }
}
