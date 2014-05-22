package com.sopinet.trazeo.app;

import java.lang.reflect.Type;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.MasterRide;
import com.sopinet.trazeo.app.gson.MasterWall;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;
import com.sopinet.trazeo.app.osmlocpull.OsmLocPullReceiver;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

@EActivity(R.layout.activity_monitor)
public class MonitorActivity extends ActionBarActivity implements ISimpleDialogListener, ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Extra
    String cancel;

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
    */

    @Pref
    MyPrefs_ myPrefs;

    public static MasterRide ride;
    public static MasterWall wall;

    String data = null;
    String data_wall = null;

    SimpleDialogFragment wait;

    ProgressDialog pdialog;

    int commentCount;
    boolean firstLoad = true;

    @Extra
    boolean fromComment;

    public static NotificationManager notificationManager = null;
    public static NotificationCompat.Builder mBuilder = null;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            checkNewComments();
        }
    };

    @AfterViews
    void init() {
        this.configureBar();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(201);

        data = "id_ride=" + myPrefs.id_ride().get() + "&email=" + myPrefs.email().get() + "&pass=" + myPrefs.pass().get();
        data_wall = "id_group=" + myPrefs.id_group().get() + "&email=" + myPrefs.email().get() + "&pass=" + myPrefs.pass().get();

        if (cancel != null && cancel.equals("1")) {
            showCancelDialog();
        }
        pdialog = new ProgressDialog(this);
        pdialog.setCancelable(false);
        pdialog.setMessage("Cargando...");
        pdialog.show();

        loadData();
    }


    void showNewCommentDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Trazeo: Escribir nuevo comentario")
                .setView(input)
                .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String comment = input.getText().toString();
                        if(!comment.equals("")) {
                            Toast.makeText(getBaseContext(), "Enviando comentario...", Toast.LENGTH_LONG).show();
                            createComment(comment);
                        }else {
                            Toast.makeText(getBaseContext(), "El mensaje no puede estar vacío", Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    @Background
    void createComment(String comment) {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String sdata = data = "id_group=" + myPrefs.id_group().get() + "&email=" + myPrefs.email().get() + "&pass=" + myPrefs.pass().get() + "&text=" + comment;
        String result = "";
        try {
            result = sc.postUrlContent(Var.URL_API_WALL_NEW, sdata);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }
        commentOk();
    }

    @UiThread
    void commentOk(){
        Intent i = new Intent(this, MonitorActivity_.class);
        i.putExtra("cancel", cancel);
        startActivity(i);
    }

    @UiThread
    void showCancelDialog() {
        SimpleDialogFragment wsure = (SimpleDialogFragment) SimpleDialogFragment.createBuilder(
                this,
                getSupportFragmentManager()).setTitle("Trazeo: Terminar paseo")
                .setMessage("Se terminará su paseo actual, ¿está seguro?")
                .setPositiveButtonText("Sí").setRequestCode(1)
                .setNegativeButtonText("No").setRequestCode(1)
                .show();
    }

    @UiThread
    void showDisconnectDialog() {
        SimpleDialogFragment wsure = (SimpleDialogFragment) SimpleDialogFragment.createBuilder(
                this,
                getSupportFragmentManager()).setTitle("Trazeo: Desconectar")
                .setMessage("Se terminará su paseo actual y desconectará su usuario de la aplicación, ¿está seguro?")
                .setPositiveButtonText("Sí").setRequestCode(2)
                .setNegativeButtonText("No").setRequestCode(2)
                .show();
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
                                // get user input and set it to result
                                // edit text
                                //result.setText(userInput.getText());
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
            result = sc.postUrlContent(Var.URL_API_SENDREPORT, sdata);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }
        showReportOK();
    }

    @UiThread
    void showReportOK() {
        wait.dismiss();
        Toast.makeText(this, "El reporte ha sido enviado con éxito.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        if (requestCode == 1 || requestCode == 2) {
            // Mensaje de inicio de detención de PASEO
            showWaitDialog();

            // stopService(SelectGroupActivity.intentGPS);  // TODO: No estoy seguro de que esto sea necesario

            // Cancelamos la ALARMA
            String data_service = "email=" + myPrefs.email().get();
            data_service += "&pass=" + myPrefs.pass().get();
            data_service += "&id_ride=" + myPrefs.id_ride().get();

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(this, OsmLocPullReceiver.class);
            i.putExtra("url", Var.URL_API_SENDPOSITION);
            i.putExtra("data", data_service);

            PendingIntent pi;
            pi = PendingIntent.getBroadcast(this, 0, i, FLAG_UPDATE_CURRENT);
            am.cancel(pi);

            // Eliminamos las ficaciones
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(200);
            notificationManager.cancel(201);

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
        handler.removeCallbacksAndMessages(null);
        SimpleContent sc = new SimpleContent(this, "trazeo", 1);
        String result = "";

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String lat = "";
        String lon = "";
        if (location != null) {
            lat = String.valueOf(location.getLatitude());
            lon = String.valueOf(location.getLongitude());
        }

        String fdata = data;
        fdata += "&latitude=" + lat;
        fdata += "&longitude=" + lon;

        try {
            result = sc.postUrlContent(Var.URL_API_RIDE_FINISH, fdata);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }
        Log.d("TEMA", result);

        gotoSelect();
    }

    @UiThread
    void showWaitDialog() {
        wait = (SimpleDialogFragment) SimpleDialogFragment.createBuilder(this, getSupportFragmentManager()).hideDefaultButton(true).setMessage("Espere...").show();
    }

    @UiThread
    void gotoSelect() {
        Toast.makeText(this, "Ok", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, InitActivity_.class));
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        //
        // this.cancel();
    }

    @Background
    void loadData() {
        SimpleContent sc = new SimpleContent(this, "trazeo", 3);
        String result = "";
        //String result_wall = "";

        try {
            result = sc.postUrlContent(Var.URL_API_RIDE_DATA, data);
            //result_wall = sc.postUrlContent(Var.URL_API_WALL, data_wall);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPDRide = new TypeToken<MasterRide>() {
        }.getType();
        //final Type objectCPDWall = new TypeToken<MasterWall>() {
        //}.getType();
        MonitorActivity.ride = new Gson().fromJson(result, objectCPDRide);
        //MonitorActivity.wall = new Gson().fromJson(result_wall, objectCPDWall);


        //myPrefs.id_ride().put(createRide.data.id_ride);
        showData();
    }

    @UiThread
    void showData() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }

        if(fromComment)
            mViewPager.setCurrentItem(2);

        checkNewComments();
    }

    @Background
    public void checkNewComments(){
        SimpleContent sc = new SimpleContent(this, "trazeo", 3);
        String result_wall = "";

        try {
            result_wall = sc.postUrlContent(Var.URL_API_WALL, data_wall);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPDWall = new TypeToken<MasterWall>() {
        }.getType();

        MonitorActivity.wall = new Gson().fromJson(result_wall, objectCPDWall);
        showNotification();
    }

    @UiThread
    public void showNotification(){
        pdialog.cancel();
        if(!firstLoad){
            if(MonitorActivity.wall.data.size() > commentCount)
                createNotification(this);
        }

        commentCount = MonitorActivity.wall.data.size();

        if(firstLoad)
            firstLoad = false;

        handler.postDelayed(runnable, 5000);
    }

    public void createNotification(Context context)
    {
        mBuilder = new NotificationCompat.Builder(context);

        TaskStackBuilder stackBuilder_go = TaskStackBuilder.create(context);

        // TODO: Estas clases podrían ser dinámicas
        stackBuilder_go.addParentStack(MonitorActivity_.class);
        Intent intent_go = new Intent(context, MonitorActivity_.class);
        intent_go.putExtra("fromComment", true);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        mBuilder.setContentText("Nuevo comentario de grupo");

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(201, mBuilder.build());
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
            case R.id.action_new:
                showNewCommentDialog();
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                Log.d("GPSLOG", "MonitorActivity, sendtofrag: " + data);
                return MonitorMapFragment.newInstance(data);
            } else if (position == 1) {
                return MonitorChildFragment.newInstance(data);
            } else if (position == 2) {
                return MonitorWallFragment.newInstance(data);
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_map).toUpperCase(l);
                case 1:
                    return getString(R.string.title_fragment_children).toUpperCase(l);
                case 2:
                    return getString(R.string.title_fragment_wall).toUpperCase(l);
            }
            return null;
        }
    }
}
