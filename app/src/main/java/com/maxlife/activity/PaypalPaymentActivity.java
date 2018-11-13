package com.maxlife.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.maxlife.data.PayPalData;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;


public class PaypalPaymentActivity extends BaseActivity {

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
    private static final String LIVE_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
    private static final String CONFIG_CLIENT_ID = "ATUVAQ2UEKpuf5S5m3v_kYWOONbzWvQLzNsOHxPcjgyaJsT2i1jJZbaBIDpd40IFLpWQoXKEk5jMT5G5";
    private static final String SECRET_KEY = "EMyWZsIDRzcU0GAooAPMproLmT3ElI4-RjgxBThztEGrrTf0eUBMbi6e92I_7_3JQrPnuFRfLUeyZvld";
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT).clientId(CONFIG_CLIENT_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
        onBuyPressed(getIntent().getStringExtra("amount"));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onBuyPressed(String amount) {
        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_AUTHORIZE, amount);
        Intent intent = new Intent(PaypalPaymentActivity.this,
                PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);

    }

    private PayPalPayment getThingToBuy(String paymentIntent, String rupees) {
        return new PayPalPayment(new BigDecimal(rupees), "USD", "Pay For super power",
                paymentIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {

                        PayPalData paypalData = new PayPalData();
                        JSONObject obj = confirm.toJSONObject();
                        JSONObject subject = confirm.getPayment()
                                .toJSONObject();

                        JSONObject obj2 = obj.getJSONObject("response");
                        paypalData.state = obj2.getString("state");
                        paypalData.id = obj2.getString("id");

                        paypalData.createTime = obj2.getString("create_time");

                        paypalData.short_description = subject
                                .getString("short_description");
                        paypalData.amount = subject.getString("amount");
                        paypalData.currencyCode = subject
                                .getString("currency_code");

                        paypalData.responseType = obj
                                .getString("response_type");

                        saveRecordOnServer(paypalData.id);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                startActivity(new Intent(this, SubscriptionActivity.class));
                finish();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                finish();
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth = data
                        .getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject()
                                .toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        Toast.makeText(getApplicationContext(),
                                "Future Payment code received from PayPal",
                                Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Log.e("FuturePaymentExample",
                                "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                startActivity(new Intent(this, SubscriptionActivity.class));
                finish();
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                finish();
            }
        }
    }

    private void sendAuthorizationToServer(PayPalAuthorization authorization) {


    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void saveRecordOnServer(String transaction_id) {

        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("transaction_id", transaction_id);
        intent.putExtra("plan_id", getIntent().getStringExtra("plan_id"));
        startActivity(intent);
        finish();
    }

    public void onBackPressed() {
        startActivity(new Intent(this, SubscriptionActivity.class));
        finish();
    }
}
