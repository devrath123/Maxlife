package com.maxlife.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.maxlife.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SubscriptionActivity extends BaseActivity {

    private TextView planPriceTV, videoLimitTV, imageLimitTV, textLimitTV;
    private Button upgradePlanBT, continueBT;
    private Button codeBT;
    private EditText buyPlanET;
    private int plan_id = 0;
    private String plan_title = "";
    private long plan_price = 0;
    Boolean isPaypal;
    Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        getSupportActionBar().setTitle(getString(R.string.subscription_plan));
        setTitle(getString(R.string.subscription_plan));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        planPriceTV = (TextView) findViewById(R.id.planPriceTV);
        videoLimitTV = (TextView) findViewById(R.id.videoLimitTV);
        imageLimitTV = (TextView) findViewById(R.id.pictureLimitTV);
        textLimitTV = (TextView) findViewById(R.id.wordLimitTV);
        upgradePlanBT = (Button) findViewById(R.id.upgradePlanBT);
        codeBT = (Button) findViewById(R.id.codeBT);

        upgradePlanBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPaypal = true;
                using_paypal();

            }
        });
        codeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPaypal = false;
                using_paypal();
            }
        });


    }

    private void using_code() {
        Dialog dialog = planDialog();
        buyPlanET = (EditText) dialog.findViewById(R.id.buyPlanET);
        continueBT = (Button) dialog.findViewById(R.id.continueBT);
        continueBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buyPlanET.getText().toString().isEmpty()) {
                    showToast("Please Enter Plan Name");
                } else {
                    Intent intent = new Intent(SubscriptionActivity.this, SubscriptionCodeActivity.class);
                    startActivity(intent);
                    prefStore.saveString("plan_buy_for", buyPlanET.getText().toString());
                }

            }
        });
    }

    private Dialog planDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_rename);
        dialog.getWindow().setBackgroundDrawable((new ColorDrawable(android.graphics.Color.TRANSPARENT)));
        dialog.show();
        return dialog;
    }


    private void using_paypal() {
        dialog = planDialog();
        buyPlanET = (EditText) dialog.findViewById(R.id.buyPlanET);
        continueBT = (Button) dialog.findViewById(R.id.continueBT);
        continueBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buyPlanET.getText().toString().isEmpty()) {
                    showToast("Please Enter Plan Name");
                } else {
                    dialog.dismiss();
                    prefStore.saveString("plan_buy_for", buyPlanET.getText().toString());
                    if(isPaypal)
                    goToPayPal();
                    else{
                        Intent intent = new Intent(SubscriptionActivity.this, SubscriptionCodeActivity.class);
                        startActivity(intent);
                    }

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPlansApi();
    }

    private void getPlansApi() {
        if (checkBeforeApi()) {
            syncManager.sendToServer("api/user/plans", null, this);
            startDialog();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void goToPayPal() {
        Intent intent = new Intent(SubscriptionActivity.this, PaypalPaymentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("amount", "" + plan_price);
        intent.putExtra("plan_id", "" + plan_id);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("plans")) {
            if (status) {
                if (jsonObject.has("detail")) {
                    try {
                        JSONArray detailArray = jsonObject.getJSONArray("detail");
                        if (detailArray.length() > 0) {
                            JSONObject planObject = detailArray.getJSONObject(0);
                            plan_id = planObject.getInt("id");
                            plan_title = planObject.getString("title");
                            plan_price = planObject.getLong("price");
                            if (plan_price > 0) {
                                planPriceTV.setText("" + plan_price);
                            }
                            imageLimitTV.setText(planObject.getJSONObject("setting").getString("image_limit") + " Images");
                            textLimitTV.setText(planObject.getJSONObject("setting").getString("text_length_limit") + " Words");

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }
}
