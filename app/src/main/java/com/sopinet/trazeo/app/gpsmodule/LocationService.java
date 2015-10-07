package com.sopinet.trazeo.app.gpsmodule;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.MonitorActivity;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private IBinder mBinder = new LocalBinder();
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    private Boolean servicesAvailable = false;

    private ArrayList<Object[]> dataStored = new ArrayList<>();
    private int dataStoredCount;
    private int accuracy = 50;
    private String rideId;

    @Override
    public void onCreate() {
        super.onCreate();

        setUpLocationClientIfNeeded();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setInterval(8000); // Update location every 8 second
        mLocationRequest.setSmallestDisplacement(3.0f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        servicesAvailable = servicesConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Group group = Group.getGroupById(intent.getStringExtra("groupId"));
        if (group != null) {
            rideId = group.ride_id;
        }

        if(!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress) {
            return START_STICKY;
        }

        setUpLocationClientIfNeeded();

        startForeground(200, getNotification());

        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress)
        {
            mInProgress = true;
            mGoogleApiClient.connect();
        }
        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.reconnect();
        mInProgress = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getAccuracy() < accuracy) {
            accuracy = 30;
            if (dataStored.size() > 0) {
                firstLocationChanged(location);
            }
            sendPoint(location);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mGoogleApiClient.reconnect();
        mInProgress = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Notification getNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.mascota_arrow2)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Trazeo"))
                        .setContentText("Paseo activo")
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true);

        Intent startIntent = new Intent(getApplicationContext(), MonitorActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 200, startIntent, 0);
        mBuilder.setContentIntent(contentIntent);
        return mBuilder.build();
    }

    /**
     * Stops service and disconnect from Google Api Client
     */
    public void stop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        // Turn off the request flag
        mInProgress = false;
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    /*
     * Create a new Google Api Client, using the enclosing class to
     * handle callbacks.
     */
    private void setUpLocationClientIfNeeded()
    {
        if(mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        return ConnectionResult.SUCCESS == resultCode;
    }

    /**
     * Gets the last location
     * @return Location
     */
    public Location getLocation() {
        return LocationServices.FusedLocationApi.
                getLastLocation(mGoogleApiClient);
    }

    /**
     * Adds data that needs a location before being sent
     * Data are send on first Location Changed and are cleared
     * @param args Objects
     */
    public void addData(Object... args){
        this.dataStored.add(args);
    }

    public ArrayList<Object[]> getDataStored() {
        return dataStored;
    }

    public boolean isDataSended(){
        return dataStored.isEmpty();
    }

    public void sendPoint(Location location) {
        SharedPreferences myPrefs = getSharedPreferences("MyPrefs", 0);
        RequestParams params = new RequestParams();
        params.put("id_ride", rideId);
        params.put("email", myPrefs.getString("email", ""));
        params.put("pass", myPrefs.getString("pass", ""));
        params.put("latitude", location.getLatitude());
        params.put("longitude", location.getLongitude());
        params.put("createat", System.currentTimeMillis());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_SENDPOSITION, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
            }

        });
    }

    public void firstLocationChanged(Location location) {
        dataStoredCount = dataStored.size();

        for (int i = 0; i < dataStored.size(); i++) {
            String[] data = (String[]) dataStored.get(i)[1];

            RequestParams params = new RequestParams();
            params.put("id_ride", data[1]);
            params.put("email", data[3]);
            params.put("pass", data[5]);
            params.put("id_child", data[7]);
            params.put("latitude", location.getLatitude());
            params.put("longitude", location.getLongitude());
            params.put("createat", data[13]);
            params.setHttpEntityIsRepeatable(true);

            String url = dataStored.get(i)[3].toString();

            restClientFirstLocationChanged(url, params);
        }
    }

    private void restClientFirstLocationChanged(final String url, final RequestParams params) {

        RestClient.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (dataStoredCount == 0) {
                        dataStored.clear();
                    } else {
                        dataStoredCount--;
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Retry on 30 seg
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restClientFirstLocationChanged(url, params);
                    }
                }, 30000);
            }

        });
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

}
