package com.amazic.petalamazic.inter_ads;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amazic.petalamazic.callback.InterAdsCallback;
import com.amazic.petalamazic.petal.AdmobApi;
import com.amazic.petalamazic.petal.Petal;
import com.huawei.hms.ads.InterstitialAd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterManager {
    private static final String TAG = "InterManager";
    private static final Map<String, InterstitialAd> listInter = new HashMap<>();

    public static void loadInterAds(AppCompatActivity activity, List<String> listIdRewardAds, String adsKey, String remoteKey) {
        if (listInter.get(adsKey) == null) {
            listInter.put(adsKey, Petal.getInstance().loadInterAds(activity, listIdRewardAds, new InterAdsCallback(), remoteKey));
        } else {
            Log.d(TAG, "Inter already loaded. (Inter != null). " + adsKey + "_" + remoteKey);
        }
    }

    public static void loadInterAds(AppCompatActivity activity, String adsKey, String remoteKey) {
        if (listInter.get(adsKey) == null) {
            listInter.put(adsKey, Petal.getInstance().loadInterAds(activity, AdmobApi.getInstance().getListIDByName(adsKey), new InterAdsCallback(), remoteKey));
        } else {
            Log.d(TAG, "Inter already loaded. (Inter != null). " + adsKey + "_" + remoteKey);
        }
    }

    public static void loadAndShowInterAds(AppCompatActivity activity, List<String> listIdRewardAds, String remoteKey) {
        Petal.getInstance().loadAndShowInterAds(activity, listIdRewardAds, new InterAdsCallback(), remoteKey);
    }

    public static void loadAndShowInterAds(AppCompatActivity activity, String adsKey, String remoteKey) {
        Petal.getInstance().loadAndShowInterAds(activity, AdmobApi.getInstance().getListIDByName(adsKey), new InterAdsCallback(), remoteKey);
    }

    public static void showInterAds(AppCompatActivity activity, String adsKey, String remoteKey, InterAdsCallback rewardAdsCallback, boolean isReloadInterAfterShow) {
        Petal.getInstance().showInterAds(activity, listInter.get(adsKey), rewardAdsCallback, remoteKey);
        listInter.put(adsKey, null);
        if (isReloadInterAfterShow) {
            loadInterAds(activity, adsKey, remoteKey);
        }
    }
}
