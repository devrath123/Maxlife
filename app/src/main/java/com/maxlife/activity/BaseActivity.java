package com.maxlife.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.maxlife.BuildConfig;
import com.maxlife.MyGcmListenerService;
import com.maxlife.MyRegistrationIntentService;
import com.maxlife.R;
import com.maxlife.data.ApplicationData;
import com.maxlife.data.ImageData;
import com.maxlife.data.PostData;
import com.maxlife.data.ProfileData;
import com.maxlife.data.VideoData;
import com.maxlife.utils.Constants;
import com.maxlife.utils.NetworkUtil;
import com.maxlife.utils.PrefStore;
import com.toxsl.trace.ExceptionHandler;
import com.toxsl.volley.AuthFailureError;
import com.toxsl.volley.NetworkError;
import com.toxsl.volley.ParseError;
import com.toxsl.volley.Request;
import com.toxsl.volley.ServerError;
import com.toxsl.volley.TimeoutError;
import com.toxsl.volley.VolleyError;
import com.toxsl.volley.toolbox.RequestParams;
import com.toxsl.volley.toolbox.SyncEventListner;
import com.toxsl.volley.toolbox.SyncManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpStatus;

/**
 * Created by anshul.mittal on 2/5/16.
 */
public class BaseActivity extends AppCompatActivity implements SyncEventListner {

    public SyncManager syncManager;
    public LayoutInflater inflater;
    public ApplicationData appData;


    private Toast myColorToast;
    public PrefStore prefStore;
    public PermCallback permCallback;

    public ProgressDialog progressDialog;
    private NetworksBroadcast networksBroadcast;
    public InputMethodManager imm;
    private android.app.AlertDialog.Builder failureDailog;
    private android.support.v7.app.AlertDialog networkAlertDialog;
    private String networkStatus;

    public boolean isImagePost;
    public boolean isVideoPost;
    public boolean isMessagePost;
    public int postCount;


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 123456789;
    private BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MyGcmListenerService.REGISTRATION_COMPLETE)) {
                String token = intent.getStringExtra("token");
                log("GCM Token: " + token);
                if (syncManager.getLoginStatus() == null) {
                    gotoLoginScreen();
                } else if (token != null) {
                    checkApi();
                }
            }
        }
    };

    protected void gotoLoginScreen() {
        gotoLoginActivity();
    }

    public Dialog getDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_buy_plan_for);
        dialog.getWindow().setBackgroundDrawable((new ColorDrawable(android.graphics.Color.TRANSPARENT)));
        dialog.show();
        return dialog;
    }

    protected void checkApi() {
        String token = prefStore.getString(Constants.DEVICE_TOKEN);
        RequestParams params = new RequestParams();
        if (token != null) {
            params.put("AuthSession[device_token]", token);
        }
        syncManager.sendToServer("api/user/check", params, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefStore = new PrefStore(this);
        syncManager = SyncManager.getInstance(getApplicationContext(), true);
        syncManager.setBaseUrl(Constants.SERVER_REMOTE_URL, getString(R.string.app_name));
        appData = (ApplicationData) getApplication();
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initializeNetworkBroadcast();
        myColorToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        failureDailog = new android.app.AlertDialog.Builder(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ExceptionHandler.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandleMessageReceiver);
    }

    public void initGCM() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.DISPLAY_MESSAGE_ACTION);
        intentFilter.addAction(MyGcmListenerService.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mHandleMessageReceiver, intentFilter);

        //Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, MyRegistrationIntentService.class);
        startService(intent);

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                log(getString(R.string.device_nt_supportted));
                finish();
            }
            return false;
        }
        return true;
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwork();
    }

    public void startDialog() {
        if (progressDialog!=null && !progressDialog.isShowing()) {
            if (isNetworkAvailable()) {
                progressDialog.show();
            } else {
                showToast(getString(R.string.network_not_available_check_internet_connection));
            }
        }
    }

    public void stopDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    android.app.AlertDialog.Builder errorDialog(String errorString, final Request mRequest) {
        failureDailog.setMessage(errorString).setCancelable(false).setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSyncStart();
                syncManager.getRequestQueue().add(mRequest);
            }
        });
        return failureDailog;
    }

    public boolean checkBeforeApi() {
        return isNetworkAvailable();
    }

    public void checkNetwork() {
        if (isNetworkAvailable())
            return;
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle(R.string.network_connection);
        myDialog.setMessage(R.string.you_are_not_connected_to_any_network_press_ok_to_change_settings);
        myDialog.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isNetworkAvailable()) {
                    dialog.dismiss();
                    checkNetwork();
                }
            }
        });
        myDialog.setNegativeButton(R.string.exit,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        exitFromApp();
                        dialog.dismiss();
                    }
                });
        myDialog.setCancelable(false);
        AlertDialog alertd = myDialog.create();
        if (!alertd.isShowing()) {
            alertd.show();
        }
    }

    public void log(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e("MaxLife log ", msg);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null
                && activeNetworkInfo.isConnectedOrConnecting();

    }

    private void initializeNetworkBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        networksBroadcast = new NetworksBroadcast();
        registerReceiver(networksBroadcast, intentFilter);
    }

    public void showToast(String msg) {
        myColorToast.setText(msg);
        myColorToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (networksBroadcast != null) {
                unregisterReceiver(networksBroadcast);
            }
        } catch (IllegalArgumentException e) {
            networksBroadcast = null;
        }
    }

    public boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public boolean isValidMobile(String phone) {
        if (phone == null) {
            return false;
        } else if (phone.isEmpty()) {
            return true;
        } else {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }

    public void exitFromApp() {

        finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public ProfileData getProfileData(JSONObject jsonObject) {
        ProfileData profileData = new ProfileData(Parcel.obtain());
        try {
            profileData.id = jsonObject.getInt("id");
            profileData.full_name = jsonObject.getString("full_name");
            profileData.email = jsonObject.getString("email");
            profileData.role_id = jsonObject.getString("role_id");
            profileData.first_name = jsonObject.getString("first_name");
            profileData.last_name = jsonObject.getString("last_name");
            profileData.contact_no = jsonObject.getString("contact_no");
            profileData.nominated_email = jsonObject.getString("nominated_email");
            profileData.is_membership = jsonObject.getInt("is_membership");
            profileData.image_file = jsonObject.getString("image_file");
            if (jsonObject.has("emails")) {
                JSONArray emailsJsonArray = jsonObject.getJSONArray("emails");
                for (int i = 0; i < emailsJsonArray.length(); i++) {
                    JSONObject emailsJsonObject = emailsJsonArray.getJSONObject(i);
                    profileData.nominatedEmailList.add(emailsJsonObject.getString("email"));
                }
            }
            if (jsonObject.has("settings")) {
                JSONArray settingsJsonArray = jsonObject.getJSONArray("settings");
                if (settingsJsonArray.length() != 0) {
                    JSONObject settingsJsonObject = settingsJsonArray.getJSONObject(0);
                    profileData.is_let = settingsJsonObject.getInt("is_let");
                    profileData.is_activate = settingsJsonObject.getInt("is_activate");
                    profileData.type_id = settingsJsonObject.getInt("type_id");
                    profileData.repeat_days = settingsJsonObject.getInt("repeat_days");
                }
            }
        } catch (JSONException e) {
            log("There is problem in profileData Parsing, Please correct it");
            e.printStackTrace();
        }
        return profileData;
    }

    public PostData getPostData(JSONObject jsonObject) {
        PostData postData = new PostData(Parcel.obtain());
        try {
            postData.id = jsonObject.getInt("id");
            postData.type_id = jsonObject.getInt("type_id");
            postData.create_time = jsonObject.getString("create_time");
            postData.tagged_email = jsonObject.getString("tagged_email");

            if (jsonObject.has("content")) {
                if (jsonObject.getString("content").length() > 0) {
                    postData.content = jsonObject.getString("content");
                } else {
                    postData.content = "";
                }
            }
            if (jsonObject.has("media")) {
                JSONArray mediaArray = jsonObject.getJSONArray("media");
                postData.image_file_list = new ArrayList<>();
                for (int i = 0; i < mediaArray.length(); i++) {
                    ImageData imageData = new ImageData(Parcel.obtain());
                    JSONObject imageObject = mediaArray.getJSONObject(i);
                    if (imageObject.getInt("type_id") == Constants.TYPE_IMAGE) {

                        imageData.id = imageObject.getInt("id");
                        imageData.post_id = imageObject.getInt("model_id");
                        imageData.caption = imageObject.getString("caption");
                        imageData.type_id = imageObject.getInt("type_id");
                        imageData.image_file = imageObject.getString("model_file");
                        postData.image_file_list.add(imageData);
                    }


                }
            }

            if (jsonObject.has("media")) {
                JSONArray mediaArray = jsonObject.getJSONArray("media");

                for (int i = 0; i < mediaArray.length(); i++) {
                    if (mediaArray.getJSONObject(i).getInt("type_id") == 1) {
                        VideoData videoData = new VideoData(Parcel.obtain());
                        JSONObject videoObject = mediaArray.getJSONObject(i);
                        if (videoObject.getInt("type_id") == Constants.TYPE_VIDEO) {
                            videoData.id = videoObject.getInt("id");
                            videoData.post_id = videoObject.getInt("model_id");
                            videoData.caption = videoObject.getString("caption");
                            videoData.video_name = videoObject.getString("file_name");
                            videoData.type_id = videoObject.getInt("type_id");
                            videoData.video_file = videoObject.getString("model_file");
                            videoData.video_thumbnail = videoObject.getString("thumb_file");
                            postData.videoData = videoData;
                        }
                    }
                }

            }
        } catch (JSONException e) {
            log("There is problem in postData Parsing, Please correct it");
            e.printStackTrace();
        }
        return postData;
    }

    public boolean checkPermissions(String[] perms) {

        ArrayList<String> permsArray = new ArrayList<>();
        boolean hasPerms = true;
        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permsArray.add(perm);
                hasPerms = false;
            }
        }
        if (!hasPerms) {
            String[] permsString = new String[permsArray.size()];
            for (int i = 0; i < permsArray.size(); i++) {
                permsString[i] = permsArray.get(i);
            }
            ActivityCompat.requestPermissions(BaseActivity.this, permsString, 99);
            return false;
        } else
            return true;
    }

    public String getDateFormatted(String str) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-d k:m:s", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdfOutput = new SimpleDateFormat("d/MM/yyyy, hh:mm a", Locale.getDefault());

        return sdfOutput.format(date);
    }

    public void logoutApi() {
        if (checkBeforeApi()) {
            syncManager.sendToServer("api/user/logout", null, this);
            startDialog();
        }
    }

    public void logout() {
        syncManager.setLoginStatus(null);
        appData.profileData = null;
        Intent intent = new Intent(BaseActivity.this, SignInActivity.class);
        startActivity(intent);
        this.finish();
    }

    public boolean isImageValid(String picturePath) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap bmp = BitmapFactory.decodeFile(picturePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        if (actualHeight < 1 || actualWidth < 1) {
            showToast(getString(R.string.please_select_some_other_image_this_image_is_not_valid));
            return false;
        }
        return true;
    }

    public Bitmap imageCompressNameCard(String picturePath) {

        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap bmp = BitmapFactory.decodeFile(picturePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        if (actualHeight == 0 || actualWidth == 0) {
            return null;
        }

        float maxHeight = 800;
        float maxWidth = 600;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        } else {
            Bitmap bitmap;
            bitmap = BitmapFactory.decodeFile(picturePath);
            bitmap = Bitmap.createScaledBitmap(bitmap, actualWidth, actualHeight, true);
            return bitmap;
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;

        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(picturePath, options);
        } catch (OutOfMemoryError exception) {
            showToast(getString(R.string.out_of_memory));
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            showToast("Not enough Memory to upload video");
            exception.printStackTrace();

        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        bmp.recycle();
        ExifInterface exif;
        try {
            exif = new ExifInterface(picturePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            if (scaledBitmap != null) {
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scaledBitmap;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int heightRatio = Math.round((float) height / (float) reqHeight);
            int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        float totalPixels = width * height;
        float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean permGrantedBool = false;
        switch (requestCode) {
            case 99:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        showToast(getString(R.string.not_sufficient_permissions)
                                + getString(R.string.app_name)
                                + getString(R.string.permissionss));
                        permGrantedBool = false;
                        break;
                    } else {
                        permGrantedBool = true;
                    }
                }
                if (permCallback != null) {
                    if (permGrantedBool) {
                        permCallback.permGranted();
                    } else {
                        permCallback.permDenied();
                    }
                }
                break;
        }
    }

    public void setPermCallback(PermCallback permCallback) {
        this.permCallback = permCallback;
    }

    public interface PermCallback {
        public void permGranted();

        public void permDenied();
    }


    @Override
    public void onSyncStart() {
        startDialog();
    }

    @Override
    public void onSyncFinish() {
        stopDialog();
    }

    @Override
    public void onSyncFailure(VolleyError error, Request mRequest) {
        handleSyncError(error, mRequest);
        stopDialog();
    }

    private void handleSyncError(VolleyError error, Request mRequest) {

        if (error instanceof NetworkError) {
            errorDialog(getString(R.string.request_timeout_slow_connection), mRequest).show();
        } else if (error instanceof ServerError) {
            showToast(getString(R.string.problem_connecting_to_the_server));
        } else if (error instanceof AuthFailureError) {
            showToast(getString(R.string.session_timeout_redirecting));
            gotoLoginActivity();
        } else if (error instanceof ParseError) {
            showToast(getString(R.string.bad_request));
        } else if (error instanceof TimeoutError) {
            errorDialog(getString(R.string.request_timeout_slow_connection), mRequest).show();
        }
    }

    public void gotoLoginActivity() {
        syncManager.setLoginStatus(null);
        Intent intent = new Intent(BaseActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("logout")) {
            if (status) {
                logout();
            }
        }

        stopDialog();
    }


    @Override
    public void onSyncProgress(long progress, long length) {

    }

    public Bitmap downloadBitmap(String url) {
        HttpURLConnection urlConnection = null;
        try {
            if (url != null) {
                URL uri = new URL(url);
                urlConnection = (HttpURLConnection) uri.openConnection();
                int statusCode = urlConnection.getResponseCode();
                if (statusCode != HttpStatus.SC_OK) {
                    return null;
                }

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    return BitmapFactory.decodeStream(inputStream);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    public static class BlurBuilder {
        private static final float BITMAP_SCALE = 0.4f;
        private static final float BLUR_RADIUS = 2f;

        @SuppressLint("NewApi")
        public static Bitmap blur(BaseActivity context, Bitmap image) {
            int width = Math.round(image.getWidth() * BITMAP_SCALE);
            int height = Math.round(image.getHeight() * BITMAP_SCALE);

            Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

            RenderScript rs = RenderScript.create(context);
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            theIntrinsic.setRadius(BLUR_RADIUS);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);

            return outputBitmap;
        }
    }

    public Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }


    public File convertBitmapToFile(Bitmap bitmap) {

        if (bitmap == null) {
            return null;
        }

        File f = new File(getCacheDir(), "" + System.currentTimeMillis() + ".png");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert fos != null;
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private void showNoNetworkDialog(String status) {
        networkStatus = status;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.netwrk_status));
        builder.setMessage(status);
        builder.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isNetworkAvailable()) {
                    dialog.dismiss();
                    showNoNetworkDialog(networkStatus);
                }
            }
        });
        builder.setCancelable(false);
        networkAlertDialog = builder.create();
        if (!networkAlertDialog.isShowing() && !isFinishing())
            networkAlertDialog.show();
    }

    public class NetworksBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = NetworkUtil.getConnectivityStatusString(context);
            log("Network Status " + status);
            if (status != null)
                showNoNetworkDialog(status);
            else {
                if (networkAlertDialog != null && networkAlertDialog.isShowing())
                    networkAlertDialog.dismiss();
            }
        }
    }

}
