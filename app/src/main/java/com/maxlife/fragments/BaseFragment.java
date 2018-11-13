package com.maxlife.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.maxlife.activity.BaseActivity;
import com.maxlife.activity.MainActivity;
import com.maxlife.activity.SignInActivity;
import com.maxlife.data.ApplicationData;
import com.maxlife.utils.Constants;
import com.maxlife.utils.PrefStore;
import com.maxlife.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;
import com.toxsl.volley.Request;
import com.toxsl.volley.VolleyError;
import com.toxsl.volley.toolbox.RequestParams;
import com.toxsl.volley.toolbox.SyncEventListner;
import com.toxsl.volley.toolbox.SyncManager;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by anshul.mittal on 7/5/16.
 */
public class BaseFragment extends Fragment implements SyncEventListner{

    public BaseActivity baseActivity;
    public SyncManager syncManager;
    public ApplicationData appData;
    public PrefStore prefStore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseActivity = (BaseActivity) getActivity();
        syncManager = SyncManager.getInstance(getActivity(),true);
        appData = baseActivity.appData;
        prefStore = baseActivity.prefStore;
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);

    }

    public void checkApi() {
        String token = baseActivity.prefStore.getString(Constants.DEVICE_TOKEN);
        if (token != null) {
            RequestParams params = new RequestParams();
            params.put("AuthSession[device_token]", token);
            syncManager.sendToServer("api/user/check", null, this);
        }
    }

    @Override
    public void onSyncStart() {
    baseActivity.startDialog();
    }

    @Override
    public void onSyncFinish() {
        baseActivity.stopDialog();
    }

    @Override
    public void onSyncFailure(VolleyError error, Request mRequest) {
        baseActivity.onSyncFailure(error,mRequest);
        baseActivity.stopDialog();
    }



    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("check")) {
            if (status) {
                if (appData != null) {
                    if (jsonObject.has("detail")) {
                        try {
                            appData.profileData = baseActivity.getProfileData(jsonObject.getJSONObject("detail"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (appData == null) {
                    appData = (ApplicationData) getActivity().getApplication();
                    try {
                        appData.profileData = baseActivity.getProfileData(jsonObject.getJSONObject("detail"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (appData.profileData.image_file != null && !appData.profileData.image_file.isEmpty()) {
                            Picasso.with(baseActivity).load(appData.profileData.image_file).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(((MainActivity) baseActivity).drawerProfileImageIV);
                        }
                        ((MainActivity) baseActivity).userNameTV.setText(appData.profileData.full_name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else{
                baseActivity.gotoLoginActivity();
            }
        }
    }

    @Override
    public void onSyncProgress(long progress, long length) {

    }



}
