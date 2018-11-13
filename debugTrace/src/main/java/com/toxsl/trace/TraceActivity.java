package com.toxsl.trace;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TraceActivity extends AppCompatActivity  {

	String data = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExceptionHandler.register(this);
	}

}
