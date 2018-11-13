package com.maxlife.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maxlife.R;
import com.maxlife.activity.MainActivity;
import com.maxlife.customvideorecorder.CameraConfig;
import com.maxlife.customvideorecorder.CameraPreview;
import com.maxlife.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;

/**
 * Created by anshul.mittal on 1/6/16.
 */
public class UploadVideoFragment extends BaseFragment {

    private static final String TAG = "CameraActivity";

    private static final int FOCUS_AREA_SIZE = 500;
    private static boolean cameraFront = false;
    private static boolean flash = false;
    ImageView capture;
    ImageView switchCamera;
    LinearLayout cameraPreview;
    Button buttonQuality;
    ImageView buttonFlash;
    ImageView chronoRecordingImage;
    Chronometer chrono;
    boolean recording = false;

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
    private long countUp;

    private int i;
    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus", "success!");
            } else {
                // do something...
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
            // get the number of cameras
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {

                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(myContext, R.string.sorry_your_phone_has_only_one_camera, Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        }
    };
    private Timer t;
    private String filename;
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


                    try {
                        t.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    stopChronometer();
                    capture.setImageResource(R.drawable.player_record);

                    releaseMediaRecorder();


                    baseActivity.setRequestedOrientation(
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    saveVid = new Dialog(baseActivity);
                    saveVid.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    saveVid.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    saveVid.setContentView(R.layout.dialog_save_video);

                    saveVid.setCancelable(false);

                    saveBTDialog = (Button) saveVid.findViewById(R.id.saveBTDialog);
                    Button cancelBTDialog = (Button) saveVid.findViewById(R.id.cancelBTDialog);

                    saveBTDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri fileContentUri = Uri.fromFile(videoFile);
                            mediaScannerIntent.setData(fileContentUri);
                            baseActivity.sendBroadcast(mediaScannerIntent);

                            baseActivity.appData.videoToUpload = videoFile.getPath();

                            appData.videoToUploadBitmap = ThumbnailUtils.createVideoThumbnail(videoFile.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                            baseActivity.appData.is_video_recording = true;

                            if (saveVid.isShowing()) {
                                saveVid.dismiss();
                            }

                            if (appData != null && !appData.videoToUpload.equals("")) {


                                if (appData != null && appData.mode.equalsIgnoreCase("edit")) {
                                    if (appData.modePostType == Constants.TYPE_IMAGE && appData.videoToUpload != null && !appData.videoToUpload.isEmpty()) {
                                        baseActivity.showToast("You are editing image post, please unselect video");
                                    } else if (appData.modePostType == Constants.TYPE_VIDEO && appData.uploadImagesList.size() > 0) {
                                        baseActivity.showToast("You are editing video post, please unselect images");
                                    } else {
                                        baseActivity.finish();
                                    }
                                } else {
                                    Intent intent = new Intent(baseActivity, MainActivity.class);
                                    intent.putExtra("newConfirmType", Constants.TYPE_VIDEO);
                                    baseActivity.startActivity(intent);
                                    baseActivity.finish();
                                }
                            }


                        }
                    });

                    cancelBTDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                if (saveVid.isShowing()) {
                                    saveVid.dismiss();
                                }
                                Toast.makeText(baseActivity, "Video have been deleted.", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            recording = false;
                            doneCaptureing = true;
                            if (videoFile.exists() && videoFile.isFile()) {
                                videoFile.delete();
                            }
                            t = null;

                            appData.galleryTabSelected = 4;
                            try {
                                startActivity(baseActivity.getIntent());
                                baseActivity.overridePendingTransition(0, 0);
                                baseActivity.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    });

                    saveVid.show();
                }
            } else {
                if (!prepareMediaRecorder()) {
                    Toast.makeText(baseActivity, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                    baseActivity.getSupportFragmentManager().popBackStack();
                    return;
                } else {

                    baseActivity.runOnUiThread(new Runnable() {

                        public void run() {

                            // If there are stories, add them to the table
                             TextView timerTV = (TextView) view.findViewById(R.id.timerTV);
                            timerTV.bringToFront();

                            i = 0;

                            try {
                                ha = new Handler();
                                ha.post(new MyRunnable(timerTV) {
                                    @Override
                                    public void run() {
                                        if (i == 0) {
                                            changeCameraside = false;
                                            ha.removeCallbacks(this);
                                            tx.setVisibility(View.GONE);
                                            try {
                                                mediaRecorder.start();
                                            } catch (RuntimeException e) {
                                                e.printStackTrace();
                                            }
                                            startChronometer();
                                            if (capture != null)
                                                capture.setFocusableInTouchMode(true);

                                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                                changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                            }
                                            if (capture != null){
                                                capture.setImageResource(R.drawable.player_stop);
                                                buttonFlash.setVisibility(View.GONE);
                                                switchCamera.setVisibility(View.GONE);
                                            }

                                            recording = true;
                                            doneCaptureing = true;

                                            baseActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                            t = new Timer();
                                            t.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    baseActivity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            baseActivity.showToast("Your recording video time exceeding the plan time length of 3 minutes");
                                                            capture.performClick();
                                                            if (t!=null)
                                                            t.cancel();
                                                        }
                                                    });

                                                }
                                            }, (179 * 1000) + 900);

                                        } else {
                                            changeCameraside = true;
                                            if (capture != null)
                                                capture.setFocusableInTouchMode(false);
                                            baseActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                            tx.setVisibility(View.VISIBLE);
                                            tx.setText("" + i--);
                                            t = null;
                                            ha.postDelayed(this, 1000);
                                        }
                                    }
                                });
                            } catch ( Exception ex) {
                                ex.printStackTrace();
                            }

                            try {
                                mPreview = new CameraPreview(myContext, mCamera);
                                cameraPreview.addView(mPreview);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    };
    private class MyRunnable implements Runnable, Chronometer.OnChronometerTickListener{
         TextView tx;
         Long time;

        public MyRunnable(TextView tx) {
            this.tx = tx;
        }
        public MyRunnable(Long time) {
            this.time = time;
        }

        @Override
        public void run() {

        }

        @Override
        public void onChronometerTick(Chronometer chronometer) {

        }
    }
    private int cameraId;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (saveVid.isShowing()) {
            int orientation = this.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                baseActivity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (mCamera != null)
            mCamera.release();
        ButterKnife.reset(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_camera, container, false);

        baseActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        baseActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        myContext = baseActivity;

        ButterKnife.inject(baseActivity);

        capture = (ImageView) view.findViewById(R.id.button_capture);
        switchCamera = (ImageView) view.findViewById(R.id.button_ChangeCamera);
        cameraPreview = (LinearLayout) view.findViewById(R.id.camera_preview);
        cameraPreview.setEnabled(false);
        buttonQuality = (Button) view.findViewById(R.id.buttonQuality);

        buttonFlash = (ImageView) view.findViewById(R.id.buttonFlash);
        buttonFlash.setVisibility(View.VISIBLE);
        switchCamera.setVisibility(View.VISIBLE);
        chronoRecordingImage = (ImageView) view.findViewById(R.id.chronoRecordingImage);
        chrono = (Chronometer) view.findViewById(R.id.textChrono);
        initialize();

        return view;
    }


    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
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
        // for every camera check
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
        if (recording) {
            try {
                if (saveVid.isShowing()) {
                    saveVid.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            recording = false;
            doneCaptureing = true;
            if (videoFile.exists() && videoFile.isFile()) {
                videoFile.delete();
            }
            t = null;

            appData.galleryTabSelected = 4;
            try {
                startActivity(baseActivity.getIntent());
                baseActivity.overridePendingTransition(0, 0);
                baseActivity.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mCamera == null) {

            releaseCamera();


            if (hasCamera(baseActivity)) {
                cameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    buttonFlash.setImageResource(R.mipmap.flash);
                }
            } else if (hasFrontCamera(baseActivity)) {
                cameraId = findFrontFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    buttonFlash.setImageResource(R.mipmap.flash);
                }
            } else {
                Toast toast = Toast.makeText(myContext, R.string.sorry_your_phone_does_not_have_a_camera, Toast.LENGTH_LONG);
                toast.show();
                baseActivity.getSupportFragmentManager().popBackStack();
                return;
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
        }
    }

    public void initialize() {

        mPreview = new CameraPreview(myContext, mCamera);

        cameraPreview.addView(mPreview);


        if (cameraPreview == null) {
            Log.e("NULLLLL", "cameraPreview is null");


        }

        if (mPreview == null) {

            Log.e("NULLLLL", "mPreview is null");
        }
        capture.setOnClickListener(captrureListener);
        switchCamera.setOnClickListener(switchCameraListener);
        buttonFlash.setOnClickListener(flashListener);

    }

    public void chooseCamera() {

        if (!changeCameraside) {
            if (cameraFront) {
                int cameraId = findBackFacingCamera();
                if (cameraId >= 0) {
                    mCamera = Camera.open(cameraId);
                    mPreview.refreshCamera(mCamera);
                }
            } else {
                int cameraId = findFrontFacingCamera();
                if (cameraId >= 0) {

                    mCamera = Camera.open(cameraId);

                    if (flash) {
                        flash = false;
                        buttonFlash.setImageResource(R.mipmap.ic_flash_off);
                        mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }
                    mPreview.refreshCamera(mCamera);
                }
            }
        }
    }

    @Override
    public void onPause() {
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

    private boolean hasFrontCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        } else {
            return false;
        }
    }

    private void changeRequestedOrientation(int orientation) {
        baseActivity.setRequestedOrientation(orientation);
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            if(mCamera!=null)
            mCamera.lock(); // lock camera for later use
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
        try {
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
                mediaRecorder.setProfile(CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P));
                buttonQuality.setText("480p");
            } else {
                mediaRecorder.setProfile(CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW));
                buttonQuality.setText("Low");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date d = new Date();
        String timestamp = String.valueOf(d.getTime());

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MaxLife");

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
            baseActivity.log("UploadCameraFragment " + e.toString());
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;


    }

    private void releaseCamera() {

        if (mCamera != null) {
            try {
                mPreview.getHolder().removeCallback(mPreview);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
            }
        }
    }

    public void setFlashMode(String mode) {

        try {
            if (baseActivity.getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)
                    && mCamera != null
                    && !cameraFront) {

                mPreview.setFlashMode(mode);
                mPreview.refreshCamera(mCamera);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, R.string.exception_changing_flashLight_mode,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startChronometer() {
        if (chrono != null) {
            chrono.setVisibility(View.VISIBLE);

            long startTime = SystemClock.elapsedRealtime();

            chrono.setOnChronometerTickListener(new MyRunnable(startTime) {
                @Override
                public void onChronometerTick(Chronometer arg0) {
                    countUp = (SystemClock.elapsedRealtime() - time) / 1000;

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
}
