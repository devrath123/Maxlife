package com.maxlife.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anshul.mittal on 23/5/16.
 */
public class VideoData implements Parcelable {

    public int id;
    public int post_id;
    public int type_id;
    public String video_file;
    public String caption;
    public String video_thumbnail;
    public String video_name;

    public VideoData(Parcel in) {
        id = in.readInt();
        post_id = in.readInt();
        type_id = in.readInt();
        video_file = in.readString();
        video_thumbnail = in.readString();
        caption = in.readString();
        video_name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(post_id);
        dest.writeInt(type_id);
        dest.writeString(video_file);
        dest.writeString(video_thumbnail);
        dest.writeString(caption);
        dest.writeString(video_name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoData> CREATOR = new Creator<VideoData>() {
        @Override
        public VideoData createFromParcel(Parcel in) {
            return new VideoData(in);
        }

        @Override
        public VideoData[] newArray(int size) {
            return new VideoData[size];
        }
    };
}
