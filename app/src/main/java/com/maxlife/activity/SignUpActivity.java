package com.maxlife.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.maxlife.BuildConfig;
import com.maxlife.R;
import com.maxlife.utils.Constants;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anshul.mittal on 7/5/16.
 */
public class SignUpActivity extends BaseActivity {

    private EditText firstNameET;
    private EditText lastNameET;
    private EditText emailET;
    private EditText passwordET;
    private EditText confirmPasswordET;

    private Button signUpBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString( R.string.signup));
        setTitle(getString( R.string.signup));

        firstNameET = (EditText) findViewById(R.id.firstNameET);
        lastNameET = (EditText) findViewById(R.id.lastNameET);
        emailET = (EditText) findViewById(R.id.emailET);
        passwordET = (EditText) findViewById(R.id.passwordET);
        confirmPasswordET = (EditText) findViewById(R.id.confirmPasswordET);
        signUpBT = (Button) findViewById(R.id.signUpBT);

        signUpBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateSignUpForm();
             }
        });
        if(BuildConfig.DEBUG) {
            setDummyData();
        }
    }

    private void setDummyData() {
        firstNameET.setText("test");
        lastNameET.setText("again");
        emailET.setText("again@toxsl.in");
        passwordET.setText("admin123");
        confirmPasswordET.setText("admin123");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private void validateSignUpForm() {
        if (firstNameET.getText().toString().trim().equals("")) {
            showToast(getString(R.string.please_enter_first_name));
        } else if (lastNameET.getText().toString().trim().equals("")) {
            showToast(getString(R.string.please_enter_last_name));
        } else if (emailET.getText().toString().equals("")) {
            showToast(getString(R.string.please_enter_email_address));
        } else if (!isValidEmail(emailET.getText().toString())) {
            showToast(getString(R.string.email_is_not_valid));
        } else if (passwordET.getText().toString().equals("")) {
            showToast(getString(R.string.please_enter_password));
        } else if (passwordET.getText().toString().length() < 8) {
            showToast(getString(R.string.password_should_have_min_8_char));
        } else if (confirmPasswordET.getText().toString().equals("")) {
            showToast(getString(R.string.please_enter_confirm_password));
        } else if (!passwordET.getText().toString().equals(confirmPasswordET.getText().toString())) {
            showToast(getString(R.string.password_and_confirm_password_doest_match));
        } else {
            signUpApi();
        }
    }


    private void signUpApi() {
        RequestParams params = new RequestParams();
        params.put("User[first_name]", firstNameET.getText().toString());
        params.put("User[last_name]", lastNameET.getText().toString());
        params.put("User[email]", emailET.getText().toString());
        params.put("User[password]", passwordET.getText().toString());
        params.put("User[password_repeat]", confirmPasswordET.getText().toString());
        if (checkBeforeApi()) {
            syncManager.sendToServer("api/user/signup", params, this);
            startDialog();
        }
    }

    private void loginApi() {
        syncManager.setLoginStatus(null);
        RequestParams params = new RequestParams();
        params.put("LoginForm[username]", emailET.getText().toString());
        params.put("LoginForm[password]", passwordET.getText().toString());
        String device_token = prefStore.getString(Constants.DEVICE_TOKEN);
        if (device_token.equals("")) {
            device_token = "1234";
        }
        params.put("LoginForm[device_token]", device_token);
        params.put("LoginForm[device_type]", "1");
        if (checkBeforeApi()) {
            syncManager.sendToServer("api/user/login", params, this);
            startDialog();
        }
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("signup")) {
            if (status) {
                prefStore.setBoolean("rememberMe", false);
                prefStore.setString("rememberEmail", "");
                prefStore.setString("rememberPass", "");

                loginApi();
            } else {
                if (jsonObject.has("error")) {
                    try {
                        showToast(jsonObject.getString("error"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("login")) {
            if (status) {

                try {
                    if (jsonObject.has("detail")) {
                        appData.profileData = getProfileData(jsonObject.getJSONObject("detail"));

                    }
                    if (jsonObject.has("auth_code")) {
                        syncManager.setLoginStatus(jsonObject.getString("auth_code"));
                    }

                    showToast(jsonObject.getString("success"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (appData != null && appData.profileData.is_membership == 0) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
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
        }
    }
}
