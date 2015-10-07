package com.sopinet.trazeo.app.gson;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class Member implements Parcelable{
    public String name = "";
    public String mobile = "";

    public Member(String name, String phone) {
        this.name = name;
        this.mobile = phone;
    }

    public Member(Parcel in) {
        name = in.readString();
        mobile = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mobile);
    }

    public static final Parcelable.Creator<Member> CREATOR
            = new Parcelable.Creator<Member>() {
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    /**
     * Comparator for sorting the list by Friend name
     */
    public static Comparator<Member> nameComparator = new Comparator<Member>() {

        public int compare(Member f1, Member f2) {
            String member1 = f1.name.toUpperCase();
            String member2 = f2.name.toUpperCase();
            //ascending order
            return member1.compareTo(member2);
        }
    };
}
