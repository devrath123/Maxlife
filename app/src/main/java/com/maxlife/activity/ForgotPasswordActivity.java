package com.maxlife.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.maxlife.R;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anshul.mittal on 9/5/16.
 */
public class ForgotPasswordActivity extends BaseActivity {

    private EditText emailET;
    private Button sendBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.forgot_password));

        emailET = (EditText) findViewById(R.id.emailET);
        sendBT = (Button) findViewById(R.id.sendBT);

        sendBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });

    }

    private void validateForm() {
        if (emailET.getText().toString().equals("")) {
            showToast(getString(R.string.Please_enter_email_address));
        } else if (!isValidEmail(emailET.getText().toString())) {
            showToast(getString(R.string.email_not_valid));
        } else {
            forgetPasswordApi();
        }
    }

    private void forgetPasswordApi() {
        RequestParams params = new RequestParams();
        params.put("User[email]", emailET.getText().toString());
        if (checkBeforeApi()) {
            syncManager.sendToServer("api/user/recover", params, this);
            startDialog();
        }
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

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("recover")) {
            if (status) {
                if (jsonObject.has("message")) {
                    try {
                        showToast(jsonObject.getString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            } else {
                showToast(getString(R.string.please_check_your_email_address));
            }
        }
    }
}
