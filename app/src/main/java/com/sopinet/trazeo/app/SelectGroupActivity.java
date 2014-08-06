package com.sopinet.trazeo.app;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.SimpleContent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import com.sopinet.trazeo.app.gson.CreateRide;
import com.sopinet.trazeo.app.gson.Group;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.TimestampData;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.widget.ListView;
import android.widget.Toast;

@EActivity(R.layout.activity_select_group)
public class SelectGroupActivity extends ActionBarActivity{
    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    ListView listSelectGroup;

    public static NotificationManager notificationManager = null;
    public static NotificationCompat.Builder mBuilder = null;

    public TimestampData timestampData;
    public static Timestamp serverTimestamp;
    public static Timestamp localTimestamp;

    @AfterViews
    void init() {
        this.localTimestamp = getCurrentTimestamp();
        loadData();
        final LocationManager manager = (LocationManager) getSystemService( this.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
            buildAlertMessageNoGps();
    }

    @Background
    void loadData() {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email="+myPrefs.email().get();
        data += "&pass="+myPrefs.pass().get();
        String result = "";
        String resultTimestamp = "";

        try {
            resultTimestamp = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_TIMESTAMP, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectTimeStamp = new TypeToken<TimestampData>(){}.getType();
        this.timestampData = new Gson().fromJson(resultTimestamp, objectTimeStamp);

        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_GROUPS, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPD = new TypeToken<Groups>(){}.getType();

        Groups groups = new Gson().fromJson(result, objectCPD);

        if(groups.data != null && groups.data.size() > 0) {
            for (int i = 0; i < groups.data.size(); i++) {
                if (groups.data.get(i).hasride.equals("true") && groups.data.get(i).ride_id.equals(myPrefs.id_ride_monitor().get())) {
                    goActivityMonitor(false);
                }
            }
        }
        
        showData(groups);
        //sc.postUrlContent()
    }

    @UiThread
    void showData(final Groups groups) {
        this.serverTimestamp = parseFromStringToTimestamp(this.timestampData.data);

        //Toast.makeText(this, this.localTimestamp.getTime() + this.serverTimestamp.getTime() + "", Toast.LENGTH_LONG).show();

        BindDictionary<Group> dict = new BindDictionary<Group>();
        dict.addStringField(R.id.name,
                new StringExtractor<Group>() {

                    @Override
                    public String getStringValue(Group group, int position) {
                        return group.name;
                    }
                });

        dict.addStringField(R.id.description,
                new StringExtractor<Group>() {
                    @Override
                    public String getStringValue(Group item, int position) {
                        if (item.hasride.equals("true")) {
                            return "...Paseo en curso...";
                        } else {
                            return "Iniciar";
                        }

                    }
                }
        );

        FunDapter<Group> adapter = new FunDapter<Group>(this, groups.data,
                R.layout.group_list_item, dict);

        listSelectGroup.setAdapter(adapter);

        listSelectGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                createRide(groups.data.get((int) l).id, groups.data.get((int) l).hasride);
            }
        });

        //Toast.makeText(this, "Espere unos segundos mientras cargamos los datos de la Ruta...", Toast.LENGTH_LONG).show();
    }


    @Background
    void createRide(String l, String hasride) {

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String lat = "";
        String lon = "";
        if (location != null) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
        }

        SimpleContent sc = new SimpleContent(this, "trazeo", 1);
        String data = "email="+myPrefs.email().get();
        data += "&pass="+myPrefs.pass().get();
        data += "&id_group="+l;
        data += "&latitude="+lat;
        data += "&longitude="+lon;
        String result = "";

        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_RIDE_CREATE, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        myPrefs.id_group().put(l);
        goGroup(result, hasride);
    }

    @UiThread
    void goGroup(String result, String hasride){
        Log.d("TEMA", result);

        final Type objectCPD = new TypeToken<CreateRide>(){}.getType();
        CreateRide createRide = new Gson().fromJson(result, objectCPD);

        myPrefs.id_ride().put(createRide.data.id_ride);

        if (hasride.equals("true")) {
            goActivitySee();
        } else {
            myPrefs.id_ride_monitor().put(createRide.data.id_ride);

            String data_service = "email=" + myPrefs.email().get();
            data_service += "&pass=" + myPrefs.pass().get();
            data_service += "&id_ride=" + createRide.data.id_ride;

            /*intentGPS = new Intent(this, OsmLocPullService.class);
            intentGPS.putExtra("url", myPrefs.url_api().get() + Var.URL_API_SENDPOSITION);
            intentGPS.putExtra("data", data_service);
            startService(intentGPS);*/

            goActivityMonitor(true);
        }
    }

    void goActivityMonitor(Boolean isNew) {
        startActivity(new Intent(SelectGroupActivity.this, MonitorActivity_.class));
        if(isNew)
            createNotification();
        finish();
    }

    void createNotification(){
        mBuilder = new NotificationCompat.Builder(this);

        TaskStackBuilder stackBuilder_go = TaskStackBuilder.create(this);
        // TODO: Estas clases podrían ser dinámicas
        stackBuilder_go.addParentStack(MonitorActivity_.class);
        Intent intent_go = new Intent(this, MonitorActivity_.class);
        intent_go.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |   Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stackBuilder_go.addNextIntent(intent_go);
        PendingIntent resultPendingIntent_go =
                stackBuilder_go.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent_go);

        mBuilder.setContentTitle("Trazeo");
        mBuilder.setSmallIcon(R.drawable.mascota3);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String end_ride = preferences.getString("end_ride", "0");
        //mBuilder.setContentText("Paseo Activo"+end_ride);
        mBuilder.setContentText("Paseo Activo");

        // Intent para CANCELAR Paseo
        TaskStackBuilder stackBuilder_cancel = TaskStackBuilder.create(this);
        stackBuilder_cancel.addParentStack(MonitorActivity_.class);
        Intent intent_cancel = new Intent(this, MonitorActivity_.class);
        intent_cancel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |   Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent_cancel.putExtra("cancel", "1");
        stackBuilder_cancel.addNextIntent(intent_cancel);
        PendingIntent resultPendingIntent_cancel =
                stackBuilder_cancel.getPendingIntent(
                        1,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Terminar Paseo", resultPendingIntent_cancel);

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        // Debe haber un ID, el "id_raid" estaría bien
        notificationManager.notify(200, mBuilder.build());
    }

    void goActivitySee() {
        startActivity(new Intent(SelectGroupActivity.this, SeeActivity_.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_disconnect) {
            myPrefs.email().put("");
            myPrefs.pass().put("");
            myPrefs.user_id().put("");
            myPrefs.url_api().put("http://beta.trazeo.es/");
            startActivity(new Intent(this, LoginSimpleActivity_.class));
            return true;
        } else if (id == R.id.refresh){
            refreshGroups();
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshGroups(){
        startActivity(new Intent(this, SelectGroupActivity_.class));
        finish();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El GPS de tu dispositivo parece estar desactivado, ¿Quieres activarlo ahora?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public Timestamp parseFromStringToTimestamp(String data){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedDate = null;

        try {
            parsedDate = dateFormat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(parsedDate != null) {
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            return timestamp;
        }
        return null;
    }

    public static Timestamp getCurrentTimestamp(){
        Date date= new Date();
        return new Timestamp(date.getTime());
    }

}