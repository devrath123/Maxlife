package com.maxlife.data;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by anshul.mittal on 2/5/16.
 */
public class ApplicationData extends Application {

    public ProfileData profileData;
    public ArrayList<UploadImgData> uploadImagesList = new ArrayList<>();
    public String videoToUpload = "";
    public Bitmap videoToUploadBitmap;
    public boolean is_video_recording;

    public String mode = "";
    public int modePostType = 0;

    public int galleryTabSelected;

    public String url;
}
