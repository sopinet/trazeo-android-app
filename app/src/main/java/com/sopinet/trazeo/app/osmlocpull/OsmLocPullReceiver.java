package com.sopinet.trazeo.app.osmlocpull;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.AlarmClock;
import android.util.Log;
import android.widget.Toast;

import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.helpers.MyLoc;
import com.sopinet.trazeo.app.helpers.Var;

import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

// http://stackoverflow.com/questions/4459058/alarm-manager-example
public class OsmLocPullReceiver extends BroadcastReceiver {
    // public MyLoc myLoc;
    public String url;
    public String data;

    public OsmLocPullReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("GPSLOG", "OnReceive");
        Log.d("GPSLOG", "OnReceive: "+this.url);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        /**
        MyLocationNewOverlay myLoc = new MyLocationNewOverlay(context, null);
        String lat = String.valueOf(myLoc.getMyLocation().getLatitude());
        String lon = String.valueOf(myLoc.getMyLocation().getLongitude());
         **/
        String lat = "0";
        String lon = "0";

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());

            // TEMP DATA, REVISAR
            // this.data = "email=trazeo@trazeo.es&pass=a3cd14aa603f9574b1917d2b8595e4b9&id_ride=1";
            // this.url = Var.URL_API_SENDPOSITION;
            Bundle extras = intent.getExtras();
            Log.d("GPSLOG", "EXTRA: "+intent.getStringExtra("url"));
            if (extras != null) {
                this.url = extras.getString("url");
                this.data = extras.getString("data");
                Log.d("GPSLOG", "OnReceiveExtras: " + this.url
                );
            }

            if (this.url != null) {
                SimpleContent sc = new SimpleContent(context, "trazeo", 3);
                this.data += "&latitude=" + lat;
                this.data += "&longitude=" + lon;
                Log.d("TEMA", "DATA_SENDPOINT: "+this.data);

                String result = "";

                try {
                    result = sc.postUrlContent(this.url, this.data);
                } catch (SimpleContent.ApiException e) {
                    e.printStackTrace();
                }
                Log.d("TEMA", result);
            }

            //location.getLatitude();
            //location.getLongitude();
        }


        // Put here YOUR code.
        Toast.makeText(context, "Alarm! " + lat + " - " + lon, Toast.LENGTH_LONG).show(); // For example

        wl.release();
    }

    public void SetLocPull(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        //OsmLocPullReceiver osmLoc = new OsmLocPullReceiver(this.url, this.data);
        Intent i = new Intent(context, OsmLocPullReceiver.class);
        Log.d("GPSLOG", "Set");
        Log.d("GPSLOG", "Set: "+this.url);
        if (this.url != null) {
            i.putExtra("url", this.url);
            i.putExtra("data", this.data);
            Log.d("GPSLOG", "SET!");
        }
        PendingIntent pi;
        pi = PendingIntent.getBroadcast(context, 0, i, FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 30 * 1, pi); // Millisec * Second * Minute (cada 30 Segundos)
    }

    public void CancelLocPull(Context context)
    {
        Log.d("GPSLOG", "Cancel");
        Intent intent = new Intent(context, OsmLocPullReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
