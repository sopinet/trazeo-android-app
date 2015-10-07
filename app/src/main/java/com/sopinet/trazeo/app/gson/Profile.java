package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable{
    public String points = "";
    public String nick = "";
    public String name = "";
    public String mobile = "";
    public String use_like = "";
    public City city =  new City();

    public Profile() {
    }

    public Profile(Parcel in) {
        points = in.readString();
        nick = in.readString();
        name = in.readString();
        mobile = in.readString();
        use_like = in.readString();
        city = in.readParcelable(City.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(points);
        dest.writeString(nick);
        dest.writeString(name);
        dest.writeString(mobile);
        dest.writeString(use_like);
        dest.writeParcelable(city, flags);
    }

    public static final Parcelable.Creator<Profile> CREATOR
            = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
