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

public class SubscriptionCodeActivity extends BaseActivity {

    private EditText codeET;
    private Button submitBT;
    private Button cancelBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_code);

        getSupportActionBar().setTitle(getString( R.string.subscription_code));
        setTitle(getString( R.string.subscription_code));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        codeET = (EditText) findViewById(R.id.codeET);
        submitBT = (Button) findViewById(R.id.submitBT);
        cancelBT = (Button) findViewById(R.id.cancelBT);

        cancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeET.getText().toString().equalsIgnoreCase("")) {
                    showToast(getString( R.string.enter_subscrption_code));
                } else {
                    codeSubscribeApi();
                }
            }
        });

    }

    private void codeSubscribeApi() {
        RequestParams params = new RequestParams();
        params.put("User[code]", codeET.getText().toString());
        params.put("Membership[title]",prefStore.getString("plan_buy_for"));
        syncManager.sendToServer("api/user/subscribe", params, this);
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
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("subscribe")) {
            if (status) {
                Intent intent = new Intent(SubscriptionCodeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                try {
                    if (jsonObject.has("error")) {
                        showToast(jsonObject.getString("error"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
