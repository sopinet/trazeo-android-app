package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

public class EThread implements Parcelable{
    public String id;
    public String is_commentable;
    public String num_comments;
    public String permalink;

    public EThread() {
    }

    public EThread(Parcel in) {
        id = in.readString();
        is_commentable = in.readString();
        num_comments = in.readString();
        permalink = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(is_commentable);
        dest.writeString(num_comments);
        dest.writeString(permalink);
    }

    public static final Parcelable.Creator<EThread> CREATOR
            = new Parcelable.Creator<EThread>() {
        public EThread createFromParcel(Parcel in) {
            return new EThread(in);
        }

        public EThread[] newArray(int size) {
            return new EThread[size];
        }
    };
}
