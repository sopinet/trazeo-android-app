package com.sopinet.trazeo.app;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.android.nethelper.StringHelper;
import com.sopinet.trazeo.app.gson.Cities;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.Location;
import com.sopinet.trazeo.app.gson.Locations;
import com.sopinet.trazeo.app.gson.MyProfile;
import com.sopinet.trazeo.app.gson.TimestampData;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


@EActivity(R.layout.activity_my_settings)
public class MySettingsActivity extends ActionBarActivity {

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    Toolbar toolbar;

    @Extra @InstanceState
    MyProfile myProfile;

    @ViewById
    LinearLayout mysettings_progress;

    @ViewById
    ScrollView edit_mysettings_form;

    @ViewById
    TextView mysettings_name;

    @ViewById
    TextView mysettings_phone;

    @ViewById
    CheckBox mysettings_ismonitor;

    @ViewById
    AutoCompleteTextView mysettings_locality;

    @StringRes
    String error_connection;

    @StringRes
    String server_error;

    @StringRes
    String error_loading;

    @StringRes
    String error_fill_data;

    @StringRes
    String error_title;

    @StringRes
    String access_denied;

    @StringRes
    String access_denied_location;

    @StringRes
    String passwd_changed_error;

    @Extra
    Groups groups;

    @Extra
    TimestampData timestampData;

    @Extra
    Cities cities;

    @Extra
    Cities catalogCities;

    SweetAlertDialog pDialog;

    String result;

    Locations locations;
    ArrayList<String> locationsString;
    ArrayAdapter<String> locationsAdapter;

    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        configureBar();
        // No es un nuevo usuario
        if (myPrefs.new_user().get() != 1) {
            fillData();
        } else {
            buildHelpDialog();
        }
        result = "";
        this.locationsString = new ArrayList<>();
        mysettings_locality.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() > 3)
                    getLocalities(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getLocalities(CharSequence q) {
        String loc = q.toString();

        RequestParams params = new RequestParams();
        params.put("q", loc.replace(" ", "+"));
        params.put("app", "true");

        RestClient.get(RestClient.URL_API + RestClient.URL_API_GET_LOCALIONS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();
                final Type objectCPD = new TypeToken<Locations>() {}.getType();
                locations = new Gson().fromJson(result, objectCPD);
                showLocalities();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }

        });
    }

    @UiThread
    void showLocalities() {
        for (Location location : this.locations.data) {
            this.locationsString.add(location.nameUtf8);
        }

        ArrayList<String> locationsStringCopy = new ArrayList<>();
        locationsStringCopy.addAll(locationsString);

        locationsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, locationsStringCopy);
        mysettings_locality.setAdapter(locationsAdapter);

        this.locationsString.clear();
    }

    @Click(R.id.btnEditSettings)
    public void btnEditSettingsClicked() {
        if (mysettings_name.getText().toString().trim().equals("") ||
        mysettings_locality.getText().toString().trim().equals("") ||
        mysettings_phone.getText().toString().trim().equals("")) {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(error_title)
                    .setContentText(error_fill_data)
                    .setConfirmText("Aceptar")
                    .show();
        } else {
            changeProfile(mysettings_locality.getText().toString(),
                    mysettings_name.getText().toString(),
                    mysettings_phone.getText().toString());
        }

    }

    @Click(R.id.btnChangePassword)
    public void btnChangePasswordClicked() {
        showChangePasswordDialog();
    }

    private void showChangePasswordDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View customDialogView = factory.inflate(
                R.layout.dialog_password_change, (ViewGroup) findViewById(R.id.layout_root));

        final Dialog dialog = new Dialog(this, R.style.Theme_Dialog);
        dialog.setContentView(customDialogView);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        final EditText passwd = (EditText) dialog.findViewById(R.id.mysettings_passwd);
        final EditText passwdConfirm = (EditText) dialog.findViewById(R.id.mysettings_confirm_passwd);

        TextView passwdOkButton = (TextView) dialog.findViewById(R.id.passwdOkButton);
        passwdOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!passwd.getText().toString().trim().equals("") &&
                        !passwdConfirm.getText().toString().trim().equals("")) {
                    if (!passwd.getText().toString().equals(passwdConfirm.getText().toString())) {
                        dialog.dismiss();
                        new SweetAlertDialog(dialog.getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.error_title))
                                .setContentText(getString(R.string.error_invalid_passwd))
                                .setConfirmText(getString(R.string.accept_button))
                                .show();
                    } else {
                        dialog.cancel();
                        SweetAlertDialog pDialog = new SweetAlertDialog(dialog.getContext(), SweetAlertDialog.PROGRESS_TYPE);
                        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
                        pDialog.setTitleText(getString(R.string.sending_changes));
                        pDialog.setCancelable(false);
                        changePassword(passwd.getText().toString());
                    }
                }
            }
        });

        TextView passwdCancelButton = (TextView) dialog.findViewById(R.id.passwdCancelButton);
        passwdCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changeProfile(String city, String name, String phone) {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
        pDialog.setTitleText("Espera un momento");
        pDialog.setCancelable(false);
        pDialog.show();

        RequestParams params = new RequestParams();
        params.put("email",myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("city", city);
        params.put("name", name);
        params.put("phone", phone);
        params.put("useLike", mysettings_ismonitor.isChecked() ? "monitor" : "user");

        RestClient.post(RestClient.URL_API + RestClient.URL_API_CHANGE_PROFILE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    result = response.getString("state");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showResult(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_connection, error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(error_connection, server_error);
            }

        });
    }
    @UiThread
    void showResult(String state) {
        pDialog.dismiss();
        if (state.equals("1")) {
            myProfile.data.name = mysettings_name.getText().toString();
            myProfile.data.mobile = mysettings_phone.getText().toString();
            myProfile.data.city.name_ascii = mysettings_locality.getText().toString();
            myProfile.data.use_like =  mysettings_ismonitor.isChecked() ? "monitor" : "user";

            if (myPrefs.new_user().get() != 1) {
                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(getString(R.string.data_changed))
                        .setConfirmText(getString(R.string.accept_button))
                        .show();
            } else {
                Intent i;
                if (mysettings_ismonitor.isChecked()) {
                    i = new Intent(this, SelectGroupActivity_.class);
                    myPrefs.new_user().put(3);
                } else {
                    i = new Intent(this, ChildrenActivity_.class);
                    myPrefs.new_user().put(2);
                }
                i.putExtra("groups", groups);
                i.putExtra("myProfile", myProfile);
                i.putExtra("timestampData", timestampData);
                i.putExtra("cities", cities);
                i.putExtra("catalogCities", catalogCities);
                startActivity(i);
                finish();
            }
        } else {
            showError(access_denied, access_denied_location);
        }
    }

    private void changePassword(final String newPassword) {
        RequestParams params = new RequestParams();
        params.put("email",myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("newPassword", StringHelper.md5(newPassword));

        RestClient.post(RestClient.URL_API + RestClient.URL_API_CHANGE_PASSWORD, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String msg = "";
                try {
                    msg = response.getString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (msg.equals("-1")) {
                    showError(access_denied, passwd_changed_error);
                } else {
                    myPrefs.pass().put(StringHelper.md5(newPassword));
                    showChangePasswordSuccess();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_connection, error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(error_connection, server_error);
            }
        });
    }

    @UiThread
    public void showChangePasswordSuccess() {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.change_passwd_ok))
                .setConfirmText(getString(R.string.accept_button))
                .show();
    }

    private void fillData() {
        mysettings_name.setText(myProfile.data.name);
        mysettings_locality.setText(myProfile.data.city.name_ascii);
        mysettings_phone.setText(myProfile.data.mobile);

        try {
            mysettings_ismonitor.setChecked(myProfile.data.use_like.equals("monitor"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildHelpDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.my_settings_desc))
                .setConfirmText(getString(R.string.understood))
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    @UiThread
    public void showError(String title, String messageError) {
        pDialog.dismiss();
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(messageError)
                .setConfirmText("Aceptar")
                .show();
    }

    @Override
    public void onBackPressed() {
        if (myPrefs.new_user().get() == 1) {
            Intent i = new Intent(this, TutorialActivity_.class);
            i.putExtra("groups", groups);
            i.putExtra("myProfile", myProfile);
            i.putExtra("timestampData", timestampData);
            i.putExtra("cities", cities);
            i.putExtra("catalogCities", catalogCities);
            startActivity(i);
        } else {
            Intent i = new Intent(this, SelectGroupActivity_.class);
            if (result.equals("1")) {
                i.putExtra("myprofile", myProfile);
                setResult(RESULT_OK, i);
            } else {
                setResult(RESULT_CANCELED, i);
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (myPrefs.new_user().get() != 1) {
            getMenuInflater().inflate(R.menu.mysettings, menu);
        }
       return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.mysettings_help) {
            buildHelpDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
