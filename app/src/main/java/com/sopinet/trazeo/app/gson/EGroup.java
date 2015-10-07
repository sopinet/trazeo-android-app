package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class EGroup implements Parcelable{
    public String id;
    public ArrayList<EChild> childs;
    public ERoute route;
    public String visibility;
    public String has_ride;
    public String name;
    public String admin;

    public EGroup() {
        id = "";
        childs = new ArrayList<>();
        route = new ERoute();
        visibility = "";
        has_ride = "";
        name = "";
        admin = "";
    }

    public EGroup(Parcel in) {
        id = in.readString();
        in.readTypedList(childs, EChild.CREATOR);
        route = in.readParcelable(ERoute.class.getClassLoader());
        visibility = in.readString();
        has_ride = in.readString();
        name = in.readString();
        admin = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeTypedList(childs);
        dest.writeParcelable(route, flags);
        dest.writeString(visibility);
        dest.writeString(has_ride);
        dest.writeString(name);
        dest.writeString(admin);
    }

    public static final Parcelable.Creator<EGroup> CREATOR
            = new Parcelable.Creator<EGroup>() {
        public EGroup createFromParcel(Parcel in) {
            return new EGroup(in);
        }

        public EGroup[] newArray(int size) {
            return new EGroup[size];
        }
    };
}