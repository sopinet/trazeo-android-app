package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by david on 7/07/14.
 */
public class TimestampData implements Parcelable{
    public String data = "";

    public TimestampData(Parcel in) {
        data = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
    }


    public static final Parcelable.Creator<TimestampData> CREATOR
            = new Parcelable.Creator<TimestampData>() {
        public TimestampData createFromParcel(Parcel in) {
            return new TimestampData(in);
        }

        public TimestampData[] newArray(int size) {
            return new TimestampData[size];
        }
    };
}
