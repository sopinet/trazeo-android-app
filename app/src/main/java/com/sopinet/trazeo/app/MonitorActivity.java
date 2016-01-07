package com.sopinet.trazeo.app;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.gpsmodule.LocationService;
import com.sopinet.trazeo.app.gson.EChild;
import com.sopinet.trazeo.app.gson.MasterRide;
import com.sopinet.trazeo.app.helpers.ChildAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_monitor)
public class MonitorActivity extends ActionBarActivity {

    @ViewById
    Button btnClose;

    @ViewById
    LinearLayout monitor_progress_dialog;

    @ViewById
    LinearLayout monitor_group;

    @ViewById
    Toolbar toolbar;

    @Extra
    String cancel;

    @Pref
    public MyPrefs_ myPrefs;

    @Extra
    String myProfileName;

    @Extra
    String groupId;

    Group group;

    @Extra
    long serverTimestamp;

    @Extra
    long localTimestamp;

    @StringRes
    String server_error;

    @StringRes
    String error_connection;

    @StringRes
    String error_connection_desc;

    @StringRes
    String server_error_exit;

    @StringRes
    String connection_error_exit;

    @StringRes
    String position_not_sended;

    SweetAlertDialog pDialog;

    @InstanceState
    MasterRide ride;

    ListView listChildren;

    @InstanceState
    int step;

    LocationService mService;
    boolean mBound = false;


    @AfterViews
    void init() {

        setSupportActionBar(toolbar);
        this.configureBar();

        group = Group.getGroupById(groupId);

        if (cancel != null && cancel.equals("1")) {
            showCancelDialog();
        }

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
        pDialog.setTitleText(getString(R.string.wait));
        pDialog.setCancelable(false);

        showProgressDialog(true);
        loadData();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCancelDialog();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra("groupId", groupId);
        startService(intent);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }

    };

    @UiThread
    void showCancelDialog() {
        if (NetHelper.isOnline(this)) {
            String message = getString(R.string.end_ride);

            if (mBound && !mService.isDataSended()) {
                message = getString(R.string.notFirstFix);
            }

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.are_you_sure))
                    .setContentText(message)
                    .setCancelText(getString(R.string.yes))
                    .setConfirmText(getString(R.string.no))
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            finishRide();
                            sDialog.dismiss();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                        }
                    })
                    .show();
        } else {
            showError(error_connection, false);
        }
    }

    @UiThread
    void showReportDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View customDialogView = factory.inflate(
                R.layout.dialog_report, (ViewGroup) findViewById(R.id.layout_root));

        final Dialog dialog = new Dialog(this, R.style.Theme_Dialog);
        dialog.setContentView(customDialogView);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        final EditText userInput = (EditText) dialog.findViewById(R.id.editREPORT);
        TextView reportOkButton = (TextView) dialog.findViewById(R.id.reportOkButton);

        reportOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userInput.getText().toString().trim().equals("")) {
                    dialog.cancel();
                    showWaitDialog();
                    sendReport(userInput.getText().toString());
                }
            }
        });
        TextView reportCancelButton = (TextView) dialog.findViewById(R.id.reportCancelButton);
        reportCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void sendReport(String text) {
        Location location = mService.getLocation();
        RequestParams params = new RequestParams();
        params.add("id_ride", group.ride_id);
        params.add("email", myPrefs.email().get());
        params.add("pass", myPrefs.pass().get());
        params.add("text", text);
        params.add("latitude", String.valueOf(location.getLatitude()));
        params.add("longitude", String.valueOf(location.getLongitude()));

        RestClient.post(RestClient.URL_API + RestClient.URL_API_SENDREPORT, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showReportOK();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                pDialog.dismiss();
                showError(error_connection_desc, false);
            }
        });
    }

    @UiThread
    void showReportOK() {
        pDialog.dismiss();
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.report_sended))
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    /**
     * Finaliza un paseo
     */
    public void finishRide() {

        showProgressDialog(true);
        String lat = "";
        String lon = "";
        if (mBound) {
            Location location = mService.getLocation();
            if (location != null) {
                lat = String.valueOf(location.getLatitude());
                lon = String.valueOf(location.getLongitude());
            }
            mService.stop();
        }

        // Enviamos solicitud de fin de PASEO en Background
        final RequestParams params = new RequestParams();
        params.put("id_ride", group.ride_id);
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("latitude", lat + "");
        params.put("longitude", lon + "");
        params.put("createat", calculateTimestamp().toString());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_RIDE_FINISH, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                gotoSelect();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                gotoSelect();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showErrorWithExit(connection_error_exit);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showErrorWithExit(server_error_exit);
            }
        });
    }

    @UiThread
    public void showProgressDialog(boolean isActive) {
        if (isActive) {
            monitor_group.setVisibility(View.GONE);
            monitor_progress_dialog.setVisibility(View.VISIBLE);
        } else {
            monitor_group.setVisibility(View.VISIBLE);
            monitor_progress_dialog.setVisibility(View.GONE);
        }
    }

    @UiThread
    void showWaitDialog() {
        pDialog.show();
    }

    @UiThread
    void gotoSelect() {
        Group myGroup = Group.getGroupById(group.id);
        myGroup.ride_id = "-1";
        myGroup.rideCreator = "";
        myGroup.save();
        Intent i = new Intent(this, SelectGroupActivity_.class);
        setResult(RESULT_OK, i);
        finish();
    }

    void loadData() {
        RequestParams params = new RequestParams();
        params.add("id_ride", group.ride_id);
        params.add("email", myPrefs.email().get());
        params.add("pass", myPrefs.pass().get());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_RIDE_DATA, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();
                Type objectCPDRide = new TypeToken<MasterRide>() {}.getType();
                try {
                    ride = new Gson().fromJson(result, objectCPDRide);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    ride = new MasterRide();
                }
                showData();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_connection_desc, true);
            }
        });
    }

    @UiThread
    void showData() {

        // Creamos adaptador
        ChildAdapter adapter = new ChildAdapter(this,
                R.layout.child_item, ride.data.group.childs);

        // Asignamos el adaptador a la vista
        listChildren = (ListView) findViewById(R.id.listChildren);
        listChildren.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                EChild echild = (EChild) adapterView.getItemAtPosition(position);
                CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkCHILD);
                checkbox.setChecked(!checkbox.isChecked());
                echild.setSelected(checkbox.isChecked());
                changeChild(echild);
            }
        });
        listChildren.setAdapter(adapter);

        showProgressDialog(false);

        if (myPrefs.rideTutorial().get()) {
            onCoachMark();
            myPrefs.rideTutorial().put(false);
        }
    }

    public void changeChild(EChild echild) {
        String url = RestClient.URL_API;
        if (echild.isSelected()) {
            echild.setSelected(true);
            url += RestClient.URL_API_CHILDIN;
        } else {
            echild.setSelected(false);
            url += RestClient.URL_API_CHILDOUT;
        }

        android.util.Log.d("ChangeChild", "ChangeChild: " + url);

        String lat = "";
        String lon = "";
        Location location = null;

        if (mBound) {
            location = mService.getLocation();
            if (location != null) {
                lat = String.valueOf(location.getLatitude());
                lon = String.valueOf(location.getLongitude());
            }
        }

        String childChangeData[] = new String[14];
        childChangeData[0] = "id_ride";
        childChangeData[1] = group.ride_id;
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

        if (location != null) {

            RequestParams params = new RequestParams();
            params.put("id_ride", group.ride_id);
            params.put("email", myPrefs.email().get());
            params.put("pass", myPrefs.pass().get());
            params.put("id_child", echild.id);
            params.put("latitude", lat + "");
            params.put("longitude", lon + "");
            params.put("createat", calculateTimestamp().toString());

            restClientChangeChild(url, params);
        // Si no hay conexión GPS, almacena los datos en una colección
        } else {
            if (mBound) {
                mService.addData(MonitorActivity.this, childChangeData, "Evento vincular/desvincular niño", url, echild);
            }
        }
    }



    private void restClientChangeChild(final String url, final RequestParams params) {
        RestClient.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Retry on 30 seg
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restClientChangeChild(url, params);
                    }
                }, 30000);
            }
        });
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
            case android.R.id.home:
                showCancelDialog();
                return true;
            case R.id.action_report:
                showReportDialog();
                return true;
            case R.id.action_chat:
                if (!group.id.equals("")) {
                    Intent i = new Intent(this, ChatActivity_.class);
                    i.putExtra("username", myProfileName);
                    i.putExtra("groupId", group.id);
                    startActivity(i);
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showCancelDialog();
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @UiThread
    public void showError(String message, final boolean back) {
        monitor_group.setVisibility(View.VISIBLE);
        monitor_progress_dialog.setVisibility(View.GONE);
        if (!isFinishing()) {
            new SweetAlertDialog(MonitorActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(error_connection)
                    .setContentText(message)
                    .setConfirmText(getString(R.string.accept_button))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            if (back) {
                                Intent i = new Intent(MonitorActivity.this, SelectGroupActivity_.class);
                                setResult(RESULT_CANCELED, i);
                            }
                            finish();
                        }
                    })
                    .show();
        }
    }

    @UiThread
    public void showErrorWithExit(String message) {
        new SweetAlertDialog(MonitorActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(error_connection)
                .setContentText(message)
                .setCancelText(getString(R.string.yes))
                .setConfirmText(getString(R.string.no))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        gotoSelect();
                        sDialog.dismiss();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        showProgressDialog(false);
                    }
                }).show();
    }

    private Timestamp calculateTimestamp() {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        try {
            long result = serverTimestamp + System.currentTimeMillis() - localTimestamp;
            timestamp.setTime(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    private void onCoachMark() {
        // Tutorial
        this.step = 0;
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.coach_step1);
        dialog.setCanceledOnTouchOutside(true);

        final Button masterView = (Button) dialog.findViewById(R.id.coach_ok_button_1);
        final TextView text = (TextView) dialog.findViewById(R.id.coach_text_1);
        final LinearLayout images = (LinearLayout) dialog.findViewById(R.id.images);
        final ImageView small_arrow = (ImageView) dialog.findViewById(R.id.coach_arrow_1);
        final ImageView coach_bell = (ImageView) dialog.findViewById(R.id.coach_bell);
        final ImageView coach_gps = (ImageView) dialog.findViewById(R.id.coach_gps);
        final ImageView end_arrow = (ImageView) dialog.findViewById(R.id.coach_arrow_end);

        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                step++;
                switch (step) {
                    case 1:
                        small_arrow.setVisibility(View.VISIBLE);
                        text.setText(getString(R.string.coach2));
                        break;
                    case 2:
                        small_arrow.setVisibility(View.GONE);
                        images.setGravity(Gravity.CENTER_HORIZONTAL);
                        coach_bell.setVisibility(View.VISIBLE);
                        coach_gps.setVisibility(View.VISIBLE);
                        text.setText(getString(R.string.coach3));
                        break;
                    case 3:
                        small_arrow.setVisibility(View.VISIBLE);
                        images.setGravity(Gravity.NO_GRAVITY);
                        coach_bell.setVisibility(View.GONE);
                        coach_gps.setVisibility(View.GONE);
                        text.setText(getString(R.string.coach4));
                        break;
                    case 4:
                        small_arrow.setVisibility(View.GONE);
                        images.setGravity(Gravity.LEFT);
                        end_arrow.setVisibility(View.VISIBLE);
                        text.setText(getString(R.string.coach5));
                        break;
                    case 5:
                        end_arrow.setVisibility(View.GONE);
                        text.setText(getString(R.string.coach6));
                        masterView.setText(getString(R.string.finish));
                        break;
                    case 6:
                        dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

}
