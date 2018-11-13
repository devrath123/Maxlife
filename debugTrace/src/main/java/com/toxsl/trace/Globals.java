

package com.toxsl.trace;

public class Globals {
	// This must be set by the application - it used to automatically
	// transmit exceptions to the trace server
	public static String FILES_PATH 				= null;
	public static String APP_VERSION 				= null;
	public static String APP_PACKAGE 				= null;
    public static String PHONE_MODEL 				= null;
    public static String ANDROID_VERSION            = null;
    // Where are the stack traces posted?
	public static String URL						= "http://android.toxsl.in/report.php";
	public static String TraceVersion				= "1.0.0";
}
