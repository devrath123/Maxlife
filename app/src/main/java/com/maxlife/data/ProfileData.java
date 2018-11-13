package com.maxlife.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by anshul.mittal on 9/5/16.
 */
public class ProfileData implements Parcelable {

    public int id;
    public String full_name;
    public String email;
    public String role_id;
    public String first_name;
    public String last_name;
    public String contact_no;
    public String nominated_email;
    public int is_membership;
    public String image_file;
    public ArrayList<String> nominatedEmailList = new ArrayList<>();

    public int is_let;
    public int is_activate;
    public int type_id;
    public int repeat_days;


    public ProfileData(Parcel in) {
        id = in.readInt();
        full_name = in.readString();
        email = in.readString();
        role_id = in.readString();
        first_name = in.readString();
        last_name = in.readString();
        contact_no = in.readString();
        nominated_email = in.readString();
        is_membership = in.readInt();
        image_file = in.readString();
        nominatedEmailList = in.createStringArrayList();
        is_let = in.readInt();
        is_activate = in.readInt();
        type_id = in.readInt();
        repeat_days = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(full_name);
        dest.writeString(email);
        dest.writeString(role_id);
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(contact_no);
        dest.writeString(nominated_email);
        dest.writeInt(is_membership);
        dest.writeString(image_file);
        dest.writeStringList(nominatedEmailList);
        dest.writeInt(is_let);
        dest.writeInt(is_activate);
        dest.writeInt(type_id);
        dest.writeInt(repeat_days);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProfileData> CREATOR = new Creator<ProfileData>() {
        @Override
        public ProfileData createFromParcel(Parcel in) {
            return new ProfileData(in);
        }

        @Override
        public ProfileData[] newArray(int size) {
            return new ProfileData[size];
        }
    };
}
