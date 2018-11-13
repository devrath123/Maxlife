package com.toxsl.volley.toolbox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.toxsl.volley.AppExpiredError;
import com.toxsl.volley.AppInMaintenance;
import com.toxsl.volley.AuthFailureError;
import com.toxsl.volley.Cache;
import com.toxsl.volley.Network;
import com.toxsl.volley.Request;
import com.toxsl.volley.RequestQueue;
import com.toxsl.volley.Response;
import com.toxsl.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpResponseException;

/**
 * Created by ankush.walia on 14-8-16.
 */
public class SyncManager implements SyncEventListner {
    private static final String PREFS_NAME = "SyncManager";
    private static SyncManager instance = null;
    private Context mContext;
    private String sBaseUrl;
    private String appName = "SyncManager";
    private RequestQueue mRequestQueue;
    private boolean isDebug;


    private SyncManager() {
    }


    public static synchronized SyncManager getInstance(Context mAct, boolean isDebug) {
        if (instance == null)
            instance = new SyncManager();
        instance.setAct(mAct, isDebug);
        return instance;
    }

    public void setBaseUrl(String url, String appName) {
        this.sBaseUrl = url;
        if (appName != null)
            this.appName = appName + "/language/" + "en" + "/timezone/" + TimeZone.getDefault().getID();
        this.appName = this.appName + " /BuildConfig. " + (isDebug ? "Debug" : "Release") + " /DeviceName. " + capitalize(Build.MANUFACTURER) + " /Model. " + capitalize(Build.MODEL) + " /Android. " + Build.VERSION.RELEASE + " /Version. " + getVersionName();
    }

    private void setAct(Context mAct, boolean isDebug) {
        this.isDebug = isDebug;
        this.mContext = mAct;
    }

    private String getVersionName() {
        try {
            return mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

            return null;
        }



    }
    class MyHanlder implements ResponseHandlerInterface {

        private SyncEventListner myHandler;

        public MyHanlder(SyncEventListner syncEventListner) {
            this.myHandler = syncEventListner;
        }

        @Override
        public void sendResponseMessage(HttpResponse response) throws IOException {

        }

        @Override
        public void sendStartMessage() {

        }

        @Override
        public void sendFinishMessage() {

        }

        @Override
        public void sendProgressMessage(long bytesWritten, long bytesTotal) {
            myHandler.onSyncProgress(bytesWritten, bytesTotal);
        }

        @Override
        public void sendCancelMessage() {

        }

        @Override
        public void sendSuccessMessage(int statusCode, Header[] headers, byte[] responseBody) {

        }

        @Override
        public void sendFailureMessage(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        }

        @Override
        public void sendRetryMessage(int retryNo) {

        }


        @Override
        public URI getRequestURI() {
            return null;
        }

        @Override
        public void setRequestURI(URI requestURI) {

        }

        @Override
        public Header[] getRequestHeaders() {
            return new Header[0];
        }

        @Override
        public void setRequestHeaders(Header[] requestHeaders) {

        }

        @Override
        public boolean getUseSynchronousMode() {
            return false;
        }

        @Override
        public void setUseSynchronousMode(boolean useSynchronousMode) {

        }

        @Override
        public boolean getUsePoolThread() {
            return false;
        }

        @Override
        public void setUsePoolThread(boolean usePoolThread) {

        }

        @Override
        public void onPreProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {

        }

        @Override
        public void onPostProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {

        }

        @Override
        public Object getTag() {
            return null;
        }

        @Override
        public void setTag(Object TAG) {

        }

    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private String addAuthCodeHeader(String url) {
        String auth_code = getLoginStatus();
        log("auth_code =" + auth_code);
        if (auth_code != null) {
            if (!url.contains("?"))
                url = url + "?auth_code=" + auth_code;
            else
                url = url + "&auth_code=" + auth_code;
        }
        return url;
    }

    private String getUrl(String url) {
        if (url.contains("http://") || url.contains("https://")) {

        } else if (url.startsWith("/")) {
            url = sBaseUrl + url.substring(1);
        } else {
            url = sBaseUrl + url;
        }
        url = addAuthCodeHeader(url);
        return url;
    }

    public String getLoginStatus() {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,
                0);
        return settings.getString("auth_code", null);
    }

    public void setLoginStatus(String loginValid) {
        if (loginValid != null) {
            if (loginValid.length() != 32)
                throw new RuntimeException("Auth Code is not valid");
        }
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME,
                0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("auth_code", loginValid);
        // Commit the edits!
        editor.apply();
    }

    private String getUserAgent() {
        return appName;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    private void log(String string) {
        Log.e(PREFS_NAME, "" + string);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            Cache cache = new DiskBasedCache(mContext.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            mRequestQueue = new RequestQueue(cache, network);
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public boolean sendToServer(String url, RequestParams params, SyncEventListner syncEventListner) {
      // String completeUrl = getUrl(url);
        String completeUrl = sBaseUrl + url;
        if (isNetworkAvailable()) {
            log("sendToServer = " + completeUrl + " params = " + params);
            sendMultiPartRequest(syncEventListner, params, completeUrl);
            return true;
        } else
            Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_SHORT).show();
        return false;
    }



    public void setBitmapImageFromServer(String url, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, int placeholder, ImageView image) {
        image.setImageResource(placeholder);
        ImageRequest request = new ImageRequest(
                url, new onVolleyImageResponse(image) {
            @Override
            public void onResponse(Bitmap response) {
                image.setImageBitmap(response);
            }
        }, maxWidth, maxHeight, scaleType, Bitmap.Config.RGB_565, new onVolleyImageError(image, placeholder) {

            @Override
            public void onErrorResponse(VolleyError error, Request mRequest) {
                image.setImageResource(errorImage);
                log(error.toString());
            }
        });
        getRequestQueue().add(request);
    }

    public void setBitmapImageFromServer(String url, ImageView image, int placeholder, int errorImage) {
        image.setImageResource(placeholder);
        ImageRequest request = new ImageRequest(
                url, new onVolleyImageResponse(image) {

            @Override
            public void onResponse(Bitmap response) {
                image.setImageBitmap(response);
            }

        }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, new onVolleyImageError(image, errorImage) {

            @Override
            public void onErrorResponse(VolleyError error, Request mRequest) {
                image.setImageResource(errorImage);
                log(error.toString());
            }
        });
        getRequestQueue().add(request);
    }

    private void sendMultiPartRequest(SyncEventListner syncEventListner, RequestParams params, String completeUrl) {
        if (syncEventListner == null) {
            syncEventListner = this;
        }
        syncEventListner.onSyncStart();
        MultipartRequest multipartRequest = new MultipartRequest(completeUrl,new MyHanlder(syncEventListner), params, new onVolleyResponse(syncEventListner) {
            @Override
            public void onResponse(String response) {
                syncEventListner.onSyncFinish();
                if (response != null && !response.equals("")) {
                    try {
                        if (!validateResponse(response, syncEventListner)) return;
                        JSONObject jsonObject = new JSONObject(response);
                        log(jsonObject.toString());
                        if (!isValidateDateCheck(jsonObject, syncEventListner)) return;
                        syncEventListner.onSyncSuccess(jsonObject.optString("controller"), jsonObject.optString("action"), jsonObject.optString("status").equalsIgnoreCase("OK"), jsonObject);
                    } catch (JSONException e) {
                        log("Error parsing Response >>>>>> " + e + " \n" + response);
                        e.printStackTrace();
                    }
                } else {
                    try {
                        syncEventListner.onSyncSuccess("", "", true, new JSONObject().put("success", "Response is empty"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }, new onVolleyError(syncEventListner) {
            @Override
            public void onErrorResponse(VolleyError error, Request mRequest) {
                syncEventListner.onSyncFinish();
                String apiCrash = "";
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    apiCrash = new String(error.networkResponse.data);
                    log(" Crash----> \n" + apiCrash);
                    error.apiCrash = apiCrash;
                } else
                    log("" + error.getMessage());
                syncEventListner.onSyncFailure(error, mRequest);
            }
        }, params == null ? Request.Method.GET : Request.Method.POST) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                if (getLoginStatus() != null)
                    headers.put("auth_code", getLoginStatus());
                headers.put("User-agent", getUserAgent());
                return headers;
            }


        };
        getRequestQueue().add(multipartRequest);
    }

    private boolean isValidateDateCheck(JSONObject jsonObject, SyncEventListner syncEventListner) throws JSONException {
        if (!jsonObject.has("controller"))
            return true;
        if (isDebug) {
            if (!jsonObject.has("datecheck") || !jsonObject.has("maintainence")) {
                syncEventListner.onSyncFailure(new AppInMaintenance("Your api response does not have datecheck or maintainance key"), null);
                return false;
            }
        } else {
            if (!jsonObject.has("datecheck") || !jsonObject.has("maintainence")) return true;
            if (!checkDateExpire(jsonObject, syncEventListner) || !checkAppInMaintenance(jsonObject, syncEventListner))
                return false;
        }
        return true;
    }

    private boolean checkAppInMaintenance(JSONObject jsonObject, SyncEventListner syncEventListner) throws JSONException {
        if (!jsonObject.getString("maintainence").equals("") && !jsonObject.getString("maintainence").equals("null")) {
            syncEventListner.onSyncFailure(new AppInMaintenance(jsonObject.getString("maintainence")), null);
            return false;
        }
        return true;
    }

    private boolean checkDateExpire(JSONObject jsonObject, SyncEventListner syncEventListner) throws JSONException {
        if (!jsonObject.getString("datecheck").equals("") || !jsonObject.getString("datecheck").equals("null")) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date d = dateFormat.parse(jsonObject.getString("datecheck"));
                cal.setTime(d);
                Calendar currentcal = Calendar.getInstance();
                if (currentcal.after(cal)) {
                    syncEventListner.onSyncFailure(new AppExpiredError(jsonObject.getString("datecheck")), null);
                    return false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean validateResponse(String response, SyncEventListner syncEventListner) {
        if (response.contains("<!DOCTYPE html>")) {
            log(" Crash--->  \n" + response);
            VolleyError error = new VolleyError();
            error.apiCrash = response;
            syncEventListner.onSyncFailure(error, null);
            return false;
        }
        return true;
    }

    @Override
    public void onSyncStart() {

    }

    @Override
    public void onSyncFinish() {

    }

    @Override
    public void onSyncFailure(VolleyError code, Request mRequest) {

    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {

    }

    @Override
    public void onSyncProgress(long progress, long length) {

    }


    private class onVolleyResponse implements Response.Listener<String> {
        SyncEventListner syncEventListner;

        onVolleyResponse(SyncEventListner syncEventListner) {
            this.syncEventListner = syncEventListner;
        }

        @Override
        public void onResponse(String response) {

        }
    }

    private class onVolleyError implements Response.ErrorListener {

        SyncEventListner syncEventListner;

        onVolleyError(SyncEventListner syncEventListner) {
            this.syncEventListner = syncEventListner;
        }

        @Override
        public void onErrorResponse(VolleyError error, Request mRequest) {

        }
    }

    private class onVolleyImageResponse implements Response.Listener<Bitmap> {
        public ImageView image;

        onVolleyImageResponse(ImageView image) {
            this.image = image;
        }

        @Override
        public void onResponse(Bitmap response) {

        }
    }

    private class onVolleyImageError implements Response.ErrorListener {
        public ImageView image;
        int errorImage;

        onVolleyImageError(ImageView image, int errorImage) {
            this.image = image;
            this.errorImage = errorImage;
        }

        @Override
        public void onErrorResponse(VolleyError error, Request mRequest) {

        }
    }
}