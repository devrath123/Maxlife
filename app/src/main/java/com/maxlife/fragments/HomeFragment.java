package com.maxlife.fragments;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.activity.BaseActivity;
import com.maxlife.activity.MainActivity;
import com.maxlife.activity.UploadActivity;
import com.maxlife.adapters.PostsAdapter;
import com.maxlife.data.PostData;
import com.maxlife.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends BaseFragment implements BaseActivity.PermCallback {

    private PostsAdapter postsAdapter;
    private ArrayList<PostData> postDatas = new ArrayList<>();

    private ListView postLV;
    private TextView errorTV;

    private CardView uploadVideoRL;
    private CardView uploadImageRL;
    private CardView uploadMessageRL;

    private int whichGalleryClicked;

    View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPostApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((MainActivity) baseActivity).getSupportActionBar().setTitle(R.string.app_name);
        getPostApi();
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, container, false);

            postLV = (ListView) view.findViewById(R.id.postLV);
            errorTV = (TextView) view.findViewById(R.id.errorTV);

            uploadVideoRL = (CardView) view.findViewById(R.id.uploadVideoRL);
            uploadImageRL = (CardView) view.findViewById(R.id.uploadImageRL);
            uploadMessageRL = (CardView) view.findViewById(R.id.uploadMessageRL);

            baseActivity.setPermCallback(this);
            uploadMessageRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (baseActivity.checkPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                        openGallery(Constants.TAB_TYPE_MESSAGE);

                    } else {
                        whichGalleryClicked = Constants.TAB_TYPE_MESSAGE;
                    }
                }
            });

            uploadVideoRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (baseActivity.checkPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                        openGallery(Constants.TAB_TYPE_VIDEO);
                    } else {
                        whichGalleryClicked = Constants.TAB_TYPE_VIDEO;
                    }
                }
            });

            uploadImageRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (baseActivity.checkPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                        openGallery(Constants.TAB_TYPE_IMAGE);
                    } else {
                        whichGalleryClicked = Constants.TAB_TYPE_IMAGE;
                    }
                }
            });

        }
        return view;
    }


    private void openGallery(int tab_type) {
        Intent intent = new Intent(baseActivity, UploadActivity.class);

        if (tab_type == Constants.TAB_TYPE_IMAGE) {
            intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_IMAGE);
        } else if (tab_type == Constants.TAB_TYPE_VIDEO) {
            intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_VIDEO);
        } else if (tab_type == Constants.TAB_TYPE_MESSAGE) {
            intent.putExtra(Constants.TAB_TYPE, Constants.TAB_TYPE_MESSAGE);
        }
        startActivity(intent);
        if (appData != null) {
            appData.mode = "";
            appData.uploadImagesList.clear();
            appData.videoToUpload = "";
            appData.videoToUploadBitmap = null;
        }
    }

    private void initialize() {
        if (appData.profileData!=null && appData.profileData.is_membership == 0) {
            uploadImageRL.setVisibility(View.INVISIBLE);
            uploadVideoRL.setVisibility(View.INVISIBLE);
            if (baseActivity.isMessagePost) {
                uploadMessageRL.setVisibility(View.GONE);
            } else {
                uploadMessageRL.setVisibility(View.VISIBLE);
            }
        } else {
            if (baseActivity.isMessagePost) {
                uploadMessageRL.setVisibility(View.GONE);
            } else {
                uploadMessageRL.setVisibility(View.VISIBLE);
            }
            if (baseActivity.isImagePost) {
                uploadImageRL.setVisibility(View.GONE);
            } else {
                uploadImageRL.setVisibility(View.VISIBLE);
            }
            if (baseActivity.isVideoPost) {
                uploadVideoRL.setVisibility(View.GONE);
            } else {
                uploadVideoRL.setVisibility(View.VISIBLE);
            }
        }


        if (baseActivity.postCount == 0) {
            postLV.setVisibility(View.GONE);
        } else if (baseActivity.postCount == 2) {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 2.0f);
            postLV.setLayoutParams(param);
        } else if (baseActivity.postCount == 3) {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 3.0f);
            postLV.setLayoutParams(param);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        checkApi();

    }

    public void getPostApi() {
        if (appData != null && appData.profileData.is_membership == 1) {
            syncManager.sendToServer("api/post/index?id=" + prefStore.getString("plan_id"), null, this);
        } else {
            syncManager.sendToServer("api/post/index?id=0", null, this);
        }
        baseActivity.startDialog();
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("post") && action.equalsIgnoreCase("index")) {
            if (status) {
                postDatas.clear();
                baseActivity.postCount = 0;
                errorTV.setText("");
                errorTV.setVisibility(View.GONE);
                try {
                    if (jsonObject.has("posts")) {
                        JSONArray postArrayObject = jsonObject.getJSONArray("posts");
                        for (int i = 0; i < postArrayObject.length(); i++) {
                            if (postArrayObject.getJSONObject(i).has("type_id")) {
                                int type_id = postArrayObject.getJSONObject(i).getInt("type_id");
                                if (type_id == Constants.TYPE_MESSAGE) {
                                    baseActivity.isMessagePost = true;
                                    baseActivity.postCount++;
                                } else if (type_id == Constants.TYPE_VIDEO) {
                                    baseActivity.isVideoPost = true;
                                    baseActivity.postCount++;
                                } else if (type_id == Constants.TYPE_IMAGE) {
                                    baseActivity.isImagePost = true;
                                    baseActivity.postCount++;
                                }
                            }
                            postDatas.add(baseActivity.getPostData(postArrayObject.getJSONObject(i)));
                        }
                        initialize();
                        postsAdapter = new PostsAdapter(baseActivity, 0, postDatas);
                        postLV.setAdapter(postsAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    errorTV.setVisibility(View.VISIBLE);
                    initialize();
                    if (jsonObject.has("error")) {
                        errorTV.setText(jsonObject.getString("error"));
                    } else {
                        errorTV.setText(baseActivity.getString(R.string.no_post_found));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void permGranted() {
        openGallery(whichGalleryClicked);
        whichGalleryClicked = 0;
    }

    @Override
    public void permDenied() {

    }
}
