package com.maxlife.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by atish.naik on 4/12/15.
 */
public class PrefStore {

    private Context activity;
    private String PREFS_NAME = "MaxLife";

    public PrefStore(Activity activity) {
        this.activity = activity;
    }

    public PrefStore(Context context) {
        this.activity = context;
    }

    private SharedPreferences getPref() {
        return activity.getSharedPreferences(PREFS_NAME, 0);
    }

    public String getLoginStatus() {
        SharedPreferences settings = getPref();
        String loginValid = settings.getString("auth_code", null);

        return loginValid;
    }

    public void setLoginStatus(String loginValid) {
        SharedPreferences settings = getPref();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("auth_code", loginValid);

        editor.commit();
        log("PrefStore" + "loginValid " + loginValid);
    }

    public String getString(String key) {
        SharedPreferences settings = getPref();
        String userName = settings.getString(key, "");

        return userName;
    }

    public void setString(String key, String value) {
        SharedPreferences mPrefs = getPref();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void saveString(String key, String value) {
        SharedPreferences settings = getPref();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public int getInt(String key,int agreeValue) {
        SharedPreferences settings = getPref();
        int userName = settings.getInt(key, agreeValue);
        return userName;
    }

    public void setInt(String key, int value) {
        SharedPreferences mPrefs = getPref();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key) {
        SharedPreferences settings = getPref();
        boolean userName = settings.getBoolean(key, false);

        return userName;
    }

    public void setBoolean(String key, Boolean value) {
        SharedPreferences mPrefs = getPref();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean contains(String key) {
        SharedPreferences mPrefs = getPref();
        boolean bContains = mPrefs.contains(key);
        return bContains;
    }

    public void clearAll() {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }

    public void log(String string) {
        Log.e("prefStore ",  string);
    }

}
