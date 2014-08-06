package com.sopinet.trazeo.app;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.extractors.StringExtractor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.mediauploader.HttpPostHelper;
import com.sopinet.android.mediauploader.MediaUploader;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gpsmodule.GPS;
import com.sopinet.trazeo.app.gpsmodule.IGPSActivity;
import com.sopinet.trazeo.app.gson.EChild;
import com.sopinet.trazeo.app.gson.MasterRide;
import com.sopinet.trazeo.app.gson.MasterWall;
import com.sopinet.trazeo.app.helpers.ChildAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.fragment_monitor_child)
public class MonitorActivity extends ActionBarActivity implements IGPSActivity {

    @Extra
    String cancel;

    @Pref
    public MyPrefs_ myPrefs;

    public static MasterRide ride;
    public static MasterWall wall;

    String data = null;
    String sendPointUrl = null;

    ProgressDialog pdialog;

    ListView listChildren;

    private GPS gps;

    private double longitude;
    private double latitude;

    @AfterViews
    void init() {
        this.configureBar();

        data = "id_ride=" + myPrefs.id_ride().get() + "&email=" + myPrefs.email().get() + "&pass=" + myPrefs.pass().get();
        this.sendPointUrl = myPrefs.url_api().get() + Var.URL_API_SENDPOSITION;

        if (cancel != null && cancel.equals("1")) {
            showCancelDialog();
        }
        pdialog = new ProgressDialog(this);
        pdialog.setCancelable(false);
        pdialog.setMessage("Cargando...");
        pdialog.show();

        MediaUploader.MODE = "any";
        MediaUploader.SENDINGCONTEXT = "com.sopinet.trazeo.app";
        MediaUploader.SENDINGCLASS = "com.sopinet.trazeo.app.MonitorActivity_";
        MediaUploader.RES_OK = "json";
        MediaUploader.NOTIFICATION_ENABLED = false;

        loadData();
    }

    @UiThread
    void showCancelDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trazeo: Terminar paseo");
        builder.setMessage("Se terminará tu paseo actual, ¿Estás seguro?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        finishRide(1);
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

    @UiThread
    void showDisconnectDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trazeo: Desconectar");
        builder.setMessage("Se terminará tu paseo actual y desconectarás tu usuario de la aplicación, ¿Estás seguro?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        myPrefs.url_api().put("http://beta.trazeo.es/");
                        finishRide(2);
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

    @UiThread
    void showReportDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View reportDialog = li.inflate(R.layout.report_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(reportDialog);
        final EditText userInput = (EditText) reportDialog.findViewById(R.id.editREPORT);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // TODO: ENVIAR LA INFO AL SERVIDOR
                                dialog.cancel();
                                showWaitDialog();
                                sendReport(userInput.getText().toString());
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Background
    void sendReport(String text) {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String sdata = data;
        sdata += "&text=" + text;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String lat = "";
        String lon = "";
        if (location != null) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
        }

        sdata += "&latitude=" + lat;
        sdata += "&longitude=" + lon;

        String result = "";
        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_SENDREPORT, sdata);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        showReportOK();
    }

    @UiThread
    void showReportOK() {
        //wait.dismiss();
        pdialog.cancel();
        Toast.makeText(this, "El reporte ha sido enviado con éxito.", Toast.LENGTH_LONG).show();
    }

    public void finishRide(int requestCode) {
        if (requestCode == 1 || requestCode == 2) {
            // Mensaje de inicio de detención de PASEO
            showWaitDialog();

            // stopService(SelectGroupActivity.intentGPS);  // TODO: No estoy seguro de que esto sea necesario

            // Cancelamos la ALARMA
            /*String data_service = "email=" + myPrefs.email().get();
            data_service += "&pass=" + myPrefs.pass().get();
            data_service += "&id_ride=" + myPrefs.id_ride().get();

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(this, OsmLocPullReceiver.class);
            i.putExtra("url", myPrefs.url_api().get() + Var.URL_API_SENDPOSITION);
            i.putExtra("data", data_service);

            PendingIntent pi;
            pi = PendingIntent.getBroadcast(this, 0, i, FLAG_UPDATE_CURRENT);
            am.cancel(pi);*/

            // Eliminamos las ficaciones
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(200);
            //notificationManager.cancel(201);

            // Deslogueamos (si es necesario)
            if (requestCode == 2) {
                myPrefs.email().put("");
                myPrefs.pass().put("");
                myPrefs.user_id().put("");
            }

            // Enviamos solicitud de fin de PASEO en Background

            sendFinishRide();
        }
    }

    @Background
    void sendFinishRide() {
        //SimpleContent sc = new SimpleContent(this, "trazeo", 1);
        //String result = "";

        Log.d("MEDIAUPLOADERURL:", "MediaUploaderUrl: " + MediaUploader.URL);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String lat = "";
        String lon = "";
        if (location != null) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
        }

        String finishRideData[] = new String[12];
        finishRideData[0] = "id_ride";
        finishRideData[1] = myPrefs.id_ride().get();
        finishRideData[2] = "email";
        finishRideData[3] = myPrefs.email().get();
        finishRideData[4] = "pass";
        finishRideData[5] = myPrefs.pass().get();
        finishRideData[6] = "latitude";
        finishRideData[7] = lat + "";
        finishRideData[8] = "longitude";
        finishRideData[9] = lon + "";
        finishRideData[10] = "createat";
        finishRideData[11] = calculateTimestamp().toString();
        HttpPostHelper.send(MonitorActivity.this, finishRideData, "Final de paseo", myPrefs.url_api().get() + Var.URL_API_RIDE_FINISH);
        myPrefs.id_ride().put("-1");
        myPrefs.id_ride_monitor().put("-1");
        gps.stopGPS();

        /*String fdata = data;
        fdata += "&latitude=" + lat;
        fdata += "&longitude=" + lon;

        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_RIDE_FINISH, fdata);
            myPrefs.id_ride().put("-1");
            myPrefs.id_ride_monitor().put("-1");
            gps.stopGPS();
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }*/

        //Log.d("TEMA", result);
        gotoSelect();
    }

    @UiThread
    void showWaitDialog() {
        pdialog.setMessage("Espera...");
        pdialog.show();
    }

    @UiThread
    void gotoSelect() {
        pdialog.cancel();
        Toast.makeText(this, "Ok", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, InitActivity_.class));
        finish();
    }

    @Background
    void loadData() {
        SimpleContent sc = new SimpleContent(this, "trazeo", 3);
        String result = "";

        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_RIDE_DATA, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        final Type objectCPDRide = new TypeToken<MasterRide>() {
        }.getType();
        this.ride = new Gson().fromJson(result, objectCPDRide);

        showData();
    }

    @UiThread
    void showData() {
        gps = new GPS(this);

        // Creamos diccionario
        BindDictionary<EChild> dict = new BindDictionary<EChild>();
        dict.addStringField(R.id.titleCHILD,
                new StringExtractor<EChild>() {
                    @Override
                    public String getStringValue(EChild item, int position) {
                        return item.nick;
                    }
                }
        );

        // Creamos adaptador
        ChildAdapter adapter = new ChildAdapter(this,
                R.layout.child_item, this.ride.data.group.childs, this);

        // Asignamos el adaptador a la vista
        listChildren = (ListView) findViewById(R.id.listChildren);

        listChildren.setAdapter(adapter);

        listChildren.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final EChild echild = (EChild) adapterView.getItemAtPosition(position);
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkCHILD);
                checkbox.setChecked(!checkbox.isChecked());
                echild.setSelected(checkbox.isChecked());
                changeChild(echild);
                changeChildShow(echild);
            }
        });
        pdialog.cancel();
    }

    @Background
    public void changeChild(EChild echild) {
        String url = "";
        if (echild.isSelected()) {
            echild.setSelected(true);
            url = myPrefs.url_api().get() + Var.URL_API_CHILDIN;
        } else {
            echild.setSelected(false);
            url = myPrefs.url_api().get() + Var.URL_API_CHILDOUT;
        }

        Log.d("ChangeChild", "ChangeChild: " + url);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String lat = "";
        String lon = "";
        if (location != null) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
        }

        String childChangeData[] = new String[14];
        childChangeData[0] = "id_ride";
        childChangeData[1] = myPrefs.id_ride().get();
        childChangeData[2] = "email";
        childChangeData[3] = myPrefs.email().get();
        childChangeData[4] = "pass";
        childChangeData[5] = myPrefs.pass().get();
        childChangeData[6] = "id_child";
        childChangeData[7] = echild.id;
        childChangeData[8] = "latitude";
        childChangeData[9] = lat + "";
        childChangeData[10] = "longitude";
        childChangeData[11] = lon + "";
        childChangeData[12] = "createat";
        childChangeData[13] = calculateTimestamp().toString();

        if(gps.isFixed() && lat != "" && lon != "" ) {
            HttpPostHelper.send(MonitorActivity.this, childChangeData, "Evento vincular/desvincular niño", url);
            //changeChildShow(echild);
        } else {
            gps.addData(MonitorActivity.this, childChangeData, "Evento vincular/desvincular niño", url, echild);
            Log.d("DATALIST", "DATALIST: " + gps.dataList.size());
        }
    }

    public void changeChildShow(EChild echild) {
        String msg = "";
        if (echild.isSelected()) {
            msg = "(" + echild.nick + ")" + " Niño registrado en este Paseo";
        } else {
            msg = "(" + echild.nick + ")" + " Niño desvinculado del Paseo";
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.monitor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_close:
            case android.R.id.home:
                showCancelDialog();
                break;
            case R.id.action_disconnect:
                showDisconnectDialog();
                break;
            case R.id.action_report:
                showReportDialog();
                break;
        }
        return true;
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Background
    public void sendPoint(double lon, double lat){
        String sendPointData[] = new String[12];
        sendPointData[0] = "id_ride";
        sendPointData[1] = myPrefs.id_ride().get();
        sendPointData[2] = "email";
        sendPointData[3] = myPrefs.email().get();
        sendPointData[4] = "pass";
        sendPointData[5] = myPrefs.pass().get();
        sendPointData[6] = "latitude";
        sendPointData[7] = lat + "";
        sendPointData[8] = "longitude";
        sendPointData[9] = lon + "";
        sendPointData[10] = "createat";
        sendPointData[11] = calculateTimestamp().toString();
        HttpPostHelper.send(MonitorActivity.this, sendPointData, "Enviando posición", this.sendPointUrl);

        /*SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String sendPointData = this.data;
        sendPointData = sendPointData.concat("&latitude=" + lat);
        sendPointData = sendPointData.concat("&longitude=" + lon);
        Log.d("TEMA", "DATA_SENDPOINT: " + sendPointData);
        String result = "";
        try {
            result = sc.postUrlContent(this.sendPointUrl, sendPointData);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPD = new TypeToken<LastPoint>() {
        }.getType();
        LastPoint lastPoint = new Gson().fromJson(result, objectCPD);

        if (lastPoint != null && lastPoint.data != null) {
            myPrefs.end_ride().put(lastPoint.data.updated_at);
        }*/
    }

    @Override
    protected void onResume() {
        //if(gps != null) gps.resumeGPS();
        super.onResume();
    }

    @Override
    public void locationChanged(double longitude, double latitude) {
        Log.d("GPSMODULE", "Longitude: " + longitude);
        Log.d("GPSMODULE", "Latitude: " + latitude);

        sendPoint(longitude, latitude);
    }

    @Override
    public void gpsFirstFix(Location location){

        String latt = "";
        String lonn = "";

        for(int i = 0; i < gps.dataList.size(); i++){
            Context context = (Context) gps.dataList.get(i)[0];
            String[] data = (String[]) gps.dataList.get(i)[1];

            data[9] = location.getLatitude() + "";
            data[11] = location.getLongitude() + "";

            String description = (String) gps.dataList.get(i)[2];
            String url = (String) gps.dataList.get(i)[3];
            EChild echild = (EChild) gps.dataList.get(i)[4];

            HttpPostHelper.send(context, data, description, url);
        }
    }

    public Timestamp calculateTimestamp(){
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        
        long result = SelectGroupActivity.serverTimestamp.getTime() +
                (SelectGroupActivity.getCurrentTimestamp().getTime() - SelectGroupActivity.localTimestamp.getTime());
        timestamp.setTime(result);

        return timestamp;
    }
}
