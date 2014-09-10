package com.sopinet.trazeo.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.extractors.StringExtractor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.MinimalJSON;
import com.sopinet.android.nethelper.SimpleContent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import com.sopinet.trazeo.app.gson.CreateRide;
import com.sopinet.trazeo.app.gson.Group;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.TimestampData;
import com.sopinet.trazeo.app.helpers.GroupAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.segment.android.Analytics;
import io.segment.android.models.Props;

@EActivity(R.layout.activity_select_group)
public class SelectGroupActivity extends ActionBarActivity{
    @Pref
    MyPrefs_ myPrefs;

    Groups groups;

    @Extra
    boolean firstGroup;

    @Extra
    boolean firstFinish;

    @ViewById
    ListView listSelectGroup;

    @ViewById
    TextView btnSearch;

    @ViewById
    LinearLayout groups_layout;

    @ViewById
    LinearLayout no_groups_layout;

    @ViewById
    TextView btnBegin;

    Menu mDynamicMenu;

    public static NotificationManager notificationManager = null;
    public static NotificationCompat.Builder mBuilder = null;

    public TimestampData timestampData;
    public static Timestamp serverTimestamp;
    public static Timestamp localTimestamp;

    @AfterViews
    void init() {
        this.localTimestamp = getCurrentTimestamp();

        Analytics.onCreate(this);

        try {
            if (firstGroup)
                onCoachMark(false);
            if(firstFinish)
                onCoachMark(true);
        } catch(Exception e) {
            e.printStackTrace();
            firstGroup = false;
            firstFinish = false;
        }

        btnSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnBegin.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);

        loadData();
        final LocationManager manager = (LocationManager) getSystemService( this.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
            buildAlertMessageNoGps();
        Analytics.track("Groups List In - Android", new Props("email", myPrefs.email().get()));
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

        this.groups = new Gson().fromJson(result, objectCPD);

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

        if(groups.data.size() > 0) {
            getMenuInflater().inflate(R.menu.select_group, mDynamicMenu);
            groups_layout.setVisibility(View.VISIBLE);
            no_groups_layout.setVisibility(View.GONE);
            BindDictionary<Group> dict = new BindDictionary<Group>();
            dict.addStringField(R.id.name,
                    new StringExtractor<Group>() {

                        @Override
                        public String getStringValue(Group group, int position) {
                            return group.name;
                        }
                    });

            GroupAdapter adapter = new GroupAdapter(this, R.layout.group_list_item, groups.data);

            listSelectGroup.setAdapter(adapter);
        } else {
            getMenuInflater().inflate(R.menu.select_no_group, mDynamicMenu);
            groups_layout.setVisibility(View.GONE);
            no_groups_layout.setVisibility(View.VISIBLE);
        }

        //Toast.makeText(this, "Espere unos segundos mientras cargamos los datos de la Ruta...", Toast.LENGTH_LONG).show();
    }


    @Background
    public void createRide(String l, String hasride) {

        String[] groupsIds = new String[6]; // Aqui guardo los ids de los grupos en los que hay que comprobar acceso.
        groupsIds[0] = "63";
        groupsIds[1] = "21";
        groupsIds[2] = "20";
        groupsIds[3] = "19";
        groupsIds[4] = "18";
        groupsIds[5] = "37";

        String[] emailsToFilter = new String[8]; // Aqui guardo los emails de los usuarios que pueden iniciar paseo en los grupos guardados en 'groupsIds'
        emailsToFilter[0] = "prudennl92@gmail.com";
        emailsToFilter[1] = "fermincabal94@gmail.com";
        emailsToFilter[2] = "victornogpan@gmail.com";
        emailsToFilter[3] = "clsouton@gmail.com";
        emailsToFilter[4] = "laura.alberquilla@gmail.com";
        emailsToFilter[5] = "lrodrigosanchez@gmail.com";
        emailsToFilter[6] = "gemi87.jg@gmail.com";
        emailsToFilter[7] = "elenacarrie@gmail.com";

        boolean canInitRide = true;

        for (String groupsId : groupsIds) {
            if (l.equals(groupsId)) {
                canInitRide = false;

                for (String anEmailsToFilter : emailsToFilter) {
                    if (myPrefs.email().get().equals(anEmailsToFilter))
                        canInitRide = true;
                }
            }
        }

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
        goGroup(result, hasride, canInitRide);
    }

    @UiThread
    void goGroup(String result, String hasride, boolean canInitRide){
        Log.d("TEMA", result);

        final Type objectCPD = new TypeToken<CreateRide>(){}.getType();
        CreateRide createRide = new Gson().fromJson(result, objectCPD);

        myPrefs.id_ride().put(createRide.data.id_ride);

        if (hasride.equals("true")) {
            goActivitySee();
        } else {
            if(canInitRide) {
                myPrefs.id_ride_monitor().put(createRide.data.id_ride);
                goActivityMonitor(true);
            } else {
                buildCantInitRideDialog();
            }
        }
    }

    void goActivityMonitor(Boolean isNew) {
        Intent i = new Intent(SelectGroupActivity.this, MonitorActivity_.class);
        i.putExtra("firstRide", firstGroup);
        startActivity(i);
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

    @Click(R.id.btnSearch)
    public void btnSearchClick(){
        startActivity(new Intent(this, SearchGroupsActivity_.class));
    }

    @Click(R.id.btnBegin)
    public void btnBeginClick() {
        startActivity(new Intent(this, AddChildrenGuide_.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mDynamicMenu = menu;
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
        } else if (id == R.id.refresh) {
            refreshGroups();
        } else if (id == R.id.new_group) {
            goNewGroup();
        } else if (id == R.id.help) {
            buildHelpDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goNewGroup(){
        startActivity(new Intent(this, NewGroupActivity_.class));
        finish();
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

    private void buildCantInitRideDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No puedes iniciar este paseo como monitor")
                .setCancelable(false)
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildHelpDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Los grupos a los que estáis vinculados tus hijos y tú aparecen aquí. Cuando quieras iniciar el paseo para ese grupo, pulsa sobre él. Te aparecerá el listado de todos los niños que están dados de alta y podrás seleccionar a los que se vayan uniendo ese día al paseo, que son los que recibirán puntos por participar y cuyos padres serán notificados cuando los marques, desmarques y cuando llegues al destino.")
                .setCancelable(false)
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.dismiss();
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

    public void onCoachMark(boolean firstFinish){
        // Tutorial
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.coach_mark);
        dialog.setCanceledOnTouchOutside(true);
        //for dismissing anywhere you touch
        View masterView = dialog.findViewById(R.id.coach_ok_button);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        if(firstFinish){
            ImageView image = (ImageView) dialog.findViewById(R.id.coach_arrow);
            image.setVisibility(View.GONE);
            TextView text = (TextView) dialog.findViewById(R.id.coach_text);
            text.setText(Html.fromHtml(getString(R.string.firstFinish)));
            text.setMovementMethod(LinkMovementMethod.getInstance());
        }
        dialog.show();
    }

    public void showInviteDialog(final String id) {
        final Dialog shareDialog;
        shareDialog = new Dialog(SelectGroupActivity.this);
        shareDialog.setTitle("Invitaciones");
        shareDialog.setContentView(R.layout.share_dialog);
        shareDialog.setCancelable(true);
        shareDialog.show();

        final EditText etEmail = (EditText) shareDialog.findViewById(R.id.etEmail);
        Button cancelBtn = (Button) shareDialog.findViewById(R.id.cancelBtn);
        Button confirmBtn = (Button) shareDialog.findViewById(R.id.confirmBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDialog.dismiss();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etEmail.getText().toString().equals("")) {
                    Toast.makeText(SelectGroupActivity.this, "Debes escribir una dirección de email", Toast.LENGTH_LONG).show();
                } else {
                    sendInvite(id, etEmail.getText().toString());
                }
            }
        });
    }

    @Background
    void sendInvite(String id, String email_invite) {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email="+myPrefs.email().get();
        data += "&pass="+myPrefs.pass().get();
        data += "&id_group="+id;
        data += "&email_invite=" + email_invite;
        String result = "";

        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_INVITE, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        showInviteResult();
    }

    @UiThread
    void showInviteResult() {
        Toast.makeText(this, "La invitación se ha enviado correctamente", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Analytics.activityStart(this);
    }

    @Override
    protected void onPause() {
        Analytics.activityPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.activityResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.activityStop(this);
    }
}