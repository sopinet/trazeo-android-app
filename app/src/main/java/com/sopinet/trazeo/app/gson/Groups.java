package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.sopinet.android.nethelper.MinimalJSON;
import com.sopinet.trazeo.app.chat.model.Group;

import java.util.ArrayList;

public class Groups extends MinimalJSON implements Parcelable{
    public ArrayList<Group> data;

    public Groups() {
        data = new ArrayList<>();
    }

    public Groups(Parcel in) {
       data = new ArrayList<>();
       in.readTypedList(data, Group.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(data);
    }

    public static final Parcelable.Creator<Groups> CREATOR
            = new Parcelable.Creator<Groups>() {
        public Groups createFromParcel(Parcel in) {
            return new Groups(in);
        }

        public Groups[] newArray(int size) {
            return new Groups[size];
        }
    };
}