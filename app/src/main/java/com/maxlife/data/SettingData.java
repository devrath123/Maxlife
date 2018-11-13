package com.maxlife.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by TOXSL\visha.sehgal on 15/6/16.
 */
public class SettingData implements Parcelable {
    public String title;
    public int id;

    public SettingData(Parcel in) {
        title = in.readString();
        id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SettingData> CREATOR = new Creator<SettingData>() {
        @Override
        public SettingData createFromParcel(Parcel in) {
            return new SettingData(in);
        }

        @Override
        public SettingData[] newArray(int size) {
            return new SettingData[size];
        }
    };
}
