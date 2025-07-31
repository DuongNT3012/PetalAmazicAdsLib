package com.amazic.petalamazic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.huawei.agconnect.remoteconfig.AGConnectConfig;
import com.huawei.agconnect.remoteconfig.ConfigValues;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Map;

public class RemoteConfigHelper {
    private static final String TAG = "RemoteConfigHelper";
    private static RemoteConfigHelper INSTANCE;
    private ArrayList<String> listRemoteStringName = new ArrayList<>();
    private ArrayList<String> listRemoteBooleanName = new ArrayList<>();
    private ArrayList<String> listRemoteLongName = new ArrayList<>();
    private ArrayList<String> listRemoteDoubleName = new ArrayList<>();

    private long intervalSeconds = 0;

    public static RemoteConfigHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RemoteConfigHelper();
        }
        return INSTANCE;
    }

    public interface IOnFetchDone {
        void onFetchDone();

        void onFetchFail();
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public void fetchData(Context context, IOnFetchDone iOnFetchDone) {
        AGConnectConfig.getInstance().fetch(getIntervalSeconds()).addOnSuccessListener(new OnSuccessListener<ConfigValues>() {
            @Override
            public void onSuccess(ConfigValues configValues) {
                // Apply the parameter values.
                AGConnectConfig.getInstance().apply(configValues);
                Map<String, Object> map = AGConnectConfig.getInstance().getMergedAll();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    String valueType = determineValueType(value.toString());
                    switch (valueType) {
                        case "String":
                            listRemoteStringName.add(key);
                            break;
                        case "Boolean":
                            listRemoteBooleanName.add(key);
                            break;
                        case "Long":
                            listRemoteLongName.add(key);
                            break;
                        case "Double":
                            listRemoteDoubleName.add(key);
                            break;
                        default:
                            break;
                    }
                    Log.d(TAG, "Key: " + key + ", Value: " + value + ", Type: " + valueType);
                }
                for (String key : listRemoteStringName) {
                    set_config_string(context, key, getRemoteConfigString(key));
                }
                for (String key : listRemoteBooleanName) {
                    set_config(context, key, getRemoteConfigBoolean(key));
                }
                for (String key : listRemoteLongName) {
                    set_config_long(context, key, getRemoteConfigLong(key));
                }
                for (String key : listRemoteDoubleName) {
                    set_config_float(context, key, (float) getRemoteConfigDouble(key));
                }
                Log.i(TAG, "onFetchDone.");
                iOnFetchDone.onFetchDone();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
                iOnFetchDone.onFetchFail();
            }
        });
    }

    private static String determineValueType(String value) {
        if (value == null || value.isEmpty()) {
            return "String";
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return "Boolean";
        }
        try {
            Long.parseLong(value);
            return "Long";
        } catch (NumberFormatException ignored) {
        }
        try {
            Double.parseDouble(value);
            return "Double";
        } catch (NumberFormatException ignored) {
        }
        return "String";
    }

    private boolean getRemoteConfigBoolean(String key) {
        return AGConnectConfig.getInstance().getValueAsBoolean(key);
    }

    private long getRemoteConfigLong(String key) {
        return AGConnectConfig.getInstance().getValueAsLong(key);
    }

    private String getRemoteConfigString(String key) {
        return AGConnectConfig.getInstance().getValueAsString(key);
    }

    private double getRemoteConfigDouble(String key) {
        return AGConnectConfig.getInstance().getValueAsDouble(key);
    }

    private byte[] getRemoteConfigByteArray(String key) {
        return AGConnectConfig.getInstance().getValueAsByteArray(key);
    }

    public boolean get_config(Context context, String name_config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        return pre.getBoolean(name_config, true);
    }

    public void set_config(Context context, String name_config, boolean config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean(name_config, config);
        editor.apply();
    }

    public void set_config_commit(Context context, String name_config, boolean config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean(name_config, config);
        editor.commit();
    }

    public String get_config_string(Context context, String name_config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        return pre.getString(name_config, "0_100");
    }

    public void set_config_string(Context context, String name_config, String config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString(name_config, config);
        editor.apply();
    }

    public void set_config_string_commit(Context context, String name_config, String config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString(name_config, config);
        editor.commit();
    }

    public void set_config_long(Context context, String name_config, Long config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putLong(name_config, config);
        editor.apply();
    }

    public void set_config_long_commit(Context context, String name_config, Long config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putLong(name_config, config);
        editor.commit();
    }

    public Long get_config_long(Context context, String name_config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        return pre.getLong(name_config, 0);
    }

    public void set_config_float(Context context, String name_config, float config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putFloat(name_config, config);
        editor.apply();
    }

    public void set_config_float_commit(Context context, String name_config, float config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putFloat(name_config, config);
        editor.commit();
    }

    public Float get_config_float(Context context, String name_config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        return pre.getFloat(name_config, 0f);
    }
}
