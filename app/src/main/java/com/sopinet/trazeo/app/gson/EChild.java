package com.sopinet.trazeo.app.gson;

import android.os.Parcel;
import android.os.Parcelable;

public class EChild implements Parcelable{
    public String id;
    public String nick;
    public String date_birth;
    public String visibility;
    public String gender;
    public String scholl;
    public String selected = "-";


    public EChild() {
        id = "";
        nick = "";
        date_birth = "";
        visibility = "";
        gender = "";
        scholl = "";
        selected = "-";
    }

    public EChild(String nick, String gender) {
        this.nick = nick;
        this.gender = gender;
        this.selected = "false";
    }

    public EChild(Parcel in) {
        id = in.readString();
        nick = in.readString();
        date_birth= in.readString();
        visibility= in.readString();
        gender= in.readString();
        scholl= in.readString();
        selected = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nick);
        dest.writeString(date_birth);
        dest.writeString(visibility);
        dest.writeString(gender);
        dest.writeString(scholl);
        dest.writeString(selected);
    }

    public static final Parcelable.Creator<EChild> CREATOR
            = new Parcelable.Creator<EChild>() {
        public EChild createFromParcel(Parcel in) {
            return new EChild(in);
        }

        public EChild[] newArray(int size) {
            return new EChild[size];
        }
    };

    public boolean isSelected() {
        try {
            return this.selected.equals("true");
        } catch(NullPointerException ne) {
            return false;
        }
    }
    public void setSelected(Boolean selected) {
        this.selected = selected.toString();
    }
}