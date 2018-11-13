package com.maxlife.fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.activity.MainActivity;
import com.maxlife.activity.SplashActivity;
import com.maxlife.data.SettingData;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    private RelativeLayout emailFrequencyRL;
    private RelativeLayout repeatFrequencyRL;
    private RelativeLayout changePasswordRL;
    private LinearLayout email_1LL;
    private LinearLayout email_2LL;
    private LinearLayout email_3LL;

    private TextView emailFrequencyTV;
    private TextView repeatFrequencyTV;
    private TextView email1TV;
    private TextView email2TV;
    private TextView email3TV;
    private AlertDialog dialog;

    private String[] repeatFrequencyArray;

    String email;
    private EditText emailET;
    private Button okBT;
    private int check;
    public int activate;
    private Switch knowSW;
    private Switch systemSW;

    private boolean isKnowChangeListenerEnable = true;
    private boolean isSystemChangeListenerEnable = true;
    ArrayList<SettingData> emailFrequencyList = new ArrayList<>();

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        baseActivity.getSupportActionBar().setTitle(R.string.settings);
        init();
        return view;
    }

    private void init() {
        changePasswordRL = (RelativeLayout) view.findViewById(R.id.changePasswordRL);
        emailFrequencyRL = (RelativeLayout) view.findViewById(R.id.emailFrequencyRL);
        repeatFrequencyRL = (RelativeLayout) view.findViewById(R.id.repeatFrequencyRL);
        emailFrequencyTV = (TextView) view.findViewById(R.id.emailFrequencyTV);
        repeatFrequencyTV = (TextView) view.findViewById(R.id.repeatFrequencyTV);
        email1TV = (TextView) view.findViewById(R.id.email1TV);
        email2TV = (TextView) view.findViewById(R.id.email2TV);
        email3TV = (TextView) view.findViewById(R.id.email3TV);
        email_1LL = (LinearLayout) view.findViewById(R.id.email_1LL);
        email_2LL = (LinearLayout) view.findViewById(R.id.email2LL);
        email_3LL = (LinearLayout) view.findViewById(R.id.email3LL);

        knowSW = (Switch) view.findViewById(R.id.knowSW);
        systemSW = (Switch) view.findViewById(R.id.systemSW);

        emailFrequencyRL.setOnClickListener(this);
        repeatFrequencyRL.setOnClickListener(this);
        changePasswordRL.setOnClickListener(this);
        email_1LL.setOnClickListener(this);
        email_2LL.setOnClickListener(this);
        email_3LL.setOnClickListener(this);

        initToggleSettings();
        initEmailSettings();

        knowSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isKnowChangeListenerEnable) {
                    if (knowSW.isChecked()) {
                        activate = 1;
                    } else {
                        activate = 0;
                    }
                    RequestParams params = new RequestParams();
                    params.put("UserSetting[is_let]", activate);
                    baseActivity.syncManager.sendToServer("api/user/letthemknow", params, SettingsFragment.this);
                    baseActivity.startDialog();
                } else {
                    isKnowChangeListenerEnable = true;
                }
            }
        });

        systemSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isSystemChangeListenerEnable) {
                    if (systemSW.isChecked()) {
                        activate = 1;
                    } else {
                        activate = 0;
                    }
                    RequestParams params = new RequestParams();
                    params.put(" UserSetting[is_activate]", activate);
                    baseActivity.syncManager.sendToServer("api/user/sys-activate", params, SettingsFragment.this);
                    baseActivity.startDialog();
                } else {
                    isSystemChangeListenerEnable = true;
                }
            }
        });

    }

    private void initSettings() {
        if (appData != null && appData.profileData != null) {
            if (appData.profileData.type_id != 0 && emailFrequencyList != null) {
                for (int i = 0; i < emailFrequencyList.size(); i++) {
                    if (emailFrequencyList.get(i).id == appData.profileData.type_id) {
                        emailFrequencyTV.setText(emailFrequencyList.get(i).title);
                    }
                }
            }
            setDefaultValues();
        }

    }

    private void initEmailSettings() {
        if (appData.profileData.nominatedEmailList.size() == 1) {
            email1TV.setText(appData.profileData.nominatedEmailList.get(0));
            email2TV.setText("");
            email3TV.setText("");
        }
        if (appData.profileData.nominatedEmailList.size() == 2) {
            email1TV.setText(appData.profileData.nominatedEmailList.get(0));
            email2TV.setText(appData.profileData.nominatedEmailList.get(1));
            email3TV.setText("");
        }
        if (appData.profileData.nominatedEmailList.size() >= 3) {
            email1TV.setText(appData.profileData.nominatedEmailList.get(0));
            email2TV.setText(appData.profileData.nominatedEmailList.get(1));
            email3TV.setText(appData.profileData.nominatedEmailList.get(2));
        }
    }

    private void initToggleSettings() {
        if (appData.profileData.is_let == 1) {
            knowSW.setChecked(true);
        } else {
            knowSW.setChecked(false);
        }

        if (appData.profileData.is_activate == 1) {
            systemSW.setChecked(true);
        } else {
            systemSW.setChecked(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        baseActivity.syncManager.sendToServer("api/email-freq/index", null, this);
        baseActivity.startDialog();
        checkApi();
    }

    private void setDefaultValues() {

        if (appData.profileData.type_id == 1) {
            repeatFrequencyArray = baseActivity.getResources().getStringArray(R.array.repeat_daily_string_array);
        } else if (appData.profileData.type_id == 2) {
            repeatFrequencyArray = baseActivity.getResources().getStringArray(R.array.repeat_weekly_string_array);
        } else if (appData.profileData.type_id == 3) {
            repeatFrequencyArray = baseActivity.getResources().getStringArray(R.array.repeat_monthly_string_array);
        }

        try {
            repeatFrequencyTV.setText(repeatFrequencyArray[appData.profileData.repeat_days - 1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emailFrequencyRL:
                showEmailFrequencyDialog();
                break;
            case R.id.repeatFrequencyRL:
                showRepeatFrequencyPopup();
                break;
            case R.id.changePasswordRL:
                ((MainActivity) baseActivity).loadFragment(new ChangePasswordFragment());
                break;
            case R.id.email_1LL:
                check = 0;
                nominatedEmailDialog();
                break;
            case R.id.email2LL:
                check = 1;
                nominatedEmailDialog();
                break;
            case R.id.email3LL:
                check = 2;
                nominatedEmailDialog();
                break;

        }
    }

    private void nominatedEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
        View view = baseActivity.inflater.inflate(R.layout.dialog_enter_email, null);
        emailET = (EditText) view.findViewById(R.id.emailET);
        okBT = (Button) view.findViewById(R.id.okBT);
        dialog = builder.create();

        if (check == 0) {
            emailET.setText(email1TV.getText().toString());
        } else if (check == 1) {
            emailET.setText(email2TV.getText().toString());
        }
        else if (check == 2) {
            emailET.setText(email3TV.getText().toString());
        }
        email = emailET.getText().toString();

        okBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!email.isEmpty() && email.equals(emailET.getText().toString())) {
                    baseActivity.showToast(baseActivity.getString(R.string.no_changes));
                    dialog.dismiss();
                    return;
                }
                if (email.equals(emailET.getText().toString())) {
                    dialog.dismiss();
                    return;
                }
                if (!emailET.getText().toString().isEmpty()) {
                    if (emailET.getText().toString().equalsIgnoreCase(email1TV.getText().toString())) {
                        baseActivity.showToast(baseActivity.getString(R.string.this_email_already_saved));
                        return;
                    }
                    if (emailET.getText().toString().equalsIgnoreCase(email2TV.getText().toString())) {
                        baseActivity.showToast(baseActivity.getString(R.string.this_email_already_saved));
                        return;
                    }
                    if (emailET.getText().toString().equalsIgnoreCase(email3TV.getText().toString())) {
                        baseActivity.showToast(baseActivity.getString(R.string.this_email_already_saved));
                        return;
                    }
                }

                if (!isValidEmail(emailET.getText().toString())) {
                    baseActivity.showToast(baseActivity.getString(R.string.nominated_email_is_not_valid));
                    return;
                }
                String url;
                if(appData.profileData.is_membership==1){
                     url = "api/user/add-email?id="+prefStore.getString("plan_id");
                }
                else{
                    url = "api/user/add-email";
                }
                RequestParams params = new RequestParams();
                String str = "";
                if (check == 0) {
                    if (!emailET.getText().toString().isEmpty()) {
                        str = str + emailET.getText().toString();
                    }
                    if (!email2TV.getText().toString().isEmpty()) {
                        str = str + "," + email2TV.getText().toString();
                    }
                    if (!email3TV.getText().toString().isEmpty()) {
                        str = str + "," + email3TV.getText().toString();
                    }
                } else if (check == 1) {
                    if (!email1TV.getText().toString().isEmpty()) {
                        str = str + email1TV.getText().toString();
                    }
                    if (!emailET.getText().toString().isEmpty()) {
                        str = str + "," + emailET.getText().toString();
                    }
                    if (!email3TV.getText().toString().isEmpty()) {
                        str = str + "," + email3TV.getText().toString();
                    }
                } else if (check == 2) {
                    if (!email1TV.getText().toString().isEmpty()) {
                        str = str + email1TV.getText().toString();
                    }
                    if (!email2TV.getText().toString().isEmpty()) {
                        str = str + "," + email2TV.getText().toString();
                    }
                    if (!emailET.getText().toString().isEmpty()) {
                        str = str + "," + emailET.getText().toString();
                    }
                }
                if (str.indexOf(",") == 0) {
                    str = str.substring(1);
                }
                if (str.isEmpty()) {
                    baseActivity.showToast(baseActivity.getString(R.string.please_enter_nominated_email_address));
                    return;
                }
                params.put("Post[email]", str);
                baseActivity.syncManager.sendToServer(url, params, SettingsFragment.this);
                baseActivity.startDialog();
                if (check == 0) {
                    email1TV.setText(emailET.getText().toString());
                } else if (check == 1) {
                    email2TV.setText(emailET.getText().toString());
                }
                else if (check == 2) {
                    email3TV.setText(emailET.getText().toString());
                }
                dialog.dismiss();

            }
        });
        dialog.setView(view);
        dialog.show();

    }

    private void showRepeatFrequencyPopup() {
        if (repeatFrequencyArray != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
            builder.setTitle(baseActivity.getString(R.string.repeat_interval));
            builder.setItems(repeatFrequencyArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (appData != null && appData.profileData != null) {
                        emailFrequencyApi(appData.profileData.type_id, which + 1);
                    }
                }
            });
            builder.show();
        } else {
            baseActivity.showToast(baseActivity.getString(R.string.please_select_email_frequency_first));
        }
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("email-freq") && action.equalsIgnoreCase("index")) {
            if (status) {
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("list");
                    emailFrequencyList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                            SettingData data = new SettingData(Parcel.obtain());
                            data.id = object.getInt("id");
                            data.title = object.getString("title");
                            emailFrequencyList.add(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    initSettings();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("add-freq")) {
            if (status) {
                try {
                    if (jsonObject.has("details")) {
                        if (appData != null && appData.profileData != null) {
                            JSONObject detailsObject = jsonObject.getJSONObject("details");
                            appData.profileData.type_id = detailsObject.getInt("type_id");
                            appData.profileData.repeat_days = detailsObject.getInt("repeat_days");
                        }
                        initSettings();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (jsonObject.has("error")) {
                        baseActivity.showToast(jsonObject.getString("error"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("add-email")) {
            if (status) {
                try {
                    if (jsonObject.has("list")) {
                        appData.profileData.nominatedEmailList.clear();
                        JSONArray emailListArray = jsonObject.getJSONArray("list");
                        for (int i = 0; i < emailListArray.length(); i++) {
                            JSONObject object = emailListArray.getJSONObject(i);
                            appData.profileData.nominatedEmailList.add(object.getString("email"));
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("letthemknow")) {
            if (status) {
                try {
                    if (jsonObject.has("post")) {
                        JSONObject postObject = jsonObject.getJSONObject("post");
                        if (postObject.has("UserSetting")) {
                            JSONObject userSettingObject = postObject.getJSONObject("UserSetting");
                            if (appData != null && appData.profileData != null) {
                                appData.profileData.is_let = userSettingObject.getInt("is_let");
                            }
                        }
                        if (jsonObject.has("message")) {
                            baseActivity.showToast(jsonObject.getString("message"));
                        }
                        initToggleSettings();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {

                    if (jsonObject.has("message")) {
                        baseActivity.showToast(jsonObject.getString("message"));
                    }
                    isKnowChangeListenerEnable = false;
                    knowSW.setChecked(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("sys-activate")) {
            if (status) {
                try {

                    if (jsonObject.has("post")) {
                        JSONObject postObject = jsonObject.getJSONObject("post");
                        if (postObject.has("UserSetting")) {
                            JSONObject userSettingObject = postObject.getJSONObject("UserSetting");
                            if (appData != null && appData.profileData != null) {
                                appData.profileData.is_activate = userSettingObject.getInt("is_activate");
                            }
                        }
                        if (jsonObject.has("message")) {
                            baseActivity.showToast(jsonObject.getString("message"));
                            generateNotification(baseActivity, jsonObject.getString("message"));
                        }
                        initToggleSettings();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else{
                try {

                    if (jsonObject.has("message")) {
                        baseActivity.showToast(jsonObject.getString("message"));
                    }
                    if (jsonObject.has("error")) {
                        baseActivity.showToast(jsonObject.getString("error"));
                    }

                    isSystemChangeListenerEnable = false;
                    systemSW.setChecked(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void showEmailFrequencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
        builder.setTitle(baseActivity.getString(R.string.set_email_frequency));

        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < emailFrequencyList.size(); i++) {
            strings.add(emailFrequencyList.get(i).title);

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(baseActivity, android.R.layout.simple_list_item_1, strings);
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                emailFrequencyApi(emailFrequencyList.get(which).id, 1);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void emailFrequencyApi(int email_freq, int repeat) {
        RequestParams params = new RequestParams();
        params.put("UserSetting[email_freq]", email_freq);
        params.put("UserSetting[days]", repeat);
        baseActivity.syncManager.sendToServer("api/user/add-freq", params, this);
        baseActivity.startDialog();
    }

    public boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else if (target.length() == 0) {
            return true;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    public void generateNotification(Context context, String message) {

        int nid = 1;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.notifications)
                .setContentTitle("MaxLife")
                .setContentText(message).setAutoCancel(true);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(message);
        mBuilder.setStyle(bigTextStyle);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(notificationSound);
        long[] vibrate = {0, 100, 200, 300};
        mBuilder.setVibrate(vibrate);

        PendingIntent resultPendingIntent = null;
        Intent resultIntent = new Intent(context, SplashActivity.class);
        resultIntent.putExtra("isPush_system_activate", true);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        resultPendingIntent = stackBuilder.getPendingIntent(
                nid, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(nid, mBuilder.build());
    }


}
