package com.maxlife.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anshul.mittal on 8/6/16.
 */
public class GalleryData implements Parcelable {

    public ImageGalleryData imageData;
    public VideoGalleryData videoData;
    public boolean isImage;
    public long date_added;

    public GalleryData(Parcel in) {
        imageData = in.readParcelable(ImageGalleryData.class.getClassLoader());
        videoData = in.readParcelable(VideoGalleryData.class.getClassLoader());
        isImage = in.readByte() != 0;
        date_added = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(imageData, flags);
        dest.writeParcelable(videoData, flags);
        dest.writeByte((byte) (isImage ? 1 : 0));
        dest.writeLong(date_added);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GalleryData> CREATOR = new Creator<GalleryData>() {
        @Override
        public GalleryData createFromParcel(Parcel in) {
            return new GalleryData(in);
        }

        @Override
        public GalleryData[] newArray(int size) {
            return new GalleryData[size];
        }
    };
}
