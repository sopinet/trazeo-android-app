package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.sopinet.android.nethelper.MinimalJSON;


public class MyProfile extends MinimalJSON implements Parcelable {
    public Profile data;

    public MyProfile() {
        data = new Profile();
    }

    public MyProfile(Parcel in) {
        data = in.readParcelable(Profile.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
    }

    public static final Parcelable.Creator<MyProfile> CREATOR
            = new Parcelable.Creator<MyProfile>() {
        public MyProfile createFromParcel(Parcel in) {
            return new MyProfile(in);
        }

        public MyProfile[] newArray(int size) {
            return new MyProfile[size];
        }
    };
}




