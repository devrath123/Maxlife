package com.maxlife.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.maxlife.R;
import com.maxlife.activity.MainActivity;
import com.maxlife.data.UploadImgData;
import com.maxlife.utils.Constants;
import com.maxlife.utils.ImageParameters;
import com.maxlife.utils.ImageUtility;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class UploadCameraFragment extends BaseFragment implements SurfaceHolder.Callback, Camera.PictureCallback {

    public static final String TAG = UploadCameraFragment.class.getSimpleName();
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String IMAGE_INFO = "image_info";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private int mCameraID;
    private Camera mCamera;
    private SurfaceView mPreviewView;
    private SurfaceHolder mSurfaceHolder;

    private boolean mIsSafeToTakePhoto = false;
    private ImageParameters mImageParameters;
    private CameraOrientationListener mOrientationListener;

    private ImageView takenPhoto1IV;
    private ImageView takenPhoto2IV;
    private ImageView takenPhoto3IV;

    private ImageView saveIV;
    private ImageView cancelIV;
    private boolean isImageClicked;
    private Bitmap tempBitmap;
    private Button doneBT;

    private ImageView takePhotoBtn;

    private RelativeLayout saveToolBarRL;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOrientationListener = new CameraOrientationListener(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mCameraID = getBackCameraID();
            mImageParameters = new ImageParameters();
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mImageParameters = savedInstanceState.getParcelable(IMAGE_INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_upload_camera, container, false);

        takenPhoto1IV = (ImageView) view.findViewById(R.id.takenPhoto1IV);
        takenPhoto2IV = (ImageView) view.findViewById(R.id.takenPhoto2IV);
        takenPhoto3IV = (ImageView) view.findViewById(R.id.takenPhoto3IV);

        saveIV = (ImageView) view.findViewById(R.id.saveIV);
        cancelIV = (ImageView) view.findViewById(R.id.cancelIV);
        doneBT = (Button) view.findViewById(R.id.doneBT);

        saveToolBarRL = (RelativeLayout) view.findViewById(R.id.saveToolBarRL);

        if (appData != null) {
            if (appData.uploadImagesList.size() > 0) {
                Picasso.with(getActivity()).load(appData.uploadImagesList.get(0).uploadImng).resize(100, 100).into(takenPhoto1IV);
            }
            if (appData.uploadImagesList.size() > 1) {
                Picasso.with(getActivity()).load(appData.uploadImagesList.get(1).uploadImng).resize(100, 100).into(takenPhoto2IV);
            }
            if (appData.uploadImagesList.size() > 2) {
                Picasso.with(getActivity()).load(appData.uploadImagesList.get(2).uploadImng).resize(100, 100).into(takenPhoto3IV);
            }
            if (appData.uploadImagesList.size() == 0) {
                doneBT.setVisibility(View.GONE);
            } else {
                doneBT.setVisibility(View.VISIBLE);
            }
        }

        saveIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoBtn.setClickable(true);
                if (appData != null && appData.uploadImagesList.size() < 3) {
                    if (isImageClicked) {

                        saveToolBarRL.setVisibility(View.GONE);
                        Uri photoUri = ImageUtility.savePicture(getActivity(), tempBitmap);

                        UploadImgData data= new UploadImgData();
                        data.uploadImng=photoUri;
                        appData.uploadImagesList.add(data);

                        if (appData.uploadImagesList.size() > 0) {
                            Picasso.with(getActivity()).load(appData.uploadImagesList.get(0).uploadImng).resize(100, 100).into(takenPhoto1IV);
                        }
                        if (appData.uploadImagesList.size() > 1) {
                            Picasso.with(getActivity()).load(appData.uploadImagesList.get(1).uploadImng).resize(100, 100).into(takenPhoto2IV);
                        }
                        if (appData.uploadImagesList.size() > 2) {
                            Picasso.with(getActivity()).load(appData.uploadImagesList.get(2).uploadImng).resize(100, 100).into(takenPhoto3IV);
                        }
                        isImageClicked = false;
                        if (appData.uploadImagesList.size() == 0) {
                            doneBT.setVisibility(View.GONE);
                        } else {
                            doneBT.setVisibility(View.VISIBLE);
                        }
                        restartPreview();
                    }
                } else {
                    baseActivity.showToast(getString(R.string.only_3_images_are_allowed));
                }
            }
        });

        cancelIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoBtn.setClickable(true);
                saveToolBarRL.setVisibility(View.GONE);
                isImageClicked = false;
                tempBitmap = null;
                if (appData != null && appData.uploadImagesList.size() == 0) {
                    doneBT.setVisibility(View.GONE);
                } else {
                    doneBT.setVisibility(View.VISIBLE);
                }
                restartPreview();
            }
        });

        doneBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appData != null && appData.mode.equalsIgnoreCase("edit")) {
                    if (appData.modePostType == Constants.TYPE_IMAGE && appData.videoToUpload != null && !appData.videoToUpload.isEmpty()) {
                        baseActivity.showToast(baseActivity.getString( R.string.please_unselect_video));
                    } else if (appData.modePostType == Constants.TYPE_VIDEO && appData.uploadImagesList.size() > 0) {
                        baseActivity.showToast(baseActivity.getString( R.string.editing_video_post));
                    } else {
                        baseActivity.finish();
                    }
                } else {


                    Intent intent = new Intent(baseActivity, MainActivity.class);
                    intent.putExtra("newConfirmType", Constants.TYPE_IMAGE);
                    baseActivity.startActivity(intent);
                    baseActivity.finish();
                }
            }
        });

        takenPhoto1IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appData != null && appData.uploadImagesList.size() > 0 && !appData.uploadImagesList.get(0).uploadImng.toString().contains("http")) {
                    Fragment fragment = new FullScreenImageFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("isLocal", Constants.LOCAL_IMAGE);
                    bundle.putString("imageUrl", appData.uploadImagesList.get(0).uploadImng.getPath());
                    bundle.putInt("imagePosition", 0);
                    fragment.setArguments(bundle);
                    baseActivity.getSupportFragmentManager().beginTransaction().replace(R.id.uploadLL, fragment).addToBackStack(null).commitAllowingStateLoss();
                }
            }
        });

        takenPhoto2IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appData != null && appData.uploadImagesList.size() > 1 && !appData.uploadImagesList.get(1).uploadImng.toString().contains("http")) {
                    Fragment fragment = new FullScreenImageFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("isLocal", Constants.LOCAL_IMAGE);
                    bundle.putString("imageUrl",appData.uploadImagesList.get(1).uploadImng.getPath());
                    bundle.putInt("imagePosition", 1);
                    fragment.setArguments(bundle);
                    baseActivity.getSupportFragmentManager().beginTransaction().replace(R.id.uploadLL, fragment).addToBackStack(null).commitAllowingStateLoss();
                }
            }
        });

        takenPhoto3IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appData != null && appData.uploadImagesList.size() > 2 && !appData.uploadImagesList.get(2).uploadImng.toString().contains("http")) {
                    Fragment fragment = new FullScreenImageFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("isLocal", Constants.LOCAL_IMAGE);
                    bundle.putString("imageUrl",appData.uploadImagesList.get(2).uploadImng.getPath());
                    bundle.putInt("imagePosition", 2);
                    fragment.setArguments(bundle);
                    baseActivity.getSupportFragmentManager().beginTransaction().replace(R.id.uploadLL, fragment).addToBackStack(null).commitAllowingStateLoss();
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOrientationListener.enable();

        mPreviewView = (SurfaceView) view.findViewById(R.id.camera_preview_view);
        mPreviewView.getHolder().addCallback(UploadCameraFragment.this);


        mImageParameters.mIsPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (savedInstanceState == null) {
            ViewTreeObserver observer = mPreviewView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageParameters.mPreviewWidth = mPreviewView.getWidth();
                    mImageParameters.mPreviewHeight = mPreviewView.getHeight();

                    mImageParameters.mCoverWidth = mImageParameters.mCoverHeight
                            = mImageParameters.calculateCoverWidthHeight();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mPreviewView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }

        ImageView swapCameraBtn = (ImageView) view.findViewById(R.id.change_camera);
        swapCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                    mCameraID = getBackCameraID();
                } else {
                    mCameraID = getFrontCameraID();
                }
                restartPreview();
            }
        });

        takePhotoBtn = (ImageView) view.findViewById(R.id.capture_image_button);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
                takePhotoBtn.setClickable(false);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putParcelable(IMAGE_INFO, mImageParameters);
        super.onSaveInstanceState(outState);
    }

    private void getCamera(int cameraID) {
        try {
            mCamera = Camera.open(cameraID);
        } catch (Exception e) {
            baseActivity.log(TAG + "Can't open camera with id " + cameraID);
            e.printStackTrace();
        }
    }

    private void restartPreview() {
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }

        getCamera(mCameraID);
        if (mCamera != null) {
            startCameraPreview();
        }
    }

    private void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();

            setSafeToTakePhoto(true);
            setCameraFocusReady(true);
        } catch (IOException e) {
            baseActivity.log(TAG + "Can't start camera preview due to IOException " + e);
            e.printStackTrace();
        }
    }

    private void stopCameraPreview() {
        setSafeToTakePhoto(false);
        setCameraFocusReady(false);

        mCamera.stopPreview();
    }

    private void setSafeToTakePhoto(boolean isSafeToTakePhoto) {
        mIsSafeToTakePhoto = isSafeToTakePhoto;
    }

    private void setCameraFocusReady(boolean isFocusReady) {
        if (this.mPreviewView != null) {
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        // Clockwise rotation needed to align the window display to the natural position
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;


        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {

            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mImageParameters.mDisplayOrientation = displayOrientation;
        mImageParameters.mLayoutOrientation = degrees;

        mCamera.setDisplayOrientation(mImageParameters.mDisplayOrientation);
    }

    private void setupCamera() {

        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);



        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }


        mCamera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    }

    private Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;
        Size size;
        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);
            boolean isDesireRatio = (size.width / 4) == (size.height / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            baseActivity.log(TAG + "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    private int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
        return CameraInfo.CAMERA_FACING_BACK;
    }

    private void takePicture() {

        if (mIsSafeToTakePhoto) {
            setSafeToTakePhoto(false);
            mOrientationListener.rememberOrientation();
            Camera.ShutterCallback shutterCallback = null;
            Camera.PictureCallback raw = null;
            Camera.PictureCallback postView = null;
            mCamera.takePicture(shutterCallback, raw, postView, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mOrientationListener.disable();

        if (mCamera != null) {
            stopCameraPreview();
            mCamera.setPreviewCallback(null);

            mCamera.release();
            mCamera = null;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;

        getCamera(mCameraID);
        if (mCamera != null) {
            startCameraPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case 1:
                Uri imageUri = data.getData();
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        int rotation = getPhotoRotation();
        baseActivity.log("rotation " + rotation);
        baseActivity.log("onPictureTaken " + data.toString());

        setSafeToTakePhoto(true);
        isImageClicked = true;
        saveToolBarRL.setVisibility(View.VISIBLE);

        tempBitmap = ImageUtility.decodeSampledBitmapFromByte(getActivity(), data);

        if (rotation != 0) {
            Bitmap oldBitmap = tempBitmap;

            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);

            tempBitmap = Bitmap.createBitmap(
                    oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, false
            );

            oldBitmap.recycle();
        }
    }

    private int getPhotoRotation() {
        int rotation;
        int orientation = mOrientationListener.getRememberedNormalOrientation();
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(mCameraID, info);

        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }

        return rotation;
    }


    private static class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        /**
         * @param degrees Amount of clockwise rotation from the device's natural position
         * @return Normalized degrees to just 0, 90, 180, 270
         */
        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            rememberOrientation();
            return mRememberedNormalOrientation;
        }
    }
}
