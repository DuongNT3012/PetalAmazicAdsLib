package com.zoomcamera.x30zoom.hdcamera;

import android.app.Application;
import android.util.Log;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.AGConnectOptionsBuilder;

import java.io.IOException;

public class App extends Application {

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

    public String getClientId() {
        return "1721850433321836800";
    }

    public String getClientSecret() {
        return "BD4F646EA4204B393D79AD1567F3EC154F170B6628AE8644174EA8B87FFCFE46";
    }

    public String getApiKey() {
        return "DQEDAG3hhlrlLcwZNdTvbxv1nkwVszPgXTqNcOtGsw5z+oc6CslyMZj4dpd7hfAFc54FPhLcL1m8O6AuCQ/4sJ74XiINnRYYkd1G3A==";
    }

    public String getCPId() {
        return "30084000022393259";
    }

    public String getProductId() {
        return "461323198430081741";
    }

    public String getAppId() {
        return "114621061";
    }
}
