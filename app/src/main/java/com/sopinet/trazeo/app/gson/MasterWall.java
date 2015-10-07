package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MasterWall implements Parcelable{
    public ArrayList<EComment> data;

    public MasterWall() {
    }

    public MasterWall(Parcel in) {
        in.readTypedList(data, EComment.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(data);
    }

    public static final Parcelable.Creator<MasterWall> CREATOR
            = new Parcelable.Creator<MasterWall>() {
        public MasterWall createFromParcel(Parcel in) {
            return new MasterWall(in);
        }

        public MasterWall[] newArray(int size) {
            return new MasterWall[size];
        }
    };
}
