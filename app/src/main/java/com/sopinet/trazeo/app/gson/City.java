package com.sopinet.trazeo.app.gson;


import android.os.Parcel;
import android.os.Parcelable;

public class City implements Parcelable{
    public String name_ascii = "";

    public City() {
        name_ascii = "";
    }

    public City(Parcel in) {
        name_ascii = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name_ascii);
    }

    public static final Parcelable.Creator<City> CREATOR
            = new Parcelable.Creator<City>() {
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        public City[] newArray(int size) {
            return new City[size];
        }
    };
}
