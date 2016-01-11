package com.sopinet.trazeo.app.gson;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Comparator;

public class Member implements Parcelable{
    public String name = "";
    public String mobile = "";
    public ArrayList<Child> childrens;

    public Member(String name, String phone) {
        this.name = name;
        this.mobile = phone;
        childrens = new ArrayList<>();
    }

    public Member(Parcel in) {
        name = in.readString();
        mobile = in.readString();
        childrens = in.createTypedArrayList(Child.CREATOR);
    }

    public String childrensToString() {
        String result = "";
        if (childrens != null) {
            result = "(";
            for (int i = 0; i < childrens.size(); i++) {
                if (i == childrens.size() -1) {
                    result += childrens.get(i).name.trim();
                } else {
                    result += childrens.get(i).name + ", ";
                }
            }
            result += ")";
        }
        if (result.equals("()")) {
            result = "";
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mobile);
        dest.writeTypedList(childrens);
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
