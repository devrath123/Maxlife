package com.maxlife.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anshul.mittal on 18/5/16.
 */
public class VideoGalleryData implements Parcelable {

    public long video_id;
    public String path;
    public String resolution;
    public int duration;
    public int size;
    public boolean isSelected;


    public VideoGalleryData(Parcel in) {
        video_id = in.readLong();
        path = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(video_id);
        dest.writeString(path);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static Creator<VideoGalleryData> CREATOR = new Creator<VideoGalleryData>() {
        @Override
        public VideoGalleryData createFromParcel(Parcel in) {
            return new VideoGalleryData(in);
        }

        @Override
        public VideoGalleryData[] newArray(int size) {
            return new VideoGalleryData[size];
        }
    };
}
