package com.sopinet.trazeo.app.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.sopinet.trazeo.app.gson.Route;

import java.util.List;

@Table(name = "Groups")
public class Group extends Model implements Parcelable{

    @Column(name = "Idd", unique = true)
    public String id;

    @Column(name = "Name")
    public String name;

    @Column(name = "Chat")
    public Chat chat;

    @Column(name = "HasMessage")
    public int hasMessage = 0;

    @Column(name = "IsMonitor")
    public String isMonitor = "false";

    @Column(name = "HasRide")
    public String hasride = "false";

    @Column(name = "RideId")
    public String ride_id = "-1";

    @Column(name = "RideCreator")
    public String rideCreator = "";

    public Route route;
    public String visibility = "";
    public String admin = "";
    public String city = "";
    public String school = "";

    public Group() {
        super();
    }

    public Group(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Group(Parcel in) {
        id = in.readString();
        name = in.readString();
        route = in.readParcelable(Route.class.getClassLoader());
        visibility = in.readString();
        hasride = in.readString();
        ride_id = in.readString();
        admin = in.readString();
        city = in.readString();
        school = in.readString();
        isMonitor = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeParcelable(route, flags);
        dest.writeString(visibility);
        dest.writeString(hasride);
        dest.writeString(ride_id);
        dest.writeString(admin);
        dest.writeString(city);
        dest.writeString(school);
        dest.writeString(isMonitor);
    }

    public static final Parcelable.Creator<Group> CREATOR
            = new Parcelable.Creator<Group>() {
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public static Group getGroupById(String id) {
        return new Select().from(Group.class).where("Idd = ?", id).executeSingle();
    }

    public static int getCount() {
        return new Select().from(Group.class).execute().size();
    }

    public static List<Group> getAllGroups() {
        return new Select().from(Group.class).execute();
    }
}