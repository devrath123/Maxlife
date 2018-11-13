package com.maxlife.customvideorecorder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxlife.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by akshay.sood on 10/5/16.
 */


public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";

    private static final int FOCUS_AREA_SIZE = 500;
    private static boolean cameraFront = false;
    private static boolean flash = false;
    @InjectView(R.id.button_capture)
    ImageView capture;
    @InjectView(R.id.button_ChangeCamera)
    ImageView switchCamera;
    @InjectView(R.id.camera_preview)
    LinearLayout cameraPreview;
    @InjectView(R.id.buttonQuality)
    Button buttonQuality;
    @InjectView(R.id.listOfQualities)
    ListView listOfQualities;
    @InjectView(R.id.buttonFlash)
    ImageView buttonFlash;
    @InjectView(R.id.chronoRecordingImage)
    ImageView chronoRecordingImage;
    @InjectView(R.id.textChrono)
    Chronometer chrono;
    boolean recording = false;
    View.OnClickListener qualityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!recording) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                        && listOfQualities.getVisibility() == View.GONE) {
                    listOfQualities.setVisibility(View.VISIBLE);
                    listOfQualities.animate().setDuration(200).alpha(95)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {

                                }


                            });
                } else {
                    listOfQualities.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    ImageView button_ChangeCamera;
    private Camera mCamera;
    private CameraPreview mPreview;
    View.OnClickListener flashListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!recording && !cameraFront) {
                if (flash) {
                    flash = false;
                    buttonFlash.setImageResource(R.mipmap.flash);
                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    flash = true;
                    buttonFlash.setImageResource(R.mipmap.ic_flash_off);
                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }
        }
    };
    private MediaRecorder mediaRecorder;
    private Context myContext;
    private String url_file;
    private long countUp;
    private int quality = CamcorderProfile.QUALITY_LOW;
    private int i;
    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {

                Log.i("tap_to_focus", "success!");
            } else {

                Log.i("tap_to_focus", "fail!");
            }
        }
    };
    private File videoFile;
    private Handler ha;
    private Dialog saveVid;
    private Button saveBTDialog;
    private boolean changeCameraside;
    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {


                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };
    private Timer t;
    private String filename;
    PowerManager.WakeLock wakeLock;
    private boolean doneCaptureing = true;

    View.OnClickListener captrureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (recording) {

                if (doneCaptureing) {
                    doneCaptureing = false;

                    try {
                        mediaRecorder.stop();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }

                    stopChronometer();
                    capture.setImageResource(R.drawable.player_record);


                    releaseMediaRecorder();

                    try {
                        if ((getTimeinLong(videoFile.getAbsolutePath()) / 1000) < 5) {
                            recording = false;
                            t.cancel();
                            Toast.makeText(CameraActivity.this, "You can't upload video less than 5 sec.", Toast.LENGTH_SHORT).show();
                            return;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    saveVid = new Dialog(CameraActivity.this);
                    saveVid.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    saveVid.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    saveVid.setContentView(R.layout.dialog_save_video);
                    saveBTDialog = (Button) saveVid.findViewById(R.id.saveBTDialog);
                    Button cancelBTDialog = (Button) saveVid.findViewById(R.id.cancelBTDialog);
                    saveVid.setCancelable(false);
                    saveBTDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (saveVid.isShowing()) {
                                saveVid.dismiss();
                            }
                            recording = false;
                            doneCaptureing = true;
                            t = null;
                        }
                    });

                    cancelBTDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (saveVid.isShowing()) {
                                saveVid.dismiss();
                            }
                            recording = false;
                            doneCaptureing = true;
                            if (videoFile.exists() && videoFile.isFile()) {
                                videoFile.delete();
                            }
                            t = null;
                            changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                            Toast.makeText(CameraActivity.this, "Video have been deleted.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    saveVid.show();
                }
            } else {
                if (!prepareMediaRecorder()) {
                    Toast.makeText(CameraActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                    prepareMediaRecorder();

                }

                runOnUiThread(new Runnable() {

                    public void run() {


                        TextView timerTV = (TextView) findViewById(R.id.timerTV);
                        timerTV.bringToFront();

                        i = 5;

                        try {


                            ha = new Handler();
                            ha.post(new RunnableCustom(timerTV) {
                                @Override
                                public void run() {
                                    if (i == 0) {
                                        changeCameraside = false;
                                        ha.removeCallbacks(this);
                                        timerTV.setVisibility(View.GONE);
                                        try {
                                            if(mediaRecorder==null)
                                            {
                                                prepareMediaRecorder();
                                            }
                                            mediaRecorder.start();

                                        } catch (RuntimeException e) {
                                            e.printStackTrace();
                                        }
                                        startChronometer();
                                        if (capture != null)
                                            capture.setFocusableInTouchMode(true);


                                        if (capture != null)
                                            capture.setImageResource(R.drawable.player_stop);
                                        recording = true;
                                        doneCaptureing = true;

                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        t = new Timer();
                                        t.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), "Your recording video time exceeding the plan time length of " + 180 * 1000 + " Sec", Snackbar.LENGTH_INDEFINITE);
                                                        View view = snack.getView();
                                                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        snack.setAction("Ok", new ClickEvent(snack) {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if (snack.isShown()) {
                                                                    snack.dismiss();
                                                                }
                                                            }
                                                        });
                                                        snack.show();


                                                    }
                                                });

                                            }
                                        }, 180 * 1000);
                                    } else {
                                        changeCameraside = true;
                                        if (capture != null)
                                            capture.setFocusableInTouchMode(false);
                                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        timerTV.setVisibility(View.VISIBLE);
                                        timerTV.setText("" + i--);
                                        t = null;
                                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                            changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                        } else {
                                            changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                        }
                                        ha.postDelayed(this, 1000);
                                    }
                                }
                            });
                        } catch (final Exception ex) {
                        }
                    }
                });

            }
        }
    };
    private int cameraId;

    public static void reset() {
        flash = false;
        cameraFront = false;
    }
    abstract   class ClickEvent implements View.OnClickListener
    {
        public  Snackbar snack;

        ClickEvent(Snackbar snack)
        {
            this.snack =snack;


        }
    }

    abstract   class RunnableCustom implements Runnable
    {
        public  TextView timerTV;

        RunnableCustom(TextView timerTV)
        {
            this.timerTV =timerTV;


        }
    }

    private long getTimeinLong(String selectedPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.fromFile(new File(selectedPath)));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        return timeInMillisec;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        wakeLock.release();
        if (mCamera != null)
            mCamera.release();
        ButterKnife.reset(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_camera);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        button_ChangeCamera = (ImageView) findViewById(R.id.button_ChangeCamera);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        myContext = this;

        ButterKnife.inject(this);

        initialize();
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;

        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }

        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;

        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }



    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {

            releaseCamera();

            final boolean frontal = cameraFront;


            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {


                switchCameraListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(CameraActivity.this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                    }
                };


                cameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    buttonFlash.setImageResource(R.mipmap.ic_flash_off);
                }
            } else if (!frontal) {


                cameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    buttonFlash.setImageResource(R.mipmap.ic_flash_off);
                }
            }
            if (cameraId == -1) {
                return;
            }
            try {
                mCamera = Camera.open(cameraId);
            } catch (Exception e) {
                Log.e("Camera Error", "" + e);

            }
            mPreview.refreshCamera(mCamera);
            reloadQualities(cameraId);

        }
    }


    public void initialize() {

        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.setEnabled(false);
        cameraPreview.addView(mPreview);


        if (cameraPreview == null) {
            Log.e("NULLLLL", "cameraPreview is null");


        }

        if (mPreview == null) {

            Log.e("NULLLLL", "mPreview is null");
        }


        capture.setOnClickListener(captrureListener);
        switchCamera.setOnClickListener(switchCameraListener);
        buttonQuality.setOnClickListener(qualityListener);
        buttonFlash.setOnClickListener(flashListener);


    }

    private void reloadQualities(int idCamera) {


        SharedPreferences prefs = getSharedPreferences("RECORDING", Context.MODE_PRIVATE);

        quality = prefs.getInt("QUALITY", CamcorderProfile.QUALITY_480P);

        changeVideoQuality(quality);

        final ArrayList<String> list = new ArrayList<String>();

        int maxQualitySupported = CamcorderProfile.QUALITY_480P;

        if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_480P)) {
            list.add("480p");
            maxQualitySupported = CamcorderProfile.QUALITY_480P;
        }

        if (!CamcorderProfile.hasProfile(idCamera, quality)) {
            quality = maxQualitySupported;
            updateButtonText(maxQualitySupported);
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listOfQualities.setAdapter(adapter);

        listOfQualities.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);

                buttonQuality.setText(item);

                if (item.equals("480p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_480P);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    listOfQualities.animate().setDuration(200).alpha(0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    listOfQualities.setVisibility(View.GONE);
                                }
                            });
                } else {
                    listOfQualities.setVisibility(View.GONE);
                }
            }

        });

    }

    public void chooseCamera() {

        if (!changeCameraside) {
            if (cameraFront) {
                int cameraId = findBackFacingCamera();
                if (cameraId >= 0) {


                    mCamera = Camera.open(cameraId);


                    mPreview.refreshCamera(mCamera);

                    reloadQualities(cameraId);

                }
            } else {
                int cameraId = findFrontFacingCamera();
                if (cameraId >= 0) {

                    mCamera = Camera.open(cameraId);


                    if (flash) {
                        flash = false;
                        buttonFlash.setImageResource(R.mipmap.flash);
                        mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }


                    mPreview.refreshCamera(mCamera);

                    reloadQualities(cameraId);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseCamera();
    }

    private boolean hasCamera(Context context) {

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private void changeRequestedOrientation(int orientation) {
        setRequestedOrientation(orientation);
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            mCamera.lock();
        }
    }

    private boolean prepareMediaRecorder() {
        if (mediaRecorder != null) {
            return true;
        }
        mediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setVideoEncodingBitRate(400);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (cameraFront) {
                mediaRecorder.setOrientationHint(270);
            } else {
                mediaRecorder.setOrientationHint(90);
            }
        }

        mediaRecorder.setProfile(CamcorderProfile.get(quality));

        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/anthorlopCamera");
        if (!file.exists()) {
            file.mkdirs();
        }

        Date d = new Date();
        String timestamp = String.valueOf(d.getTime());

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + "ISelfTape/videos");

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdir();
        }
        filename = "VID_" + timestamp + ".mp4";
        videoFile = new File(mediaStorageDir, filename);
        if (!videoFile.exists()) {
            try {
                videoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setMaxDuration(CameraConfig.MAX_DURATION_RECORD);
        mediaRecorder.setMaxFileSize(CameraConfig.MAX_FILE_SIZE_RECORD);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;


    }

    private void releaseCamera() {

        if (mCamera != null) {
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void changeVideoQuality(int quality) {
        SharedPreferences prefs = getSharedPreferences("RECORDING", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("QUALITY", quality);

        editor.commit();

        this.quality = quality;

        updateButtonText(quality);
    }

    private void updateButtonText(int quality) {
        if (quality == CamcorderProfile.QUALITY_480P)
            buttonQuality.setText("480p");
        if (quality == CamcorderProfile.QUALITY_720P)
            buttonQuality.setText("720p");
        if (quality == CamcorderProfile.QUALITY_1080P)
            buttonQuality.setText("1080p");
        if (quality == CamcorderProfile.QUALITY_2160P)
            buttonQuality.setText("2160p");
    }

    public void setFlashMode(String mode) {

        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)
                    && mCamera != null
                    && !cameraFront) {

                mPreview.setFlashMode(mode);
                mPreview.refreshCamera(mCamera);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception changing flashLight mode",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startChronometer() {
        if (chrono != null) {
            chrono.setVisibility(View.VISIBLE);

            final long startTime = SystemClock.elapsedRealtime();

            chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer arg0) {
                    countUp = (SystemClock.elapsedRealtime() - startTime) / 1000;

                    if (countUp % 2 == 0) {
                        chronoRecordingImage.setVisibility(View.VISIBLE);
                    } else {
                        chronoRecordingImage.setVisibility(View.INVISIBLE);
                    }

                    String asText = String.format("%02d", countUp / 60) + ":" + String.format("%02d", countUp % 60);
                    chrono.setText(asText);
                }
            });
            chrono.start();
        }
    }

    private void stopChronometer() {
        chrono.stop();
        chronoRecordingImage.setVisibility(View.INVISIBLE);
        chrono.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (recording) {

            if (mediaRecorder != null) {
                if (t == null)
                    mediaRecorder.stop();
            }
            if (chrono != null && chrono.isActivated())
                chrono.stop();

            releaseMediaRecorder();
            recording = false;

            if (videoFile.exists() && videoFile.isFile()) {
                videoFile.delete();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null) {

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                Log.i(TAG, "fancy !");
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            } else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}