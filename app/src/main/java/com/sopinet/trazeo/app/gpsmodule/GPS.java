package com.sopinet.trazeo.app.gpsmodule;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by david on 24/06/14.
 */
public class GPS {
    private IGPSActivity main;

    private LocationListener mlocListener;
    private LocationManager mlocManager;

    private boolean isRunning;

    public GPS(IGPSActivity main) {
        this.main = main;

        mlocManager = (LocationManager) ((Activity) this.main).getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mlocListener);

        this.isRunning = true;
    }

    public void stopGPS() {
        if(isRunning) {
            mlocManager.removeUpdates(mlocListener);
            this.isRunning = false;
        }
    }

    public void resumeGPS() {
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mlocListener);
        this.isRunning = true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            GPS.this.main.locationChanged(loc.getLongitude(), loc.getLatitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }
}
