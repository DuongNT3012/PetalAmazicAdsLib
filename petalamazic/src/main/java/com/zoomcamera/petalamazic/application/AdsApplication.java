package com.zoomcamera.petalamazic.application;

import android.app.Application;
import android.util.Log;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.AGConnectOptionsBuilder;

import java.io.IOException;

public abstract class AdsApplication extends Application {
    public abstract String getClientId();
    public abstract String getClientSecret();
    public abstract String getApiKey();
    public abstract String getCPId();
    public abstract String getProductId();
    public abstract String getAppId();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            AGConnectOptionsBuilder builder = new AGConnectOptionsBuilder();
            builder.setInputStream(getAssets().open("agconnect-services.json"));
            builder.setClientId(getClientId());
            builder.setClientSecret(getClientSecret());
            builder.setApiKey(getApiKey());
            builder.setCPId(getCPId());
            builder.setProductId(getProductId());
            builder.setAppId(getAppId());
            AGConnectInstance.initialize(this, builder);
        } catch (IOException e) {
            Log.d("AdsApplication", "Exception: " + e.getMessage());
        }
    }
}
