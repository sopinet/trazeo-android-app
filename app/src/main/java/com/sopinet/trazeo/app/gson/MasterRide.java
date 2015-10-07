package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.sopinet.android.nethelper.MinimalJSON;

public class MasterRide extends MinimalJSON implements Parcelable{
    public ERide data;

    public MasterRide() {
        data = new ERide();
    }

    public MasterRide(Parcel in) {
        data = in.readParcelable(ERide.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
    }

    public static final Parcelable.Creator<MasterRide> CREATOR
            = new Parcelable.Creator<MasterRide>() {
        public MasterRide createFromParcel(Parcel in) {
            return new MasterRide(in);
        }

        public MasterRide[] newArray(int size) {
            return new MasterRide[size];
        }
    };
}