package com.toxsl.trace;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TraceService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		ExceptionHandler.register(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
