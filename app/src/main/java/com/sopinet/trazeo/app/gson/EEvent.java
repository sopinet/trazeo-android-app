package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;


public class EEvent implements Parcelable{
    public String id = "";
    public String action = "";
    public String data = "";
    public String created_at = "";
    public String updated_at = "";
    public ELocation location;

    public EEvent() {
        id = "";
        action = "";
        data = "";
        created_at = "";
        updated_at = "";
        location = new ELocation();
    }

    public EEvent(Parcel in) {
        id = in.readString();
        action = in.readString();
        data = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        location = in.readParcelable(ELocation.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(action);
        dest.writeString(created_at);
        dest.writeString(updated_at);
        dest.writeParcelable(location, flags);
    }

    public static final Parcelable.Creator<EEvent> CREATOR
            = new Parcelable.Creator<EEvent>() {
        public EEvent createFromParcel(Parcel in) {
            return new EEvent(in);
        }

        public EEvent[] newArray(int size) {
            return new EEvent[size];
        }
    };
}