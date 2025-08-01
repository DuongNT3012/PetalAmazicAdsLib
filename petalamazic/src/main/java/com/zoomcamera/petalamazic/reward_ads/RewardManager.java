package com.zoomcamera.petalamazic.reward_ads;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.zoomcamera.petalamazic.callback.RewardAdsCallback;
import com.zoomcamera.petalamazic.petal.AdmobApi;
import com.zoomcamera.petalamazic.petal.Petal;
import com.huawei.hms.ads.reward.RewardAd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardManager {
    private static final String TAG = "RewardManager";
    private static final Map<String, RewardAd> listReward = new HashMap<>();

    public static void loadRewardAds(AppCompatActivity activity, List<String> listIdRewardAds, String adsKey, String remoteKey) {
        if (listReward.get(adsKey) == null) {
            listReward.put(adsKey, Petal.getInstance().loadRewardAd(activity, listIdRewardAds, new RewardAdsCallback(), remoteKey));
        } else {
            Log.d(TAG, "Reward already loaded. (Reward != null)");
        }
    }
    public static void loadRewardAds(AppCompatActivity activity, String adsKey, String remoteKey) {
        if (listReward.get(adsKey) == null) {
            listReward.put(adsKey, Petal.getInstance().loadRewardAd(activity, AdmobApi.getInstance().getListIDByName(adsKey), new RewardAdsCallback(), remoteKey));
        } else {
            Log.d(TAG, "Reward already loaded. (Reward != null)");
        }
    }

    public static void showRewardAds(AppCompatActivity activity, String adsKey, String remoteKey, RewardAdsCallback rewardAdsCallback, boolean isReloadRewardAfterShow) {
        Petal.getInstance().rewardAdShow(activity, listReward.get(adsKey), rewardAdsCallback, remoteKey);
        listReward.put(adsKey, null);
        if (isReloadRewardAfterShow) {
            loadRewardAds(activity, adsKey, remoteKey);
        }
    }
}
