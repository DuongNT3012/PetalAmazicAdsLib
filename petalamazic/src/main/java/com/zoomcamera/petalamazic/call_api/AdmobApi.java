package com.zoomcamera.petalamazic.call_api;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.hms.ads.splash.SplashView;
import com.zoomcamera.petalamazic.callback.ApiCallback;
import com.zoomcamera.petalamazic.callback.SplashAdsCallback;
import com.zoomcamera.petalamazic.petal.Petal;
import com.zoomcamera.petalamazic.utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdmobApi {
    private String TAG = "AdmobApi";
    private String remoteKey = "inter_splash";
    private ApiService apiService;
    private String linkServer = "http://language-master.top";
    private String packageName = "";
    public String appIDRelease = "ca-app-pub-4973559944609228~2346710863";
    private static volatile AdmobApi INSTANCE;
    private Context context;
    private String jsonIdAdsDefault = "[{\"id\": 1,\"package_name\": null,\"app name\": \"Api test\",\"app_id\": \"ca-app-pub-4973559944609228~2346710863\",\"name\": \"splash_ads\",\"ads_id\": \"testq6zq98hecj\"}, {\"id\": 1,\"package_name\": null,\"app name\": \"Api test\",\"app_id\": \"ca-app-pub-4973559944609228~2346710863\",\"name\": \"native_permission\",\"ads_id\": \"testy63txaom86\"}, {\"id\": 1,\"package_name\": null,\"app name\": \"Api test\",\"app_id\": \"ca-app-pub-4973559944609228~2346710863\",\"name\": \"native_language\",\"ads_id\": \"testy63txaom86\"}, {\"id\": 1,\"package_name\": null,\"app name\": \"Api test\",\"app_id\": \"ca-app-pub-4973559944609228~2346710863\",\"name\": \"native_intro\",\"ads_id\": \"testy63txaom86\"}, {\"id\": 1,\"package_name\": null,\"app name\": \"Api test\",\"app_id\": \"ca-app-pub-4973559944609228~2346710863\",\"name\": \"reward\",\"ads_id\": \"testx9dtjwj8hp\"}, {\"id\": 1,\"package_name\": null,\"app name\": \"Api test\",\"app_id\": \"ca-app-pub-4973559944609228~2346710863\",\"name\": \"inter_all\",\"ads_id\": \"testb4znbuh3n2\"}, {\"id\": 1,\"package_name\": null,\"app name\": \"Api test\",\"app_id\": \"ca-app-pub-4973559944609228~2346710863\",\"name\": \"native_all\",\"ads_id\": \"testy63txaom86\"}, {\"id\": 1,\"package_name\": null,\"app name\": \"Api test\",\"app_id\": \"ca-app-pub-4973559944609228~2346710863\",\"name\": \"banner_all\",\"ads_id\": \"testw6vs28auh3\"}]";
    private boolean isSetId = false;
    private int timeOutCallApi = 12000;

    public String getRemoteKey() {
        return remoteKey;
    }

    public void setRemoteKey(String remoteKey) {
        this.remoteKey = remoteKey;
    }

    public int getListAdsSize() {
        if (listAds != null) {
            return listAds.size();
        } else {
            return 0;
        }
    }

    public String getJsonIdAdsDefault() {
        return jsonIdAdsDefault;
    }

    public void setJsonIdAdsDefault(String jsonIdAdsDefault) {
        this.jsonIdAdsDefault = jsonIdAdsDefault;
    }

    public int getTimeOutCallApi() {
        return timeOutCallApi;
    }

    public void setTimeOutCallApi(int timeOutCallApi) {
        this.timeOutCallApi = timeOutCallApi;
    }

    LinkedHashMap<String, List<String>> listAds = new LinkedHashMap<>();

    public List<String> getListIDByName(String nameAds) {
        List<String> list = new ArrayList<>();
        if (listAds.get(nameAds.trim()) != null)
            list.addAll(Objects.requireNonNull(listAds.get(nameAds)));
        return list;
    }

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public static synchronized AdmobApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdmobApi();
        }
        return INSTANCE;
    }

    public void init(Context context, String linkServerRelease, String AppID, ApiCallback callBack) {
        this.context = context;
        listAds.clear();
        isSetId = false;
        this.packageName = context.getPackageName();
        if (linkServerRelease != null && AppID != null) {
            if (!linkServerRelease.trim().equals("")
                    && (linkServerRelease.contains("http://")
                    || linkServerRelease.contains("https://"))) {
                this.linkServer = linkServerRelease.trim();
                this.appIDRelease = AppID.trim();
            }
        }

        String baseURL = linkServer + "/api/";
        apiService = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);

        Log.i(TAG, "link Server:" + baseURL);

        if (NetworkUtil.isNetworkActive(context)) {
            fetchData(callBack);
            //after 12s, if cannot call api -> set list id default
            new Handler().postDelayed(() -> {
                if (!isSetId) { //if not set id from api -> set list id default
                    convertJsonIdAdsDefaultToList(jsonIdAdsDefault);
                    isSetId = true;
                    Log.d(TAG, "isSetId = true1");
                    callBack.onReady();
                } else {
                    Log.d(TAG, "xxxxxx1");
                }
            }, timeOutCallApi);
        } else {
            callBack.onReady();
        }
    }

    public void convertJsonIdAdsDefaultToList(String jsonIdAds) {
        listAds.clear();
        try {
            JSONArray jsonArray = new JSONArray(jsonIdAds);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String name = jsonObject.getString("name").trim();
                String ads_id = jsonObject.getString("ads_id");

                // check if 'name' exists
                List<String> listIDAds = listAds.get(name);
                if (listIDAds == null) {
                    listIDAds = new ArrayList<>();
                    listAds.put(name, listIDAds);
                }

                // add ads_id
                listIDAds.add(ads_id);
            }
            Log.d(TAG, "convertJsonIdAdsDefaultToList: " + listAds.size());

            for (Map.Entry<String, List<String>> entry : listAds.entrySet()) {
                Log.d(TAG, "Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }
        } catch (Exception e) {
            Log.d(TAG, "convertJsonIdAdsDefaultToList: Exception: " + e.getMessage());
        }
    }

    private void fetchData(ApiCallback callBack) {
        Log.e(TAG, "fetchData: ");
        try {
            String appID_package = appIDRelease + "+" + packageName;
            Log.i(TAG, "link Server query :" + linkServer + "/api/getidv2/" + appID_package);
            apiService.callAds(appID_package).enqueue(new Callback<List<AdsModel>>() {
                @Override
                public void onResponse(@NonNull Call<List<AdsModel>> call, @NonNull Response<List<AdsModel>> response) {
                    Log.d(TAG, "onResponse: isSetId: " + isSetId);
                    if (!isSetId) {
                        if (response.body() == null || response.body().isEmpty()) {
                            callBack.onReady();
                            return;
                        }
                        Log.d(TAG, "onResponse: " + listAds.size());
                        for (AdsModel ads : response.body()) {
                            List<String> listIDAds = null;
                            if (listAds.containsKey(ads.getName())) {
                                listIDAds = listAds.get(ads.getName());
                            }
                            if (listIDAds == null) {
                                listIDAds = new ArrayList<>();
                            }
                            listIDAds.add(ads.getAds_id());
                            listAds.put(ads.getName().trim(), listIDAds);
                            Log.d(TAG, ads.getName().trim() + "_" + ads.getAds_id());
                        }
                        isSetId = true;
                        Log.d(TAG, "isSetId = true2, listAds size = " + listAds.size());
                        callBack.onReady();
                    } else {
                        Log.d(TAG, "xxxxxx2");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<AdsModel>> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t);
                    Log.d(TAG, "onFailure: isSetId: " + isSetId);
                    if (!isSetId) {
                        convertJsonIdAdsDefaultToList(jsonIdAdsDefault);
                        isSetId = true;
                        Log.d(TAG, "isSetId = true3");
                        callBack.onReady();
                    } else {
                        Log.d(TAG, "xxxxxx3");
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "fetchData: Exception: isSetId: " + isSetId);
            if (!isSetId) {
                convertJsonIdAdsDefaultToList(jsonIdAdsDefault);
                isSetId = true;
                Log.d(TAG, "isSetId = true4");
                callBack.onReady();
            } else {
                Log.d(TAG, "xxxxxx4");
            }
        }
    }

    public void loadInterAdSplashFloor(AppCompatActivity activity, SplashView splashView, String adsKey, SplashAdsCallback splashAdsCallback) {
        Petal.getInstance().loadSplashAds(activity, splashView, AdmobApi.getInstance().getListIDByName(adsKey), splashAdsCallback, remoteKey);
    }
}
