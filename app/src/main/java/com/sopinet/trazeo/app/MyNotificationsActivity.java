package com.sopinet.trazeo.app;

import android.support.v7.app.AppCompatActivity;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_my_notifications)
public class MyNotificationsActivity extends AppCompatActivity {


    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    Toolbar toolbar;

    @ViewById
    CheckBox cbOption1;

    @ViewById
    CheckBox cbOption2;

    @ViewById
    CheckBox cbOption3;

    @ViewById
    CheckBox cbMobileOption1;

    @ViewById
    CheckBox cbMobileOption2;

    @ViewById
    CheckBox cbMobileOption3;

    @StringRes
    String access_denied;

    @StringRes
    String server_error;

    @StringRes
    String error_connection;

    @StringRes
    String error_loading;

    SweetAlertDialog pDialog;

    String notification_value;

    public static final String NOW = "now";
    public static final String IMPORTANT = "important";
    public static final String NEVER = "never";


    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        configureBar();
        switch (myPrefs.notifications().get()) {
            case NEVER:
                setCheckedNever();
                break;
            case IMPORTANT:
                setCheckImportant();
                break;
            case NOW:
                setCheckedNow();
                break;
        }
        cbMobileOption1.setChecked(myPrefs.RaidInOutNotification().get());
        cbMobileOption2.setChecked(myPrefs.childInOutNotification().get());
        cbMobileOption3.setChecked(myPrefs.chatsNotification().get());
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setCheckedNever() {
        cbOption1.setChecked(true);
        cbOption2.setChecked(false);
        cbOption3.setChecked(false);
        notification_value = NEVER;
    }

    private void setCheckImportant() {
        cbOption1.setChecked(false);
        cbOption2.setChecked(true);
        cbOption3.setChecked(false);
        notification_value = IMPORTANT;
    }

    private void setCheckedNow() {
        cbOption1.setChecked(false);
        cbOption2.setChecked(false);
        cbOption3.setChecked(true);
        notification_value = NOW;
    }

    @Click(R.id.cbOption1)
    void cbOption1Clicked() {
        setCheckedNever();
    }

    @Click(R.id.cbOption2)
    void cbOption2Clicked() {
        setCheckImportant();
    }

    @Click(R.id.cbOption3)
    void cbOption3Clicked() {
        setCheckedNow();
    }

    @Click(R.id.cbMobileOption1)
    void cbcbMobileOption1Clicked() {
        myPrefs.RaidInOutNotification().put(cbMobileOption1.isChecked());
    }

    @Click(R.id.cbMobileOption2)
    void cbcbMobileOption2Clicked() {
        myPrefs.childInOutNotification().put(cbMobileOption2.isChecked());
    }

    @Click(R.id.cbMobileOption3)
    void cbcbMobileOption3Clicked() {
        myPrefs.chatsNotification().put(cbMobileOption3.isChecked());
    }

    @Click(R.id.notification_change_apply)
    void otherservicesChangeApplyClicked() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
        pDialog.setTitleText("Espera un momento");
        pDialog.setCancelable(false);
        pDialog.show();

        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("email_notification_id", myPrefs.notificationsId().get());
        params.put("email_notification_value", notification_value);
        params.put("civiclub_conexion_id", myPrefs.civiclubNotificationsId().get());
        params.put("civiclub_conexion_value", myPrefs.civiclubNotifications().get());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_CHANGE_NOTIFICATIONS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = "";
                try {
                    result = response.getString("state");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                myPrefs.notifications().put(notification_value);
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
    void showResult(String state) {
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
    public void showError(String messageError) {
        pDialog.dismiss();
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(error_connection)
                .setContentText(messageError)
                .setConfirmText(getString(R.string.accept_button))
                .show();

    }

    private void buildHelpDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.info_my_notifications))
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
