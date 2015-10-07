package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ERoute implements Parcelable{
    public String id;
    public ArrayList<EPoint> points;
    public String name;

    public ERoute() {
        id = "";
        points = new ArrayList<>();
        name = "";
    }

    public ERoute(Parcel in) {
      id = in.readString();
      in.readTypedList(points, EPoint.CREATOR);
      name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeTypedList(points);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<ERoute> CREATOR
            = new Parcelable.Creator<ERoute>() {
        public ERoute createFromParcel(Parcel in) {
            return new ERoute(in);
        }

        public ERoute[] newArray(int size) {
            return new ERoute[size];
        }
    };
}