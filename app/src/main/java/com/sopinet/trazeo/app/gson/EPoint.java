package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

public class EPoint implements Parcelable{
    public String id;
    public String pickup;
    public ELocation location;

    public EPoint() {
        id = "";
        pickup = "";
        location = new ELocation();
    }

    public EPoint(Parcel in) {
        id = in.readString();
        pickup = in.readString();
        location = in.readParcelable(ELocation.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(pickup);
        dest.writeParcelable(location, flags);
    }

    public static final Parcelable.Creator<EPoint> CREATOR
            = new Parcelable.Creator<EPoint>() {
        public EPoint createFromParcel(Parcel in) {
            return new EPoint(in);
        }

        public EPoint[] newArray(int size) {
            return new EPoint[size];
        }
    };
}