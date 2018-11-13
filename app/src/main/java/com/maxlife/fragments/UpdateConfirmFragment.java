package com.maxlife.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.activity.BaseActivity;
import com.maxlife.activity.MainActivity;
import com.maxlife.activity.UploadActivity;
import com.maxlife.data.PostData;
import com.maxlife.data.UploadImgData;
import com.maxlife.service.DownloadService;
import com.maxlife.utils.Constants;
import com.maxlife.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;
import com.toxsl.volley.Request;
import com.toxsl.volley.VolleyError;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by anshul.mittal on 11/5/16.
 */
public class UpdateConfirmFragment extends BaseFragment implements BaseActivity.PermCallback {

    private static final int ONE_FIFTY_MB = 150 * (1024 * 1024);
    private final int PERMISSION_TASK_OPEN_GALLERY = 1;
    private final int PERMISSION_TASK_DOWNLOAD_VIDEO = 2;
    public EditText captionVTV;
    List<EditText> editTextList;
    PowerManager.WakeLock wakeLock;
    private ImageView profileImageIV;
    private TextView nameTV;
    private TextView dateTV;
    private ImageView editIV;
    private LinearLayout imagesLL;
    private EditText messageET;
    private ImageView image1IV;
    private ImageView image2IV;
    private ImageView image3IV;
    private ImageView deleteImage1IV;
    private ImageView deleteImage2IV;
    private ImageView deleteImage3IV;
    private RelativeLayout videoFL;
    private ImageView videoThumbIV;
    private ImageView clearVideoIV;
    private EditText caption1TV;
    private EditText caption2TV;
    private EditText caption3TV;
    private Button postBT;
    private PostData postData;
    private ArrayList<Integer> deleteImageList = new ArrayList<>();
    private ProgressDialog mProgressDialog;
    private boolean videoUploading;
    private boolean imageUploading;
    private long imageFilelength;
    private Button cancelBT;
    private TextView countTV;
    private ImageView clearMsgIV;
    private int permissionTask;
    private boolean isUploadApi;
    private ProgressDialog indetminateProgressDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("postData")) {
            postData = bundle.getParcelable("postData");
        }
        baseActivity.setPermCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) baseActivity).getSupportActionBar().setTitle(baseActivity.getString(R.string.edit_post));
        ((MainActivity) baseActivity).setTitle(baseActivity.getString(R.string.edit_post));
        View view = inflater.inflate(R.layout.fragment_update_confirm, container, false);
        baseActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        baseActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        baseActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        PowerManager powerManager = (PowerManager) baseActivity.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        createProgressDialog();
        createInderminateDialog();
        initUI(view);

        postBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (postData != null && appData != null) {

                    if (postData.type_id == Constants.TYPE_MESSAGE) {
                        updateTextApi(postData.id);
                    } else if (postData.type_id == Constants.TYPE_IMAGE) {
                        if (appData.uploadImagesList.size() > 0) {
                            for (int i = 0; i < postData.image_file_list.size(); i++) {
                                if (appData.uploadImagesList.size() > 0) {
                                    appData.uploadImagesList.remove(0);
                                }
                            }
                            if (deleteImageList.size() > 0) {
                                deleteImageApi(deleteImageList.get(0));
                            }
                            if (appData.uploadImagesList.size() >= 0 || postData.image_file_list.size() != 0) {
                                baseActivity.log(" 2nd case");
                                uploadImageFileApi(postData.id);
                            } else {
                                baseActivity.showToast(baseActivity.getString(R.string.your_post_is_saved_successfully));
                                ((MainActivity) baseActivity).loadFragment(new HomeFragment());
                            }
                        } else {
                            baseActivity.showToast(baseActivity.getString(R.string.please_upload_photo));
                        }
                    } else if (postData.type_id == Constants.TYPE_VIDEO) {

                        if (!appData.videoToUpload.contains("http") && !appData.videoToUpload.isEmpty()) {
                            updateVideoFileApi(postData.id);
                        } else if (postData.videoData == null && appData.videoToUpload.isEmpty()) {
                            baseActivity.showToast(baseActivity.getString(R.string.please_upload_video));
                        } else if (captionVTV.getText().length() > 0) {
                            updateVideoFileApi(postData.id);
                        } else {
                            baseActivity.showToast(getString(R.string.no_changes_made));
                            ((MainActivity) baseActivity).loadFragment(new HomeFragment());
                        }
                    }
                }

                baseActivity.hideKeyboard();
            }
        });

        editIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appData != null) {
                    if (baseActivity.checkPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                        if (appData.uploadImagesList.size() > 0) {
                            addCaptiondata(appData.uploadImagesList.size());
                        }
                        goToGalleryActivity();
                    } else {
                        permissionTask = PERMISSION_TASK_OPEN_GALLERY;
                        baseActivity.showToast(baseActivity.getString(R.string.something_gets_wrong_please_go_to_home_screen_and_then_try_again));
                    }
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
                                if (postData != null) {
                                    if (postData.image_file_list.size() > 0) {
                                        deleteImageList.add(postData.image_file_list.get(0).id);
                                        postData.image_file_list.remove(0);
                                    }
                                    if (appData != null && appData.uploadImagesList.size() > 0) {
                                        appData.uploadImagesList.remove(0);
                                        caption1TV.setText("");

                                    }
                                    initialize();
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
                                if (postData != null) {
                                    if (postData.image_file_list.size() > 1) {
                                        deleteImageList.add(postData.image_file_list.get(1).id);
                                        postData.image_file_list.remove(1);
                                    }
                                    if (appData != null && appData.uploadImagesList.size() > 1) {
                                        appData.uploadImagesList.remove(1);
                                        caption2TV.setText("");
                                    }
                                    initialize();
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
                                if (postData != null) {
                                    if (postData.image_file_list.size() > 2) {
                                        deleteImageList.add(postData.image_file_list.get(2).id);
                                        postData.image_file_list.remove(2);
                                    }
                                    if (appData != null && appData.uploadImagesList.size() > 2) {
                                        appData.uploadImagesList.remove(2);

                                        caption3TV.setText("");
                                    }
                                    initialize();
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

        return view;
    }

    private void initUI(View view) {
        editTextList = new ArrayList<>();
        clearMsgIV = (ImageView) view.findViewById(R.id.clearMsgIV);
        countTV = (TextView) view.findViewById(R.id.countTV);
        cancelBT = (Button) view.findViewById(R.id.cancelBT);
        profileImageIV = (ImageView) view.findViewById(R.id.profileImageIV);
        nameTV = (TextView) view.findViewById(R.id.nameTV);
        dateTV = (TextView) view.findViewById(R.id.dateTV);
        editIV = (ImageView) view.findViewById(R.id.editIV);

        imagesLL = (LinearLayout) view.findViewById(R.id.imagesLL);
        messageET = (EditText) view.findViewById(R.id.messageET);

        caption1TV = (EditText) view.findViewById(R.id.caption1TV);
        caption2TV = (EditText) view.findViewById(R.id.caption2TV);
        caption3TV = (EditText) view.findViewById(R.id.caption3TV);

        editTextList.add(caption1TV);
        editTextList.add(caption1TV);
        editTextList.add(caption1TV);
        captionVTV = (EditText) view.findViewById(R.id.captionVTV);

        image1IV = (ImageView) view.findViewById(R.id.image1IV);
        image2IV = (ImageView) view.findViewById(R.id.image2IV);
        image3IV = (ImageView) view.findViewById(R.id.image3IV);

        deleteImage1IV = (ImageView) view.findViewById(R.id.deleteImage1IV);
        deleteImage2IV = (ImageView) view.findViewById(R.id.deleteImage2IV);
        deleteImage3IV = (ImageView) view.findViewById(R.id.deleteImage3IV);

        videoFL = (RelativeLayout) view.findViewById(R.id.videoFL);
        videoThumbIV = (ImageView) view.findViewById(R.id.videoThumbIV);
        clearVideoIV = (ImageView) view.findViewById(R.id.clearVideoIV);

        postBT = (Button) view.findViewById(R.id.postBT);
        cancelBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                baseActivity.hideKeyboard();
            }
        });
        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {

                countTV.setText("" + messageET.getText().length() + "/50000");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        clearMsgIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageET.setText("");
            }
        });

        clearVideoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appData != null) {
                    postData.videoData = null;
                    appData.videoToUpload = "";
                    appData.videoToUploadBitmap = null;
                }
                initialize();
            }
        });

        videoThumbIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (appData != null && appData.videoToUpload != null && !appData.videoToUpload.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(new File(appData.videoToUpload)));
                    intent.setDataAndType(Uri.fromFile(new File(appData.videoToUpload)), "video/*");
                    baseActivity.startActivity(intent);
                } else if (postData != null && postData.videoData != null && !postData.videoData.video_file.isEmpty()) {

                    File dir = new File(Environment.getExternalStorageDirectory(), "MaxLife");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, "" + postData.videoData.video_name);
                    if (file.exists()) {
                        postData.videoData.video_file = file.getAbsolutePath();

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(postData.videoData.video_file));
                        intent.setDataAndType(Uri.parse(postData.videoData.video_file), "video/*");
                        baseActivity.startActivity(intent);

                    } else {
                        if (baseActivity.checkPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                            downloadVideo();
                        } else {
                            permissionTask = PERMISSION_TASK_DOWNLOAD_VIDEO;
                        }
                    }
                }

            }
        });
    }

    private void downloadVideo() {
        if (postData != null && postData.videoData != null) {
            showDownloadingProgressDialog();
            Intent intent = new Intent(baseActivity, DownloadService.class);
            intent.putExtra("url", postData.videoData.video_file);
            intent.putExtra("video_name", postData.videoData.video_name);
            intent.putExtra("receiver", new DownloadReceiver(new Handler()));
            baseActivity.startService(intent);
        } else {
            baseActivity.showToast(getString(R.string.no_video));
        }
    }

    private void goToGalleryActivity() {
        appData.mode = "edit";

        Intent intent = new Intent(baseActivity, UploadActivity.class);
        if (postData != null) {
            if (postData.type_id == Constants.TYPE_IMAGE) {
                intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_IMAGE);
            } else if (postData.type_id == Constants.TYPE_VIDEO) {
                intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_VIDEO);
            } else if (postData.type_id == Constants.TYPE_MESSAGE) {
                intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_MESSAGE);
            }
        }
        startActivity(intent);
    }

    private void createProgressDialog() {
        mProgressDialog = new ProgressDialog(baseActivity);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgress(0);
    }

    @Override
    public void onSyncFailure(VolleyError error, Request mRequest) {
        super.onSyncFailure(error, mRequest);
        postBT.setEnabled(true);
        if (videoUploading || imageUploading) {
            videoUploading = false;
            imageUploading = false;
            dismissDialog();
            hideInderminateDialog();
            hideInderminateDialog();
            baseActivity.showToast("Please try again");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        setHasOptionsMenu(true);
        initialize();
        checkApi();
    }

    private void initialize() {

        if (appData != null && appData.profileData != null) {
            nameTV.setText(appData.profileData.full_name);
            if (appData.profileData.image_file != null && !appData.profileData.image_file.equals("")) {
                Picasso.with(baseActivity).load(appData.profileData.image_file).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(profileImageIV);
            }
            if (postData != null) {
                try {
                    dateTV.setText(baseActivity.getDateFormatted(postData.create_time).replace(".", ""));
                } catch (Exception ignored) {
                }
            }
        }

        if (postData != null && appData != null) {
            appData.modePostType = postData.type_id;

            if (postData.type_id == Constants.TYPE_MESSAGE) {

                imagesLL.setVisibility(View.GONE);
                messageET.setVisibility(View.VISIBLE);
                videoFL.setVisibility(View.GONE);
                editIV.setVisibility(View.GONE);

                clearMsgIV.setVisibility(View.VISIBLE);
                countTV.setText("" + postData.content.length() + "/50000");
                captionVTV.setVisibility(View.GONE);
                messageET.setText(postData.content);

            } else if (postData.type_id == Constants.TYPE_IMAGE) {

                imagesLL.setVisibility(View.VISIBLE);
                messageET.setVisibility(View.GONE);
                videoFL.setVisibility(View.GONE);

                if (appData.uploadImagesList.size() == 0) {

                    for (int i = 0; i < postData.image_file_list.size(); i++) {
                        UploadImgData data = new UploadImgData();
                        data.uploadImng = Uri.parse(postData.image_file_list.get(i).image_file);
                        data.caption = postData.image_file_list.get(i).caption;

                        appData.uploadImagesList.add(data);

                    }

                }

                if (appData.uploadImagesList.size() != 0) {
                    countTV.setText("" + appData.uploadImagesList.size() + "/3");
                } else {
                    countTV.setText("0/3");
                }

                if (appData.uploadImagesList.size() == 0) {
                    Picasso.with(getActivity()).load(R.mipmap.placeholder).into(image1IV);
                    Picasso.with(getActivity()).load(R.mipmap.placeholder).into(image2IV);
                    Picasso.with(getActivity()).load(R.mipmap.placeholder).into(image3IV);

                } else if (appData.uploadImagesList.size() == 1) {

                    caption1TV.setVisibility(View.VISIBLE);
                    caption1TV.setText(appData.uploadImagesList.get(0).caption);
                    Picasso.with(getActivity()).load(appData.uploadImagesList.get(0).uploadImng).placeholder(R.mipmap.placeholder).resize(200, 200).centerCrop().into(image1IV);
                    Picasso.with(getActivity()).load(R.mipmap.placeholder).into(image2IV);
                    Picasso.with(getActivity()).load(R.mipmap.placeholder).into(image3IV);
                } else if (appData.uploadImagesList.size() == 2) {

                    caption1TV.setText(appData.uploadImagesList.get(0).caption);
                    caption2TV.setText(appData.uploadImagesList.get(1).caption);
                    Picasso.with(getActivity()).load(appData.uploadImagesList.get(0).uploadImng).placeholder(R.mipmap.placeholder).resize(200, 200).centerCrop().into(image1IV);
                    Picasso.with(getActivity()).load(appData.uploadImagesList.get(1).uploadImng).placeholder(R.mipmap.placeholder).resize(200, 200).centerCrop().into(image2IV);
                    Picasso.with(getActivity()).load(R.mipmap.placeholder).into(image3IV);
                } else if (appData.uploadImagesList.size() == 3) {

                    caption1TV.setText(appData.uploadImagesList.get(0).caption);
                    caption2TV.setText(appData.uploadImagesList.get(1).caption);
                    caption3TV.setText(appData.uploadImagesList.get(2).caption);
                    Picasso.with(getActivity()).load(appData.uploadImagesList.get(0).uploadImng).placeholder(R.mipmap.placeholder).resize(200, 200).centerCrop().into(image1IV);

                    Picasso.with(getActivity()).load(appData.uploadImagesList.get(1).uploadImng).placeholder(R.mipmap.placeholder).resize(200, 200).centerCrop().into(image2IV);

                    Picasso.with(getActivity()).load(appData.uploadImagesList.get(2).uploadImng).placeholder(R.mipmap.placeholder).resize(200, 200).centerCrop().into(image3IV);


                }

                if (appData.uploadImagesList.size() == 0) {

                    deleteImage1IV.setVisibility(View.GONE);
                    deleteImage2IV.setVisibility(View.GONE);
                    deleteImage3IV.setVisibility(View.GONE);
                    caption1TV.setVisibility(View.GONE);
                    caption2TV.setVisibility(View.GONE);
                    caption3TV.setVisibility(View.GONE);
                }

                if (appData.uploadImagesList.size() == 1) {

                    deleteImage1IV.setVisibility(View.VISIBLE);
                    deleteImage2IV.setVisibility(View.GONE);
                    deleteImage3IV.setVisibility(View.GONE);

                    caption1TV.setVisibility(View.VISIBLE);
                    caption2TV.setVisibility(View.GONE);
                    caption3TV.setVisibility(View.GONE);
                } else if (appData.uploadImagesList.size() == 2) {

                    deleteImage1IV.setVisibility(View.VISIBLE);
                    deleteImage2IV.setVisibility(View.VISIBLE);
                    deleteImage3IV.setVisibility(View.GONE);

                    caption1TV.setVisibility(View.VISIBLE);
                    caption2TV.setVisibility(View.VISIBLE);
                    caption3TV.setVisibility(View.GONE);
                } else if (appData.uploadImagesList.size() == 3) {

                    deleteImage1IV.setVisibility(View.VISIBLE);
                    deleteImage2IV.setVisibility(View.VISIBLE);
                    deleteImage3IV.setVisibility(View.VISIBLE);

                    caption1TV.setVisibility(View.VISIBLE);
                    caption2TV.setVisibility(View.VISIBLE);
                    caption3TV.setVisibility(View.VISIBLE);
                }

            } else if (postData.type_id == Constants.TYPE_VIDEO) {

                imagesLL.setVisibility(View.GONE);
                messageET.setVisibility(View.GONE);
                videoFL.setVisibility(View.VISIBLE);
                captionVTV.setVisibility(View.VISIBLE);


                if (!appData.videoToUpload.isEmpty()) {
                    postData.videoData = null;
                }

                if (postData.videoData != null) {
                    captionVTV.setText(postData.videoData.caption);
                    if (postData.videoData.video_file != null) {
                        countTV.setText("1/1");
                    }
                    clearVideoIV.setVisibility(View.VISIBLE);
                } else if (!appData.videoToUpload.isEmpty()) {
                    countTV.setText("1/1");
                    clearVideoIV.setVisibility(View.VISIBLE);
                } else {
                    countTV.setText("0/1");
                    clearVideoIV.setVisibility(View.GONE);
                }

                if (!appData.videoToUpload.isEmpty()) {
                    postData.videoData = null;
                }
                if (appData.videoToUploadBitmap != null) {
                    videoThumbIV.setImageBitmap(appData.videoToUploadBitmap);


                } else if (postData.videoData != null && postData.videoData.video_thumbnail != null && !postData.videoData.video_thumbnail.isEmpty()) {
                    Picasso.with(baseActivity).load(postData.videoData.video_thumbnail).placeholder(R.mipmap.placeholder).resize(400, 400).into(videoThumbIV);
                } else if (appData.videoToUploadBitmap != null) {
                    videoThumbIV.setImageBitmap(appData.videoToUploadBitmap);
                } else {
                    Picasso.with(baseActivity).load(R.mipmap.placeholder).into(videoThumbIV);
                }
            }
        }
    }

    private void updateTextApi(int post_id) {
        if (messageET.getText().toString().trim().equals("")) {
            baseActivity.showToast(baseActivity.getString(R.string.please_enter_message_to_post));
        } else if (messageET.getText().toString().length() > 50000 && appData.profileData.is_membership == 1) {
            baseActivity.showToast(baseActivity.getString(R.string.maximum_50000_characters_allowed));
        } else if (messageET.getText().toString().length() > 10000 && appData.profileData.is_membership == 0) {
            baseActivity.showToast(baseActivity.getString(R.string.maximum_10000_characters_allowed));
        } else {
            RequestParams params = new RequestParams();
            params.put("Post[content]", messageET.getText().toString());
            syncManager.sendToServer("api/post/update?id=" + post_id, params, this);
        }
    }

    private void deleteImageApi(int image_id) {
        syncManager.sendToServer("api/post/delimage?id=" + image_id, null, this);
        baseActivity.startDialog();
    }

    private void uploadImageFileApi(int postId) {
        imageUploading = true;
        RequestParams params = new RequestParams();

        String[] imageParam = {"Media[model_file][0]", "Media[model_file][1]", "Media[model_file][2]"};
        String[] imageCaption = {"Post[caption][0]", "Post[caption][1]", "Post[caption][2]"};
        String[] postID = {"Post[media_id][0]", "Post[media_id][1]", "Post[media_id][2]"};

        String caption = "";


        postData.image_file_list.size();
        if (appData.uploadImagesList.size() > 0) {
            for (int i = 0; i < appData.uploadImagesList.size(); i++) {
                try {
                    Bitmap compressBitmap = baseActivity.imageCompressNameCard(appData.uploadImagesList.get(i).uploadImng.getPath());
                    File file = baseActivity.convertBitmapToFile(compressBitmap);
                    imageFilelength = imageFilelength + file.length();

                    int postion = i + postData.image_file_list.size();
                    caption = getCaptionET(postion).getText().toString();
                    params.put(imageParam[postion], file);
                    params.put(imageCaption[postion], caption);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            if (!caption1TV.getText().toString().isEmpty()) {
                params.put(imageCaption[0], caption1TV.getText());
            }
            if (!caption2TV.getText().toString().isEmpty()) {
                params.put(imageCaption[1], caption2TV.getText());
            }
            if (!caption3TV.getText().toString().isEmpty()) {
                params.put(imageCaption[2], caption3TV.getText());
            }

            if (postData.image_file_list.size() > 0) {
                for (int i = 0; i < postData.image_file_list.size(); i++) {
                    params.put(postID[i], postData.image_file_list.get(i).id);

                }
            }
        }

        postBT.setEnabled(false);
        syncManager.sendToServer("api/post/update?id=" + postId, params, this);
        baseActivity.startDialog();

    }


    public EditText getCaptionET(int pos) {
        switch (pos) {
            case 0:
                return caption1TV;
            case 1:
                return caption2TV;
            case 2:
                return caption3TV;
        }
        return captionVTV;
    }

    @Override
    public void onSyncStart() {

    }

    private void updateVideoFileApi(int postId) {
        RequestParams params = new RequestParams();
        if (appData.videoToUploadBitmap != null) {
            try {
                params.put("Media[thumb_file]", baseActivity.convertBitmapToFile(appData.videoToUploadBitmap));
                File videoFile = new File(appData.videoToUpload);
                params.put("Media[model_file]", new File(appData.videoToUpload));
                params.put("Post[caption]", captionVTV.getText().toString());
                baseActivity.log(" video size: >>  " + videoFile.length());
                postBT.setEnabled(false);
                syncManager.sendToServer("api/post/update?id=" + postId, params, this);
                showProgressDialog();
                videoUploading = true;

            } catch (Exception e) {
                e.printStackTrace();
                baseActivity.showToast(baseActivity.getString(R.string.error));
            }

        } else if (postData.videoData != null) {
            postBT.setEnabled(false);
            params.put("Post[caption]", captionVTV.getText().toString());
            syncManager.sendToServer("api/post/update?id=" + postId, params, this);
            isUploadApi = true;
            showProgressDialog();
        } else {
            baseActivity.showToast(baseActivity.getString(R.string.please_select_video_from_gallery_again));
        }
    }

    private void showProgressDialog() {
        mProgressDialog.setMessage(baseActivity.getString(R.string.uploading_dot));
        mProgressDialog.show();
        mProgressDialog.setProgress(0);
    }

    private void showDownloadingProgressDialog() {
        mProgressDialog.setMessage(baseActivity.getString(R.string.downloading_dot));
        mProgressDialog.show();
        mProgressDialog.setProgress(0);
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        if (controller.equalsIgnoreCase("post") && action.equalsIgnoreCase("update")) {
            postBT.setEnabled(true);
            if (status) {
                if (jsonObject.has("detail")) {
                    try {
                        JSONObject detailObject = jsonObject.getJSONObject("detail");
                        int type_id = detailObject.getInt("type_id");

                        if (jsonObject.has("message")) {
                            baseActivity.showToast(jsonObject.getString("message"));
                        }
                        ((MainActivity) baseActivity).loadFragment(new HomeFragment());

                        if (type_id == Constants.TYPE_MESSAGE) {
                        } else if (type_id == Constants.TYPE_IMAGE) {
                            dismissDialog();
                        } else if (type_id == Constants.TYPE_VIDEO) {
                            dismissDialog();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String error = "Unexpected error occured !";
                try {
                    if (jsonObject.has("error")) {

                        error = jsonObject.getString("error");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                baseActivity.showToast(error);
            }
        } else if (controller.equalsIgnoreCase("post") && action.equalsIgnoreCase("delimage")) {
            if (status) {
                try {
                    if (deleteImageList.size() > 0) {
                        deleteImageList.remove(0);
                    }
                    if (deleteImageList.size() > 0) {
                        deleteImageApi(deleteImageList.get(0));
                    } else {
                        if (appData != null && postData != null || appData.uploadImagesList.size() > 0) {

                        } else {
                            dismissDialog();
                            baseActivity.showToast(getString(R.string.your_post_is_saved_successfully));
                            ((MainActivity) baseActivity).loadFragment(new HomeFragment());
                        }
                    }
                    if (appData != null && appData.uploadImagesList.size() == 0) {
                        dismissDialog();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String error = "Unexpected error occured !";
                try {
                    if (jsonObject.has("error")) {

                        error = jsonObject.getString("error");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                baseActivity.showToast(error);
            }
        }
    }

    private void dismissDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
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
    public void onSyncFinish() {
        super.onSyncFinish();
        dismissDialog();
        hideInderminateDialog();
    }

    @Override
    public void permGranted() {
        if (permissionTask == PERMISSION_TASK_OPEN_GALLERY) {
            goToGalleryActivity();
        } else if (permissionTask == PERMISSION_TASK_DOWNLOAD_VIDEO) {
            downloadVideo();
        }
        permissionTask = 0;
    }


    @Override
    public void permDenied() {
        baseActivity.showToast(getString(R.string.permissions_denied));
    }

    private void addCaptiondata(int size) {
        switch (size) {
            case 1:
                appData.uploadImagesList.get(0).caption = caption1TV.getText().toString();
                break;
            case 2:
                appData.uploadImagesList.get(0).caption = caption1TV.getText().toString();
                appData.uploadImagesList.get(1).caption = caption2TV.getText().toString();
                break;
            case 3:
                appData.uploadImagesList.get(0).caption = caption1TV.getText().toString();
                appData.uploadImagesList.get(1).caption = caption2TV.getText().toString();
                appData.uploadImagesList.get(2).caption = caption3TV.getText().toString();
                break;
        }
    }

    @SuppressLint("ParcelCreator")
    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                int progress = resultData.getInt("progress");
                mProgressDialog.setProgress(progress);
                if (progress == 100) {
                    mProgressDialog.dismiss();
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resultData.getString("file_path")));
                        intent.setDataAndType(Uri.parse(resultData.getString("file_path")), "video/mp4");
                        baseActivity.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
