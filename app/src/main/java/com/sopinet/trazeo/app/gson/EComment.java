package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

public class EComment implements Parcelable{
    public String id;
    public String body;
    public String depth;
    public String created_at;
    public String state;
    public String previous_state;
    public EThread thread;
    public String ancestors;
    public String score;
    public String author_name;

    public EComment() {
    }

    public EComment(Parcel in) {
        id = in.readString();
        body = in.readString();
        depth = in.readString();
        created_at = in.readString();
        state = in.readString();
        previous_state = in.readString();
        thread = in.readParcelable(EThread.class.getClassLoader());
        ancestors = in.readString();
        score = in.readString();
        author_name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(body);
        dest.writeString(depth);
        dest.writeString(created_at);
        dest.writeString(state);
        dest.writeParcelable(thread, flags);
        dest.writeString(ancestors);
        dest.writeString(score);
        dest.writeString(author_name);
    }

    public static final Parcelable.Creator<EComment> CREATOR
            = new Parcelable.Creator<EComment>() {
        public EComment createFromParcel(Parcel in) {
            return new EComment(in);
        }

        public EComment[] newArray(int size) {
            return new EComment[size];
        }
    };
}
