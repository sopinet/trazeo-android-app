package com.sopinet.trazeo.app;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;

@EActivity(R.layout.activity_other_services)
public class OtherServicesActivity extends ActionBarActivity {

    @Pref
    MyPrefs_ myPrefs;

    @StringRes
    String error_connection;

    @StringRes
    String access_denied;

    @StringRes
    String server_error;

    @StringRes
    String error_loading;

    @ViewById
    Toolbar toolbar;

    @ViewById
    CheckBox cbYes;

    @ViewById
    CheckBox cbNo;

    SweetAlertDialog pDialog;


    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        configureBar();
        if (myPrefs.civiclubNotifications().get().equals("yes")) {
            cbYes.setChecked(true);
            cbNo.setChecked(false);
        } else {
            cbYes.setChecked(false);
            cbNo.setChecked(true);
        }
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Click(R.id.cbYes)
    void cbYesClicked() {
        cbYes.setChecked(true);
        cbNo.setChecked(false);
    }

    @Click(R.id.cbNo)
    void cbNoClicked() {
        cbYes.setChecked(false);
        cbNo.setChecked(true);
    }

    @Click(R.id.otherservices_change_apply)
    void otherservicesChangeApplyClicked() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
        pDialog.setTitleText("Espera un momento");
        pDialog.setCancelable(false);
        pDialog.show();

        final String civiclub_conexion_value= cbYes.isChecked() ? "yes" : "no";

        RequestParams params = new RequestParams();
        params.put("email",myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("email_notification_id", myPrefs.notificationsId().get());
        params.put("email_notification_value", myPrefs.notifications().get());
        params.put("civiclub_conexion_id", myPrefs.civiclubNotificationsId().get());
        params.put("civiclub_conexion_value", cbYes.isChecked() ? "yes" : "no");

        RestClient.post(RestClient.URL_API + RestClient.URL_API_CHANGE_NOTIFICATIONS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = "";
                try {
                    result = response.getString("state");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                myPrefs.notifications().put(civiclub_conexion_value);
                showResult(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @UiThread
    void showResult(String state){
        pDialog.dismiss();
        if (state.equals("1")) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.data_changed))
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
        } else {
            showError(access_denied);
        }
    }

    @UiThread
    public void showError(final String messageError) {
        pDialog.dismiss();
        new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText(error_connection)
                .setContentText(messageError)
                .setConfirmText("Aceptar")
                .show();
    }

    private void buildHelpDialog() {
       new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.other_services_desc))
                .setConfirmText(getString(R.string.understood))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.other_services, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.help:
                buildHelpDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
