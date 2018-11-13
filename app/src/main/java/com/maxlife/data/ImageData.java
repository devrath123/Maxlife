package com.maxlife.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anshul.mittal on 23/5/16.
 */
public class ImageData implements Parcelable {

    public int id;
    public int post_id;
    public int type_id;
    public String caption;
    public String image_file;


    public ImageData(Parcel in) {
        id = in.readInt();
        post_id = in.readInt();
        type_id = in.readInt();
        caption = in.readString();
        image_file = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(post_id);
        dest.writeInt(type_id);
        dest.writeString(image_file);
        dest.writeString(caption);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static Creator<ImageData> CREATOR = new Creator<ImageData>() {
        @Override
        public ImageData createFromParcel(Parcel in) {
            return new ImageData(in);
        }

        @Override
        public ImageData[] newArray(int size) {
            return new ImageData[size];
        }
    };
}
