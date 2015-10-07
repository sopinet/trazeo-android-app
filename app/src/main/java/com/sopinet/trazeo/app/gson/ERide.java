package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ERide implements Parcelable{
    public String id = "";
    public EGroup group;
    public ArrayList<EEvent> events;
    public String created_at;
    public String updated_at;

    public ERide() {
        id = "";
        group = new EGroup();
        events = new ArrayList<>();
        created_at = "";
        updated_at = "";
    }

    public ERide(Parcel in) {
        id = in.readString();
        group = in.readParcelable(EGroup.class.getClassLoader());
        in.readTypedList(events, EEvent.CREATOR);
        created_at = in.readString();
        updated_at = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(group, flags);
        dest.writeTypedList(events);
        dest.writeString(created_at);
        dest.writeString(updated_at);
    }

    public static final Parcelable.Creator<ERide> CREATOR
            = new Parcelable.Creator<ERide>() {
        public ERide createFromParcel(Parcel in) {
            return new ERide(in);
        }

        public ERide[] newArray(int size) {
            return new ERide[size];
        }
    };
}