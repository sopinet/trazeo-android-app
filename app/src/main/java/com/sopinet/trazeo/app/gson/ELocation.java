package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

public class ELocation implements Parcelable{
    public String latitude;
    public String longitude;

    public ELocation() {
        latitude = "";
        longitude = "";
    }

    public ELocation(Parcel in) {
        latitude = in.readString();
        longitude = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(latitude);
        dest.writeString(longitude);
    }

    public static final Parcelable.Creator<ELocation> CREATOR
            = new Parcelable.Creator<ELocation>() {
        public ELocation createFromParcel(Parcel in) {
            return new ELocation(in);
        }

        public ELocation[] newArray(int size) {
            return new ELocation[size];
        }
    };
}