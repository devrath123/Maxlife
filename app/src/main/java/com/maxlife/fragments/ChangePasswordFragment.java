package com.maxlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.maxlife.R;
import com.maxlife.activity.MainActivity;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anshul.mittal on 9/5/16.
 */
public class ChangePasswordFragment extends BaseFragment {

    private EditText oldPasswordET;
    private EditText newPasswordET;
    private EditText confirmPasswordET;
    private Button changePasswordBT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        ((MainActivity) baseActivity).getSupportActionBar().setTitle(baseActivity.getString( R.string.change_password_));
        ((MainActivity) baseActivity).setTitle(baseActivity.getString( R.string.change_password_));

        oldPasswordET = (EditText) view.findViewById(R.id.oldPasswordET);
        newPasswordET = (EditText) view.findViewById(R.id.newPasswordET);
        confirmPasswordET = (EditText) view.findViewById(R.id.confirmPasswordET);
        changePasswordBT = (Button) view.findViewById(R.id.changePasswordBT);

        changePasswordBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });

        return view;
    }

    private void validateForm() {
        if (oldPasswordET.getText().toString().equals("")) {
            baseActivity.showToast(baseActivity.getString(R.string.please_enter_old_password));
        }else if (oldPasswordET.getText().toString().length()<8){
            baseActivity.showToast(baseActivity.getString(R.string.password_should_have_min_8_char));

        }
        else if (newPasswordET.getText().toString().equals("")) {
            baseActivity.showToast(baseActivity.getString(R.string.please_enter_new_password));
        }
        else if (newPasswordET.getText().toString().length()<8){
            baseActivity.showToast(baseActivity.getString(R.string.password_should_have_min_8_char));

        }
        else if (confirmPasswordET.getText().toString().equals("")) {
            baseActivity.showToast(baseActivity.getString(R.string.please_enter_confirm_password));
        }else if (confirmPasswordET.getText().toString().length()<8){
            baseActivity.showToast(baseActivity.getString(R.string.password_should_have_min_8_char));

        }

        else if (!newPasswordET.getText().toString().equals(confirmPasswordET.getText().toString())) {
            baseActivity.showToast(baseActivity.getString(R.string.new_password_and_retype_password_doest_match));
        } else {
            changePasswordApi();
        }
    }

    private void changePasswordApi() {
        RequestParams params = new RequestParams();
        params.put("User[old_password]", oldPasswordET.getText().toString());
        params.put("User[password]", newPasswordET.getText().toString());
        params.put("User[password_repeat]", confirmPasswordET.getText().toString());
        if (baseActivity.checkBeforeApi()) {
            syncManager.sendToServer("api/user/changepassword", params, this);
            baseActivity.startDialog();
        }
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("changepassword")) {
            if (status) {
                if (jsonObject.has("message")) {
                    try {
                        baseActivity.showToast(jsonObject.getString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ((MainActivity) baseActivity).loadFragment(new HomeFragment());
                }
            } else {
                if (jsonObject.has("error")) {
                    try {
                        baseActivity.showToast(jsonObject.getString("error"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    baseActivity.showToast(baseActivity.getString(R.string.try_again));
                }
            }
        }
    }
}
