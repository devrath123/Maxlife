package com.maxlife.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by anshul.mittal on 18/5/16.
 */
public class PostData implements Parcelable {

    public int id;
    public int type_id;
    public String create_time;
    public String tagged_email;
    public String caption;

    public String content;

    public ArrayList<ImageData> image_file_list;

    public VideoData videoData;


    public PostData(Parcel in) {
        id = in.readInt();
        type_id = in.readInt();
        create_time = in.readString();
        tagged_email = in.readString();
        content = in.readString();
        caption = in.readString();
        image_file_list = in.createTypedArrayList(ImageData.CREATOR);
        videoData = in.readParcelable(VideoData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type_id);
        dest.writeString(create_time);
        dest.writeString(tagged_email);
        dest.writeString(content);
        dest.writeString(caption);
        dest.writeTypedList(image_file_list);
        dest.writeParcelable(videoData, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PostData> CREATOR = new Creator<PostData>() {
        @Override
        public PostData createFromParcel(Parcel in) {
            return new PostData(in);
        }

        @Override
        public PostData[] newArray(int size) {
            return new PostData[size];
        }
    };
}
