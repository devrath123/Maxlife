package com.maxlife.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anshul.mittal on 18/5/16.
 */
public class ImageGalleryData implements Parcelable {

    public String path;
    public boolean isSelected;


    public ImageGalleryData(Parcel in) {
        path = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static Creator<ImageGalleryData> CREATOR = new Creator<ImageGalleryData>() {
        @Override
        public ImageGalleryData createFromParcel(Parcel in) {
            return new ImageGalleryData(in);
        }

        @Override
        public ImageGalleryData[] newArray(int size) {
            return new ImageGalleryData[size];
        }
    };
}
