package com.maxlife.fragments;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.maxlife.R;
import com.maxlife.activity.BaseActivity;
import com.maxlife.activity.MainActivity;
import com.maxlife.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;
import com.toxsl.imageview.CircularImageView;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by anshul.mittal on 9/5/16.
 */
public class EditProfileFragment extends BaseFragment {

    private EditText firstNameET;
    private EditText lastNameET;
    private EditText contactNoET;
    private CircularImageView profileImageCIV;
    private ImageView bannerIV;
    private ImageView uploadImageIV;
    private Button updateBT;

    public ArrayList<String> mSelectPath = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((MainActivity) baseActivity).getSupportActionBar().setTitle(baseActivity.getString(R.string.edit_Profile));
        ((MainActivity) baseActivity).setTitle(baseActivity.getString(R.string.edit_Profile));

        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        firstNameET = (EditText) view.findViewById(R.id.firstNameET);
        lastNameET = (EditText) view.findViewById(R.id.lastNameET);
        contactNoET = (EditText) view.findViewById(R.id.contactNoET);
        uploadImageIV = (ImageView) view.findViewById(R.id.uploadImageIV);
        profileImageCIV = (CircularImageView) view.findViewById(R.id.profileImageCIV);
        bannerIV = (ImageView) view.findViewById(R.id.bannerIV);
        updateBT = (Button) view.findViewById(R.id.updateBT);

        if (appData.profileData != null) {
            firstNameET.setText(appData.profileData.first_name);
            lastNameET.setText(appData.profileData.last_name);
            contactNoET.setText(appData.profileData.contact_no);
            try {
                new ImageDownloaderTask(appData.profileData.image_file).execute();
                Picasso.with(baseActivity).load(appData.profileData.image_file).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(profileImageCIV);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        uploadImageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        updateBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkApi();
    }

    private void validateForm() {
        if (firstNameET.getText().toString().trim().equals("")) {
            baseActivity.showToast(getString(R.string.please_enter_first_name));
        } else if (lastNameET.getText().toString().trim().equals("")) {
            baseActivity.showToast(getString(R.string.please_enter_last_name));
        } else if (contactNoET.getText().toString().trim().equals("")) {
            baseActivity.showToast(getString(R.string.enter_contact_no));
        } else if (contactNoET.getText().toString().trim().length() < 10) {
            baseActivity.showToast(getString(R.string.enter_min_10));
        } else if (!baseActivity.isValidMobile(contactNoET.getText().toString().trim())) {
            baseActivity.showToast(getString(R.string.contact_number_is_not_valid));
        } else {
            updateProfileApi();
        }
    }

    private void updateProfileApi() {
        RequestParams params = new RequestParams();
        params.put("User[first_name]", firstNameET.getText().toString().trim());
        params.put("User[last_name]", lastNameET.getText().toString().trim());
        params.put("User[contact_no]", contactNoET.getText().toString());
        if (mSelectPath.size() > 0 && !mSelectPath.get(0).equals("")) {
            try {
                File filee;
                Bitmap selectedImage = baseActivity.imageCompressNameCard(mSelectPath.get(0));
                if (selectedImage != null) {
                    filee = new File(baseActivity.getCacheDir(), +System.currentTimeMillis() + ".jpg");
                    FileOutputStream out;
                    try {
                        out = new FileOutputStream(filee);
                        selectedImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                    params.put("User[image_file]", filee);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (baseActivity.checkBeforeApi()) {
            syncManager.sendToServer("api/user/update", params, this);
            baseActivity.startDialog();
        }
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("update")) {
            if (status) {
                if (jsonObject.has("detail")) {
                    try {
                        appData.profileData = baseActivity.getProfileData(jsonObject.getJSONObject("detail"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                baseActivity.showToast(baseActivity.getString(R.string.profile_successfully_updated));
                ((MainActivity) baseActivity).getSupportFragmentManager().popBackStack();
                try {
                    if (appData.profileData.image_file != null && !appData.profileData.image_file.isEmpty()) {
                        Picasso.with(baseActivity).load(appData.profileData.image_file).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(((MainActivity) baseActivity).drawerProfileImageIV);
                    }
                    ((MainActivity) baseActivity).userNameTV.setText(appData.profileData.full_name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (jsonObject.has("error")) {
                        baseActivity.showToast(jsonObject.getString("error"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void openGallery() {
        if (baseActivity.checkPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})) {
            imageIntent();
        }
    }

    private void imageIntent() {
        Intent intent = new Intent(getActivity(), MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
                MultiImageSelectorActivity.MODE_SINGLE);

        intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == baseActivity.RESULT_OK) {
                mSelectPath.clear();
                mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                try {
                    if (mSelectPath.size() > 0 && mSelectPath.get(0) != null && !mSelectPath.get(0).equals("")) {
                        if (baseActivity.isImageValid(mSelectPath.get(0))) {
                            Picasso.with(baseActivity).load(new File(mSelectPath.get(0))).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(profileImageCIV);

                            Bitmap blurredBitmap;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                blurredBitmap = BaseActivity.BlurBuilder.blur(baseActivity, baseActivity.imageCompressNameCard(mSelectPath.get(0)));
                            } else {
                                blurredBitmap = baseActivity.fastblur(baseActivity.imageCompressNameCard(mSelectPath.get(0)), 0.4f, 2);
                            }
                            bannerIV.setImageBitmap(blurredBitmap);

                        }
                    } else {
                        baseActivity.showToast(getString(R.string.try_again));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class ImageDownloaderTask extends AsyncTask<Void, Void, Bitmap> {
        String imageFile;

        public ImageDownloaderTask(String url) {
            imageFile = url;

        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return baseActivity.downloadBitmap(imageFile);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (bitmap != null) {
                if (isAdded()) {
                    Bitmap blurredBitmap;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        blurredBitmap = BaseActivity.BlurBuilder.blur(baseActivity, bitmap);
                    } else {
                        blurredBitmap = baseActivity.fastblur(bitmap, 0.4f, 2);
                    }
                    bannerIV.setImageBitmap(blurredBitmap);
                }
            }
        }
    }
}
