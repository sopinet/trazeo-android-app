package com.sopinet.trazeo.app.osmlocpull;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.MonitorActivity;
import com.sopinet.trazeo.app.MonitorActivity_;
import com.sopinet.trazeo.app.R;
import com.sopinet.trazeo.app.gson.LastPoint;
import com.sopinet.trazeo.app.helpers.MyLoc;
import com.sopinet.trazeo.app.helpers.Var;

import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.lang.reflect.Type;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

// http://stackoverflow.com/questions/4459058/alarm-manager-example
public class OsmLocPullReceiver extends BroadcastReceiver {
    // public MyLoc myLoc;
    public String url;
    public String data;
    public static NotificationManager notificationManager = null;
    public static NotificationCompat.Builder mBuilder = null;

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

                final Type objectCPD = new TypeToken<LastPoint>(){}.getType();
                LastPoint lastPoint = new Gson().fromJson(result, objectCPD);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (lastPoint != null && lastPoint.data != null) {
                   preferences.getString("end_ride", lastPoint.data.updated_at);
                }
                // Log.d("TEMA", "HORA ACTUAL: "+lastPoint.data.updated_at);
                // TODO: Result puede ser nulo, deberíamos revisarlo
                //Log.d("TEMA", result);


//                mBuilder.setContentText(result);
            }

            //location.getLatitude();
            //location.getLongitude();
        }


        // Put here YOUR code.
        //Toast.makeText(context, "Alarm! " + lat + " - " + lon, Toast.LENGTH_LONG).show(); // For example

        wl.release();
    }

    public void SetLocPull(Context context)
    {
        mBuilder = new NotificationCompat.Builder(context);

        TaskStackBuilder stackBuilder_go = TaskStackBuilder.create(context);
        /*
        dynamic class

        		Class<?> cls = null;
		try {
			cls = Class.forName(MediaUploader.SENDINGCLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
         */
        // TODO: Estas clases podrían ser dinámicas
        stackBuilder_go.addParentStack(MonitorActivity_.class);
        Intent intent_go = new Intent(context, MonitorActivity_.class);
        stackBuilder_go.addNextIntent(intent_go);
        PendingIntent resultPendingIntent_go =
                stackBuilder_go.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent_go);

        mBuilder.setContentTitle("Trazeo");
        mBuilder.setSmallIcon(R.drawable.mascota3);
        //mBuilder.setNumber(12);
        //mBuilder.setProgress(100, 12, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String end_ride = preferences.getString("end_ride", "0");
        mBuilder.setContentText("Se encuentra en una ruta ACTIVA "+end_ride);

        // Intent para CANCELAR Paseo
        TaskStackBuilder stackBuilder_cancel = TaskStackBuilder.create(context);
        stackBuilder_cancel.addParentStack(MonitorActivity_.class);
        Intent intent_cancel = new Intent(context, MonitorActivity_.class);
        intent_cancel.putExtra("cancel", "1");
        stackBuilder_cancel.addNextIntent(intent_cancel);
        PendingIntent resultPendingIntent_cancel =
                stackBuilder_cancel.getPendingIntent(
                        1,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Terminar Paseo", resultPendingIntent_cancel);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Debe haber un ID, el "id_raid" estaría bien
        notificationManager.notify(200, mBuilder.build());



        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        //OsmLocPullReceiver osmLoc = new OsmLocPullReceiver(this.url, this.data);
        Intent i = new Intent(context, OsmLocPullReceiver.class);
        if (this.url != null) {
            i.putExtra("url", this.url);
            i.putExtra("data", this.data);
        }
        PendingIntent pi;
        pi = PendingIntent.getBroadcast(context, 0, i, FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 10 * 1, pi); // Millisec * Second * Minute (cada 30 Segundos)
    }

    public void CancelLocPull(Context context)
    {
        Log.d("GPSLOG", "Cancel");

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        //OsmLocPullReceiver osmLoc = new OsmLocPullReceiver(this.url, this.data);
        Intent i = new Intent(context, OsmLocPullReceiver.class);
        if (this.url != null) {
            i.putExtra("url", this.url);
            i.putExtra("data", this.data);
        }
        PendingIntent pi;
        pi = PendingIntent.getBroadcast(context, 0, i, FLAG_UPDATE_CURRENT);
        am.cancel(pi);
    }
}
