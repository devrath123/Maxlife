package com.maxlife.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.activity.MainActivity;
import com.maxlife.activity.UploadActivity;
import com.maxlife.utils.Constants;
import com.maxlife.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;
import com.toxsl.volley.Request;
import com.toxsl.volley.VolleyError;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UploadConfirmFragment extends BaseFragment {
    private static final int ONE_FIFTY_MB = 150 * (1024 * 1024);
    public ImageView editIV;
    public EditText captionET;
    public Button postBT;
    public Button cancelBT;
    int dataType;
    private ImageView profileImageIV;
    private TextView nameTV;
    private TextView dateTV;
    private EditText captionET1;
    private EditText captionET2;
    private EditText captionET3;
    private LinearLayout imagesLL;
    private TextView messageTV;
    private ImageView image1IV;
    private ImageView image2IV;
    private ImageView image3IV;
    private ImageView deleteImage1IV;
    private ImageView deleteImage2IV;
    private ImageView deleteImage3IV;
    private RelativeLayout videoFL;
    private ImageView videoThumbIV;
    private ImageView clearVideoIV;
    private ProgressDialog mProgressDialog;
    private boolean videoUploading;
    private boolean imageUploading;
    private long imageFilelength = 0;
    private TextView countTV;
    private LinearLayout captionLL1;
    private LinearLayout captionLL2;
    private LinearLayout captionLL3;
    private ProgressDialog indetminateProgressDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            dataType = bundle.getInt("type");
        }

        ((MainActivity) baseActivity).getSupportActionBar().setTitle(getString(R.string.upload));
        ((MainActivity) baseActivity).setTitle(getString(R.string.upload));
        createProgressDialog();
        createInderminateDialog();
        View view = inflater.inflate(R.layout.fragment_upload_confirm, container, false);


        countTV = (TextView) view.findViewById(R.id.countTV);
        cancelBT = (Button) view.findViewById(R.id.cancelBT);

        profileImageIV = (ImageView) view.findViewById(R.id.profileImageIV);
        nameTV = (TextView) view.findViewById(R.id.nameTV);
        dateTV = (TextView) view.findViewById(R.id.dateTV);
        editIV = (ImageView) view.findViewById(R.id.editIV);
        captionET = (EditText) view.findViewById(R.id.captionET);

        captionET1 = (EditText) view.findViewById(R.id.captionET1);
        captionET2 = (EditText) view.findViewById(R.id.captionET2);
        captionET3 = (EditText) view.findViewById(R.id.captionET3);

        imagesLL = (LinearLayout) view.findViewById(R.id.imagesLL);
        messageTV = (TextView) view.findViewById(R.id.messageTV);

        image1IV = (ImageView) view.findViewById(R.id.image1IV);
        image2IV = (ImageView) view.findViewById(R.id.image2IV);
        image3IV = (ImageView) view.findViewById(R.id.image3IV);

        deleteImage1IV = (ImageView) view.findViewById(R.id.deleteImage1IV);
        deleteImage2IV = (ImageView) view.findViewById(R.id.deleteImage2IV);
        deleteImage3IV = (ImageView) view.findViewById(R.id.deleteImage3IV);

        captionLL1 = (LinearLayout) view.findViewById(R.id.captionLL1);
        captionLL2 = (LinearLayout) view.findViewById(R.id.captionLL2);
        captionLL3 = (LinearLayout) view.findViewById(R.id.captionLL3);

        videoFL = (RelativeLayout) view.findViewById(R.id.videoFL);
        videoThumbIV = (ImageView) view.findViewById(R.id.videoThumbIV);
        clearVideoIV = (ImageView) view.findViewById(R.id.clearVideoIV);

        postBT = (Button) view.findViewById(R.id.postBT);

        postBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = false;
                if (dataType == Constants.TYPE_MESSAGE) {
                    uploadTextApi();
                } else if (dataType == Constants.TYPE_IMAGE) {
                    if (appData.uploadImagesList.size() > 0) {
                        for (int i = 0; i < appData.uploadImagesList.size(); i++) {
                            if (i == 0 && captionET1.getText().toString().isEmpty()) {
                                isValid = true;
                                baseActivity.showToast(getString(R.string.add_caption));
                            } else if (i == 1 && captionET2.getText().toString().isEmpty()) {
                                isValid = true;
                                baseActivity.showToast(getString(R.string.add_caption));
                            } else if (i == 2 && captionET3.getText().toString().isEmpty()) {
                                isValid = true;
                                baseActivity.showToast(getString(R.string.add_caption));
                            }
                        }
                        if (!isValid) {
                            uploadImagesApi();
                        }


                    } else {
                        baseActivity.showToast(baseActivity.getString(R.string.please_upload_photo));
                    }
                } else if (dataType == Constants.TYPE_VIDEO) {
                    if (appData != null) {
                        if (appData.videoToUpload != null && !appData.videoToUpload.isEmpty()) {
                            appData.uploadImagesList.clear();
                            uploadVideoApi();
                        } else {
                            baseActivity.showToast(baseActivity.getString(R.string.please_upload_video));
                        }
                    }
                }

                baseActivity.hideKeyboard();
            }
        });

        cancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        editIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (appData.uploadImagesList.size() > 0) {
                    addCaptiondata(appData.uploadImagesList.size());
                }

                Bundle bundle = getArguments();
                if (bundle != null && bundle.containsKey("type")) {
                    Intent intent = new Intent(baseActivity, UploadActivity.class);
                    if (bundle.getInt("type") == Constants.TYPE_MESSAGE) {
                        intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_MESSAGE);
                    } else if (bundle.getInt("type") == Constants.TYPE_IMAGE) {
                        intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_IMAGE);
                    } else if (bundle.getInt("type") == Constants.TYPE_VIDEO) {
                        intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_VIDEO);
                    }
                    startActivity(intent);
                }
            }
        });

        deleteImage1IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity)
                        .setMessage(R.string.do_you_want_to_delete_image)
                        .setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (appData != null) {
                                    if (appData != null && appData.uploadImagesList.size() > 0) {
                                        appData.uploadImagesList.remove(0);
                                        initialize();
                                    }
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });

        deleteImage2IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity)
                        .setMessage(R.string.do_you_want_to_delete_image)
                        .setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (appData != null) {
                                    if (appData != null && appData.uploadImagesList.size() > 1) {
                                        appData.uploadImagesList.remove(1);
                                        initialize();
                                    }
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });
        deleteImage3IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity)
                        .setMessage(R.string.do_you_want_to_delete_image)
                        .setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (appData != null) {
                                    if (appData != null && appData.uploadImagesList.size() > 2) {
                                        appData.uploadImagesList.remove(2);
                                        initialize();
                                    }
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });

        clearVideoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appData != null) {
                    appData.videoToUpload = "";
                    appData.videoToUploadBitmap = null;
                    initialize();
                }
            }
        });

        return view;
    }

    private void createProgressDialog() {
        mProgressDialog = new ProgressDialog(baseActivity);

        mProgressDialog.setMessage(getString(R.string.uploading_dot));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgress(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        initialize();
        checkApi();
    }

    private void initialize() {

        try {
            dateTV.setText(getCurrentFormattedDate().replace(".", ""));
        } catch (Exception e) {
        }

        if (appData != null && appData.profileData != null) {
            nameTV.setText(appData.profileData.full_name);
            if (appData.profileData.image_file != null && !appData.profileData.image_file.equals("")) {
                Picasso.with(baseActivity).load(appData.profileData.image_file).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(profileImageIV);
            }

            if (appData.uploadImagesList.size() == 1) {
                image2IV.setVisibility(View.GONE);
                image3IV.setVisibility(View.GONE);
                captionLL2.setVisibility(View.GONE);
                captionLL3.setVisibility(View.GONE);
            } else if (appData.uploadImagesList.size() == 2) {
                image1IV.setVisibility(View.VISIBLE);
                image2IV.setVisibility(View.VISIBLE);
                image3IV.setVisibility(View.GONE);
                captionLL1.setVisibility(View.VISIBLE);
                captionLL2.setVisibility(View.VISIBLE);
                captionLL3.setVisibility(View.GONE);
            }
        }

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("type")) {
            if (bundle.getInt("type") == Constants.TYPE_MESSAGE) {
                imagesLL.setVisibility(View.GONE);
                messageTV.setVisibility(View.VISIBLE);
                videoFL.setVisibility(View.GONE);

                messageTV.setText(bundle.getString("text"));
                countTV.setText("" + bundle.getString("text").length() + "/50000");
            } else if (bundle.getInt("type") == Constants.TYPE_IMAGE) {
                imagesLL.setVisibility(View.VISIBLE);
                messageTV.setVisibility(View.GONE);
                videoFL.setVisibility(View.GONE);

                if (appData != null) {
                    if (appData.uploadImagesList.size() == 0) {
                        Picasso.with(getActivity()).load(R.mipmap.placeholder).into(image1IV);
                    }
                    if (appData.uploadImagesList.size() > 0) {

                        Picasso.with(getActivity()).load(appData.uploadImagesList.get(0).uploadImng).placeholder(R.mipmap.placeholder).resize(400, 400).into(image1IV);
                        captionET1.setText(appData.uploadImagesList.get(0).caption);
                    }
                    if (appData.uploadImagesList.size() > 1) {

                        captionET2.setText(appData.uploadImagesList.get(1).caption);
                        Picasso.with(getActivity()).load(appData.uploadImagesList.get(1).uploadImng).placeholder(R.mipmap.placeholder).resize(200, 200).into(image2IV);
                    }
                    if (appData.uploadImagesList.size() > 2) {
                        captionET3.setText(appData.uploadImagesList.get(2).caption);
                        Picasso.with(getActivity()).load(appData.uploadImagesList.get(2).uploadImng).placeholder(R.mipmap.placeholder).resize(200, 200).into(image3IV);
                    }

                    if (appData.uploadImagesList.size() != 0) {
                        countTV.setText("" + appData.uploadImagesList.size() + "/3");
                    } else {
                        countTV.setText("0/3");
                    }

                    if (appData.uploadImagesList.size() == 0) {

                        deleteImage1IV.setVisibility(View.GONE);
                        deleteImage2IV.setVisibility(View.GONE);
                        deleteImage3IV.setVisibility(View.GONE);
                        captionLL1.setVisibility(View.GONE);
                        captionLL2.setVisibility(View.GONE);
                        captionLL3.setVisibility(View.GONE);
                    } else if (appData.uploadImagesList.size() == 1) {

                        deleteImage1IV.setVisibility(View.VISIBLE);
                        deleteImage2IV.setVisibility(View.GONE);
                        deleteImage3IV.setVisibility(View.GONE);

                        captionLL1.setVisibility(View.VISIBLE);
                        captionLL2.setVisibility(View.GONE);
                        captionLL3.setVisibility(View.GONE);
                    } else if (appData.uploadImagesList.size() == 2) {

                        deleteImage1IV.setVisibility(View.VISIBLE);
                        deleteImage2IV.setVisibility(View.VISIBLE);
                        deleteImage3IV.setVisibility(View.GONE);

                        captionLL1.setVisibility(View.VISIBLE);
                        captionLL2.setVisibility(View.VISIBLE);
                        captionLL3.setVisibility(View.GONE);
                    } else if (appData.uploadImagesList.size() == 3) {

                        deleteImage1IV.setVisibility(View.VISIBLE);
                        deleteImage2IV.setVisibility(View.VISIBLE);
                        deleteImage3IV.setVisibility(View.VISIBLE);

                        captionLL1.setVisibility(View.VISIBLE);
                        captionLL2.setVisibility(View.VISIBLE);
                        captionLL3.setVisibility(View.VISIBLE);
                    }
                }

            } else if (bundle.getInt("type") == Constants.TYPE_VIDEO) {
                imagesLL.setVisibility(View.GONE);
                messageTV.setVisibility(View.GONE);
                videoFL.setVisibility(View.VISIBLE);
                captionET.setVisibility(View.VISIBLE);

                if (appData != null) {

                    if (appData.videoToUploadBitmap == null) {
                        Bitmap bitmap = getBitmapImage(appData.videoToUpload);
                        if (bitmap != null) {
                            videoThumbIV.setImageBitmap(bitmap);
                            appData.videoToUploadBitmap = bitmap;
                        } else {
                            Picasso.with(baseActivity).load(R.mipmap.placeholder).into(videoThumbIV);
                        }
                    } else {
                        videoThumbIV.setImageBitmap(appData.videoToUploadBitmap);
                    }

                    videoThumbIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (appData != null && !appData.videoToUpload.isEmpty()) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appData.videoToUpload));
                                intent.setDataAndType(Uri.parse(appData.videoToUpload), "video/mp4*");
                                if (intent.resolveActivity(baseActivity.getPackageManager()) != null) {
                                    startActivity(intent);
                                } else {
                                    baseActivity.showToast("No application to open video");
                                }
                            } else {
                                baseActivity.showToast(baseActivity.getString(R.string.no_video));
                            }
                        }
                    });

                    if (!appData.videoToUpload.isEmpty()) {
                        countTV.setText("1/1");
                        clearVideoIV.setVisibility(View.VISIBLE);
                    } else {
                        countTV.setText("0/1");
                        clearVideoIV.setVisibility(View.GONE);
                    }
                }

            }
        }
    }

    private void uploadTextApi() {
        RequestParams params = new RequestParams();
        params.put("Post[type_id]", Constants.TYPE_MESSAGE);
        params.put("Post[content]", messageTV.getText().toString());
        baseActivity.log(prefStore.getString("plan_id"));
        if (appData.profileData.is_membership == 0) {
            syncManager.sendToServer("api/post/add", params, this);
        } else if (appData.profileData.is_membership == 1) {
            syncManager.sendToServer("api/post/add-post?id=" + prefStore.getString("plan_id"), params, this);
        }
    }

    private void uploadImagesApi() {
        showProgressDialog();
        imageUploading = true;
        RequestParams params = new RequestParams();
        params.put("Post[type_id]", Constants.TYPE_IMAGE);

        String[] imageParam = {"Post[model_file][0]", "Post[model_file][1]", "Post[model_file][2]"};
        String[] imageCaption = {"Post[caption][0]", "Post[caption][1]", "Post[caption][2]"};

        String caption = "";
        for (int i = 0; i < appData.uploadImagesList.size(); i++) {
            try {
                Bitmap compressBitmap = baseActivity.imageCompressNameCard(appData.uploadImagesList.get(i).uploadImng.getPath());
                File file = baseActivity.convertBitmapToFile(compressBitmap);
                imageFilelength = imageFilelength + file.length();
                params.put(imageParam[i], file);
                caption = getCaptionET(i).getText().toString();
                appData.uploadImagesList.get(i).caption = caption;
                params.put(imageCaption[i], caption);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        syncManager.sendToServer("api/post/add-post?id=" + prefStore.getString("plan_id"), params, this);
        postBT.setEnabled(false);

    }

    public EditText getCaptionET(int pos) {
        switch (pos) {
            case 0:
                return captionET1;
            case 1:
                return captionET2;
            case 2:
                return captionET3;
        }
        return captionET;
    }

    private void uploadVideoApi() {
        videoUploading = true;
        if (appData != null && appData.videoToUploadBitmap != null) {
            RequestParams params = new RequestParams();
            params.put("Post[type_id]", Constants.TYPE_VIDEO);
            params.put("Post[caption]", captionET.getText().toString());
            try {
                params.put("Post[thumb_file]", baseActivity.convertBitmapToFile(appData.videoToUploadBitmap));
                File videoFile = new File(appData.videoToUpload);
                params.put("Post[model_file]", videoFile);
                postBT.setEnabled(false);
                baseActivity.log(" video size: >>  " + videoFile.length());
                postBT.setEnabled(false);
                syncManager.sendToServer("api/post/add-post?id=" + prefStore.getString("plan_id"), params, this);
                showProgressDialog();

            } catch (Exception e) {
                e.printStackTrace();
                baseActivity.showToast(baseActivity.getString(R.string.video_not_uploaded_for_some_reason));
            }

        } else {
            baseActivity.showToast(baseActivity.getString(R.string.please_again_select_video_from_gallery));
        }
    }


    @Override
    public void onSyncStart() {

    }

    private void showProgressDialog() {
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgress(0);
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {

        if (controller.equalsIgnoreCase("post") && action.equalsIgnoreCase("add-post")) {
            postBT.setEnabled(true);
            if (status) {
                if (jsonObject.has("detail")) {
                    try {
                        JSONObject detailObject = jsonObject.getJSONObject("detail");
                        int type_id = detailObject.getInt("type_id");
                        if (type_id == Constants.TYPE_MESSAGE) {

                        } else if (type_id == Constants.TYPE_IMAGE) {
                            dismissDialog();
                        } else if (type_id == Constants.TYPE_VIDEO) {
                            dismissDialog();
                        }

                        if (jsonObject.has("message")) {
                            baseActivity.showToast(jsonObject.getString("message"));
                        }
                        ((MainActivity) baseActivity).loadFragment(new HomeFragment());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    if (jsonObject.has("message")) {
                        baseActivity.showToast(jsonObject.getString("message"));
                    } else {
                        baseActivity.showToast(baseActivity.getString(R.string.post_not_added));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (controller.equalsIgnoreCase("post") && action.equalsIgnoreCase("add")) {
            postBT.setEnabled(true);
            if (status) {
                if (jsonObject.has("detail")) {
                    try {
                        JSONObject detailObject = jsonObject.getJSONObject("detail");
                        int type_id = detailObject.getInt("type_id");
                        if (type_id == Constants.TYPE_MESSAGE) {

                        } else if (type_id == Constants.TYPE_IMAGE) {
                            dismissDialog();
                        } else if (type_id == Constants.TYPE_VIDEO) {
                            dismissDialog();
                        }

                        if (jsonObject.has("message")) {
                            baseActivity.showToast(jsonObject.getString("message"));
                        }
                        ((MainActivity) baseActivity).loadFragment(new HomeFragment());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    if (jsonObject.has("message")) {
                        baseActivity.showToast(jsonObject.getString("message"));
                    } else {
                        baseActivity.showToast(baseActivity.getString(R.string.post_not_added));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void dismissDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Override
    public void onSyncFinish() {
        super.onSyncFinish();
        dismissDialog();
        hideInderminateDialog();
    }

    @Override
    public void onSyncProgress(long progress, long length) {
        int p = (int) ((progress * 100) / length);
        baseActivity.log("progress   " + p);
        mProgressDialog.setProgress(p);
        if (p == 100) {
            baseActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInderminateDialog();
                }
            });
        }
    }

    private void createInderminateDialog() {
        indetminateProgressDialog = new ProgressDialog(baseActivity);
        indetminateProgressDialog.setIndeterminate(true);
        indetminateProgressDialog.setMessage("Finishing Upload...");
        indetminateProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        indetminateProgressDialog.setCancelable(false);
    }

    private void showInderminateDialog() {
        dismissDialog();
        hideInderminateDialog();
        if (!baseActivity.isFinishing() && !indetminateProgressDialog.isShowing())
            indetminateProgressDialog.show();

    }

    private void hideInderminateDialog() {
        if (indetminateProgressDialog.isShowing())
            indetminateProgressDialog.dismiss();
    }

    @Override
    public void onSyncFailure(VolleyError error, Request mRequest) {
        super.onSyncFailure(error, mRequest);
        dismissDialog();
        hideInderminateDialog();
    }


    public String getCurrentFormattedDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdfOutput = new SimpleDateFormat("d/MM/yyyy, hh:mm a", Locale.getDefault());

        return sdfOutput.format(c.getTime());
    }

    private Bitmap getBitmapImage(String imageUrl) {

        Bitmap bitmap = null;

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.Media._ID,
                MediaStore.MediaColumns.DATA};
        Cursor cursor = baseActivity.getContentResolver().query(uri, projection, null, null, null);

        while (cursor.moveToNext()) {

            long video_id = cursor.getLong(0);
            String absolutePathOfImage = cursor.getString(1);

            if (appData != null) {
                if (imageUrl.equalsIgnoreCase(absolutePathOfImage)) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    bitmap = MediaStore.Video.Thumbnails.getThumbnail(baseActivity.getContentResolver(), video_id, MediaStore.Video.Thumbnails.MINI_KIND, options);
                }
            }
        }
        return bitmap;
    }

    private void addCaptiondata(int size) {
        switch (size) {
            case 1:
                appData.uploadImagesList.get(0).caption = captionET1.getText().toString();
                break;
            case 2:
                appData.uploadImagesList.get(0).caption = captionET1.getText().toString();
                appData.uploadImagesList.get(1).caption = captionET2.getText().toString();
                break;
            case 3:
                appData.uploadImagesList.get(0).caption = captionET1.getText().toString();
                appData.uploadImagesList.get(1).caption = captionET2.getText().toString();
                appData.uploadImagesList.get(2).caption = captionET3.getText().toString();
                break;
        }
    }

}
