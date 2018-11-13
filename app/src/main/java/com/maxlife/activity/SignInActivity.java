package com.maxlife.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.maxlife.BuildConfig;
import com.maxlife.R;
import com.google.gson.JsonIOException;
import com.maxlife.utils.Constants;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SignInActivity extends BaseActivity {

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    private EditText emailET;
    private EditText passwordET;
    private Button loginBT;

    private CheckBox rememberMeCB;
    private TextView forgotPasswordTV;
    private TextView signUpTV;
    private LoginButton facebookBT;

    private boolean isRememberMe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {

            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile2) {

            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        setContentView(R.layout.activity_sign_in);

        getSupportActionBar().setTitle(getString(R.string.app_name));
        setTitle(getString(R.string.app_name));

        emailET = (EditText) findViewById(R.id.emailET);
        passwordET = (EditText) findViewById(R.id.passwordET);
        loginBT = (Button) findViewById(R.id.loginBT);

        rememberMeCB = (CheckBox) findViewById(R.id.rememberMeCB);
        forgotPasswordTV = (TextView) findViewById(R.id.forgotPasswordTV);
        signUpTV = (TextView) findViewById(R.id.signUpTV);
        facebookBT = (LoginButton) findViewById(R.id.facebookBT);


        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLoginForm();
            }
        });

        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        signUpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
      initFacebook();

    }

    private void initFacebook() {

        facebookBT.setReadPermissions(Arrays.asList("email", "public_profile"));


        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                 String token = loginResult.getAccessToken().getToken();

                String[] requiredFields = new String[]{"id", "email", "name", "gender", "birthday"};
                Bundle parameters = new Bundle();
                parameters.putString("fields", TextUtils.join(",", requiredFields));
                GraphRequest requestEmail = new GraphRequest(loginResult.getAccessToken(), "me", parameters, null, new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            GraphRequest.GraphJSONObjectCallback callbackEmail = new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject me, GraphResponse response) {
                                    if (response.getError() != null) {

                                    } else {
                                        String id = "";
                                        String fbemail = "";
                                        String name = "";
                                        try {
                                            id = me.optString("id");
                                        } catch (JsonIOException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            name = me.optString("name");
                                        } catch (JsonIOException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            fbemail = me.optString("email");
                                        } catch (JsonIOException e) {
                                            e.printStackTrace();
                                        }

                                        loginUsingFacebook(id, name, fbemail);
                                    }
                                }
                            };

                            callbackEmail.onCompleted(response.getJSONObject(), response);
                            facebookBT.setVisibility(View.GONE);

                            prefStore.setBoolean("rememberMe", false);
                            prefStore.setString("rememberEmail", "");
                            prefStore.setString("rememberPass", "");

                            emailET.setText(prefStore.getString(""));
                            passwordET.setText(prefStore.getString(""));
                            rememberMeCB.setChecked(false);

                        }
                    }
                });

                requestEmail.executeAsync();
            }

            @Override
            public void onCancel() {
                Intent intent = new Intent(SignInActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                log("Facebook error :" + error.toString());
            }
        });
    }



    private void loginUsingFacebook(String id, String name, String email) {

        String action = "api/user/facebook";
        RequestParams params = new RequestParams();

        String[] stringsname = name.split(" ");
        String first_name = "";
        String last_name = "";
        if (stringsname.length >= 1) {
            first_name = stringsname[0];
        }
        if (stringsname.length >= 2) {
            last_name = stringsname[1];
        }

        params.put("User[userId]", "" + id);
        params.put("User[email]", "" + email);
        params.put("User[first_name]", "" + first_name);
        params.put("User[last_name]", "" + last_name);

        params.put("User[provider]", "" + "facebook");

        String device_token = prefStore.getString(Constants.DEVICE_TOKEN);
        if (device_token.equals("")) {
            device_token = "1234";
        }
        params.put("User[device_token]", device_token);
        params.put("User[device_type]", "1");

        syncManager.sendToServer(action, params, this);
        startDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRememberMe = prefStore.getBoolean("rememberMe");
        if (isRememberMe) {
            emailET.setText(prefStore.getString("rememberEmail"));
            passwordET.setText(prefStore.getString("rememberPass"));
            rememberMeCB.setChecked(true);
        } else {
            emailET.setText(prefStore.getString(""));
            passwordET.setText(prefStore.getString(""));
            rememberMeCB.setChecked(false);
        }
        if(BuildConfig.DEBUG){
            emailET.setText("rawal@toxsl.in");
            passwordET.setText("admin@123");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    private void validateLoginForm() {
        if (emailET.getText().toString().equals("")) {
            showToast(getString(R.string.please_enter_email_address));
        } else if (!isValidEmail(emailET.getText().toString())) {
            showToast(getString(R.string.email_is_not_valid));
        } else if (passwordET.getText().toString().equals("")) {
            showToast(getString(R.string.please_enter_password));
        } else {
            loginApi();
            syncManager.setLoginStatus(null);
        }
    }

    private void loginApi() {
        syncManager.setLoginStatus(null);
        RequestParams params = new RequestParams();
        params.put("email", emailET.getText().toString());
        params.put("password", passwordET.getText().toString());
        String device_token = prefStore.getString(Constants.DEVICE_TOKEN);
      //  if (device_token.equals("")) {
      //      device_token = "1234";
     //   }
      //  params.put("LoginForm[device_token]", device_token);
      //  params.put("LoginForm[device_type]", "1");
        if (checkBeforeApi()) {
            syncManager.sendToServer("user/login", params, this);
            startDialog();
        }
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("login")) {
            if (status) {

                try {
                    if (jsonObject.has("detail")) {
                        appData.profileData = getProfileData(jsonObject.getJSONObject("detail"));

                    }
                    if (jsonObject.has("auth_code")) {
                        syncManager.setLoginStatus(jsonObject.getString("auth_code"));
                    }

                    if (rememberMeCB.isChecked()) {
                        prefStore.setBoolean("rememberMe", true);
                        prefStore.setString("rememberEmail", emailET.getText().toString());
                        prefStore.setString("rememberPass", passwordET.getText().toString());
                    } else {
                        prefStore.setBoolean("rememberMe", false);
                        prefStore.setString("rememberEmail", "");
                        prefStore.setString("rememberPass", "");
                    }

                    showToast(jsonObject.getString("success"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (appData != null && appData.profileData.is_membership == 0) {
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

            } else {
                if (jsonObject.has("error")) {
                    try {
                        showToast(jsonObject.getString("error"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("facebook")) {
            if (status) {
                try {
                    if (jsonObject.has("auth_code")) {
                        syncManager.setLoginStatus(jsonObject.getString("auth_code"));
                    }
                    if (jsonObject.has("detail")) {
                        appData.profileData = getProfileData(jsonObject.getJSONObject("detail"));
                    }
                    showToast(jsonObject.getString("success"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (appData != null && appData.profileData.is_membership == 0) {
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
            } else {
                if (jsonObject.has("error")) {
                    try {
                        showToast(jsonObject.getString("error"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                facebookBT.setVisibility(View.VISIBLE);
            }
        }
    }
}
