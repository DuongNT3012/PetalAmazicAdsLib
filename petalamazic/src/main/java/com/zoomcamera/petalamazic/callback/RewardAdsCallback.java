package com.zoomcamera.petalamazic.callback;

import com.huawei.hms.ads.reward.Reward;

public class RewardAdsCallback {
    public void onRewardedLoaded() {}
    public void onRewardAdFailedToLoad(int errorCode){}
    public void onRewardAdOpened() {}
    public void onRewardAdFailedToShow(int errorCode) {}
    public void onRewardAdClosed() {}
    public void onRewarded(Reward reward) {}
    public void onNextAction() {}
}
