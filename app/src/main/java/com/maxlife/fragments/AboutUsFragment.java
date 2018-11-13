package com.maxlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.maxlife.R;
import com.maxlife.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anshul.mittal on 9/5/16.
 */
public class AboutUsFragment extends BaseFragment {

    WebView aboutUsWV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        ((MainActivity) baseActivity).getSupportActionBar().setTitle(baseActivity.getString( R.string.about_us));
        ((MainActivity) baseActivity).setTitle(baseActivity.getString( R.string.about_us));

        aboutUsWV = (WebView) view.findViewById(R.id.aboutUsWV);

        aboutUsWV.getSettings().setLoadsImagesAutomatically(true);
        aboutUsWV.getSettings().setJavaScriptEnabled(true);
        aboutUsWV.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        getAboutUsApi();

        return view;
    }

    public void getAboutUsApi(){
        syncManager.sendToServer("api/page/get?title=About", null, this);
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("page") && action.equalsIgnoreCase("get")){
            if (status){
                try {
                    JSONObject pageObject = jsonObject.getJSONObject("page");
                    aboutUsWV.loadData(pageObject.getString("description"), "text/html; charset=UTF-8", null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        aboutUsWV.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        aboutUsWV.onPause();
    }
}

