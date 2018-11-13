package com.maxlife.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.maxlife.R;
import com.maxlife.activity.MainActivity;
import com.maxlife.adapters.GalleryAdapter;
import com.maxlife.data.GalleryData;
import com.maxlife.data.ImageGalleryData;
import com.maxlife.data.VideoGalleryData;
import com.maxlife.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;

public class UploadGalleryFragment extends BaseFragment {

    private int tab_type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_gallery, container, false);

        GridView grid_view = (GridView) view.findViewById(R.id.grid_view);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(Constants.TAB_TYPE)) {
            tab_type = bundle.getInt(Constants.TAB_TYPE);
        }
        grid_view.setAdapter(new GalleryAdapter(baseActivity, getImageVideoDataList()));
        return view;
    }

    private ArrayList<GalleryData> getImageVideoDataList() {
        ArrayList<GalleryData> imageList = new ArrayList<>();
        ArrayList<GalleryData> videoList = new ArrayList<>();
        if (tab_type == Constants.TAB_TYPE_IMAGE) {
            imageList = getAllShownImagesPath(baseActivity);
        } else if (tab_type == Constants.TAB_TYPE_VIDEO) {
            videoList = getAllShownVideoPath(baseActivity);
        } else {
            // Combine gallery
            imageList = getAllShownImagesPath(baseActivity);
            videoList = getAllShownVideoPath(baseActivity);
        }


        ArrayList<GalleryData> combineList = new ArrayList<>();

        int imageListSize = imageList.size();
        int videoListSize = videoList.size();
        while (imageListSize > 0 || videoListSize > 0) {
            if (imageListSize > 0 && videoListSize > 0) {

                if (imageList.get(0).date_added > videoList.get(0).date_added) {
                    combineList.add(imageList.get(0));
                    imageList.remove(0);
                    imageListSize--;
                } else {
                    combineList.add(videoList.get(0));
                    videoList.remove(0);
                    videoListSize--;
                }
            } else if (imageListSize > 0) {
                combineList.addAll(imageList);
                imageList.clear();
                imageListSize = 0;

            } else if (videoListSize > 0) {
                combineList.addAll(videoList);
                videoList.clear();
                videoListSize = 0;
            }
        }

        return combineList;
    }

    private ArrayList<GalleryData> getAllShownImagesPath(Activity activity) {

        Uri uri;
        Cursor cursor;
        ArrayList<GalleryData> listOfAllImages = new ArrayList<>();

        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.DATE_ADDED};

        cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        while (cursor.moveToNext()) {
            GalleryData galleryData = new GalleryData(Parcel.obtain());
            galleryData.imageData = new ImageGalleryData(Parcel.obtain());
            galleryData.imageData.path = cursor.getString(0);
            if (appData != null) {
                for (int i = 0; i < appData.uploadImagesList.size(); i++) {
                    if (appData.uploadImagesList.get(i).uploadImng.getPath().equals(galleryData.imageData.path)) {
                        galleryData.imageData.isSelected = true;
                    }
                }
            }
            galleryData.isImage = true;
            galleryData.date_added = cursor.getLong(1);
            listOfAllImages.add(galleryData);
        }
        Collections.reverse(listOfAllImages);
        return listOfAllImages;
    }

    private ArrayList<GalleryData> getAllShownVideoPath(Activity activity) {

        Uri uri;
        Cursor cursor;
        ArrayList<GalleryData> listOfAllVideos = new ArrayList<>();

        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED};

        cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        while (cursor.moveToNext()) {
            GalleryData galleryData = new GalleryData(Parcel.obtain());
            galleryData.videoData = new VideoGalleryData(Parcel.obtain());

            galleryData.videoData.video_id = cursor.getLong(0);
            String absolutePathOfImage = cursor.getString(1);
            galleryData.videoData.path = absolutePathOfImage;
            galleryData.videoData.resolution = cursor.getString(2);
            galleryData.videoData.duration = cursor.getInt(3);
            galleryData.videoData.size = cursor.getInt(4);

            if (appData != null) {
                if (appData.videoToUpload.equals(absolutePathOfImage)) {
                    galleryData.videoData.isSelected = true;
                }
            }

            galleryData.isImage = false;
            galleryData.date_added = cursor.getLong(5);
            listOfAllVideos.add(galleryData);
        }
        Collections.reverse(listOfAllVideos);


        return listOfAllVideos;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_gallery, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.done:
                if (appData != null && appData.mode.equalsIgnoreCase("edit")) {
                    if (appData.modePostType == Constants.TYPE_IMAGE && appData.videoToUpload != null && !appData.videoToUpload.isEmpty()) {
                        baseActivity.showToast(baseActivity.getString(R.string.please_unselect_video));
                    } else if (appData.modePostType == Constants.TYPE_VIDEO && appData.uploadImagesList.size() > 0) {
                        baseActivity.showToast(baseActivity.getString(R.string.editing_video_post));
                    } else {
                        baseActivity.finish();
                    }
                } else if (appData != null && appData.uploadImagesList.size() > 0 && appData.videoToUpload.equals("")) {
                    Intent intent = new Intent(baseActivity, MainActivity.class);
                    intent.putExtra("newConfirmType", Constants.TYPE_IMAGE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    baseActivity.startActivity(intent);
                    baseActivity.finish();

                } else if (appData != null && !appData.videoToUpload.equals("") && appData.uploadImagesList.size() == 0) {

                    Intent intent = new Intent(baseActivity, MainActivity.class);
                    intent.putExtra("newConfirmType", Constants.TYPE_VIDEO);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    baseActivity.startActivity(intent);
                    baseActivity.finish();

                } else if (appData != null && !appData.videoToUpload.equals("") && appData.uploadImagesList.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
                    builder.setMessage(R.string.image_validation);
                    builder.setPositiveButton("Images", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(baseActivity, MainActivity.class);
                            intent.putExtra("newConfirmType", Constants.TYPE_IMAGE);
                            baseActivity.startActivity(intent);
                            baseActivity.finish();

                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Video", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(baseActivity, MainActivity.class);
                            intent.putExtra("newConfirmType", Constants.TYPE_VIDEO);
                            baseActivity.startActivity(intent);
                            baseActivity.finish();

                            dialog.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    if (tab_type == Constants.TAB_TYPE_IMAGE) {
                        baseActivity.showToast(baseActivity.getString(R.string.please_select_images_to_upload));
                    } else if (tab_type == Constants.TAB_TYPE_VIDEO) {
                        baseActivity.showToast(getString(R.string.please_select_video_to_upload));
                    }
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
