package com.sopinet.trazeo.app.helpers;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.Serializable;

/**
 * Created by hidabe on 28/04/14.
 */
public class MyLoc implements Parcelable {
    private MyLocationNewOverlay loc;

    public MyLoc(MyLocationNewOverlay loc) {
        this.loc = loc;
    }

    public MyLocationNewOverlay getLoc() {
        return this.loc;
    }

    public void setLoc(MyLocationNewOverlay loc) {
        this.loc = loc;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeSerializable((Serializable) loc);
    }
}
