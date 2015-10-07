package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

public class Route implements Parcelable {
    public String name = "";
    public String admin_name = "";

    public Route(Parcel in) {
        name = in.readString();
        admin_name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(admin_name);
    }

    public static final Parcelable.Creator<Route> CREATOR
            = new Parcelable.Creator<Route>() {
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

}
