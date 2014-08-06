package com.sopinet.trazeo.app.gpsmodule;

import android.app.Activity;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by david on 24/06/14.
 */
public class GPS {
    private IGPSActivity main;

    private LocationListener mlocListener;

    private LocationManager mlocManager;

    private boolean isRunning;

    private GpsStatus.Listener gpsListener;

    private boolean isFixed = false;

    public ArrayList<Object[]> dataList;

    public GPS(final IGPSActivity main) {
        this.main = main;

        mlocManager = (LocationManager) ((Activity) this.main).getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mlocListener);

        dataList = new ArrayList<Object[]>();

        gpsListener = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                GpsStatus gpsStatus = mlocManager.getGpsStatus(null);
                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        Log.i("GPSMODULE", "onGpsStatusChanged(): GPS started");
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        Log.i("GPSMODULE", "onGpsStatusChanged(): time to first fix in ms = " + gpsStatus.getTimeToFirstFix());
                        setFixed(true);
                        main.gpsFirstFix(getLastLocation());
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        //Log.d("GPSMODULE", "onGpsStatusChanged(): ##,used,s/n,az,el");
                        Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
                        Iterator<GpsSatellite> satI = satellites.iterator();
                        while (satI.hasNext()) {
                            GpsSatellite satellite = satI.next();
                            /*Log.d("GPSMODULE", "onGpsStatusChanged(): " +
                                    satellite.getPrn() + "," + satellite.usedInFix() + "," +
                                    satellite.getSnr() + "," + satellite.getAzimuth() + "," +
                                    satellite.getElevation());*/
                        }
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        Log.i("GPSMODULE", "onGpsStatusChanged(): GPS stopped");
                        break;
                }
            }
        };

        mlocManager.addGpsStatusListener(gpsListener);

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

    public Location getLastLocation(){
        return this.mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean isFixed) {
        this.isFixed = isFixed;
    }

    public void addData(Object... args){
        this.dataList.add(args);
    }

    public void addData(int index, Object... args){
        this.dataList.add(index, args);
    }

    public class MyLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location loc) {
            GPS.this.main.locationChanged(loc.getLongitude(), loc.getLatitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
