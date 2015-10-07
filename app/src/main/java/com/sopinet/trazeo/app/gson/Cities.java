package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Cities implements Parcelable {
    public ArrayList<String> cities;
    public String userCity;
    public ArrayList<String> data;

    public Cities() {
        cities = new ArrayList<>();
        data = new ArrayList<>();
        userCity = "";
    }

    public Cities(Parcel in) {
        cities = new ArrayList<>();
        in.readStringList(cities);
        data = new ArrayList<>();
        in.readStringList(data);
        userCity = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(cities);
        dest.writeStringList(data);
        dest.writeString(userCity);
    }

    public static final Parcelable.Creator<Cities> CREATOR
            = new Parcelable.Creator<Cities>() {
        public Cities createFromParcel(Parcel in) {
            return new Cities(in);
        }

        public Cities[] newArray(int size) {
            return new Cities[size];
        }
    };
}
