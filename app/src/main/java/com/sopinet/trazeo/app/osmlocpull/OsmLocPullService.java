package com.sopinet.trazeo.app.osmlocpull;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.sopinet.trazeo.app.helpers.MyLoc;

public class OsmLocPullService extends Service {
    OsmLocPullReceiver osmReceiver;

    public OsmLocPullService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Bundle extras = intent.getExtras();
        osmReceiver = new OsmLocPullReceiver();
        osmReceiver.url = extras.getString("url");
        osmReceiver.data = extras.getString("data");
        Log.d("GPSLOG", "ServiceCommand: "+osmReceiver.data);
        osmReceiver.SetLocPull(OsmLocPullService.this);
        return START_STICKY;
    }

    public void onStart(Context context,Intent intent, int startId)
    {
        Bundle extras = intent.getExtras();
        osmReceiver = new OsmLocPullReceiver();
        osmReceiver.url = extras.getString("url");
        osmReceiver.data = extras.getString("data");
        Log.d("GPSLOG", "Service: "+osmReceiver.data);
        osmReceiver.SetLocPull(context);
    }
}
