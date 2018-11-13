package com.maxlife.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.maxlife.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by anshul.mittal on 7/5/16.
 */
public class SplashActivity extends BaseActivity {

    private static int SPLASH_DISPLAY_LENGTH = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        initGCM();
        keyHash();
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("check")){
            if (status){
                try {
                    if (jsonObject.has("detail")) {
                        appData.profileData = getProfileData(jsonObject.getJSONObject("detail"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Bundle bundle = getIntent().getExtras();
                if (bundle != null && bundle.containsKey("isPush")) {
                    if (bundle.containsKey("to_user") && bundle.getString("to_user").equalsIgnoreCase(appData.profileData.email)) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(R.string.you_are_not_logged_in);
                        builder.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                exitFromApp();
                            }
                        });
                        try {
                            builder.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    if (appData.profileData.is_membership == 0 ) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }else {
                syncManager.setLoginStatus(null);
                Intent intent = new Intent(this, SignInActivity.class);
                Bundle bundle = getIntent().getExtras();
                if (bundle != null && bundle.containsKey("isPush")) {
                    intent.putExtras(bundle);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        } else if (controller.equalsIgnoreCase("Authenticate") && action.equalsIgnoreCase("auth_code")){
            if (!status){
                syncManager.setLoginStatus(null);
                Intent intent = new Intent(this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    public void keyHash() {

        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.maxlife", PackageManager.GET_SIGNATURES);

            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.d("KeyHash:>>>>>>>>>>>>>>>", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("excepp", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.d("excepp", e.toString());
        }
    }
}
