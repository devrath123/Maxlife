package com.toxsl.volley.toolbox;

import android.graphics.Bitmap;

import com.toxsl.volley.Request;
import com.toxsl.volley.VolleyError;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public interface SyncEventListner {

	public void onSyncStart();

	public void onSyncFinish();

	public void onSyncFailure(VolleyError error, Request mRequest);

	public void onSyncSuccess(String controller, String action, boolean status,
							  JSONObject jsonObject);
	public void onSyncProgress(long progress, long length);



}
