package com.sopinet.trazeo.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.trazeo.app.gson.EditGroup;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.gson.Location;
import com.sopinet.trazeo.app.gson.Locations;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_edit_group)
public class EditGroupActivity extends AppCompatActivity{

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    Toolbar toolbar;

    @Extra
    Group group;

    @ViewById
    AutoCompleteTextView group_name;

    @ViewById
    AutoCompleteTextView locality;

    @ViewById
    Spinner visibilitySpinner;

    @ViewById
    AutoCompleteTextView school;

    SweetAlertDialog pDialog;

    Locations locations;
    ArrayList<String> locationsString;

    ArrayAdapter<String> locationsAdapter;

    @AfterViews
    void init(){
        setSupportActionBar(toolbar);

        configureBar();

        this.group_name.setText(group.name);
        this.locality.setText(group.city);
        this.school.setText(group.school);
        int visibility;
        try {
           visibility = Integer.parseInt(group.visibility);
        } catch (NumberFormatException e) {
            visibility = 0;
        }
        this.visibilitySpinner.setSelection(visibility);
        this.locationsString = new ArrayList<>();

        locality.addTextChangedListener(new TextWatcher() {
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

    void getLocalities(CharSequence q) {
        String loc = q.toString();

        RequestParams params = new RequestParams();
        params.put("q", loc.replace(" ", "+"));
        params.put("app", "true");

        RestClient.get(RestClient.URL_API + RestClient.URL_API_GET_LOCALIONS, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                final Type objectCPD = new TypeToken<Locations>() {
                }.getType();
                locations = gson.fromJson(result, objectCPD);
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
        locality.setAdapter(locationsAdapter);

        this.locationsString.clear();
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Click(R.id.btnEditGroup)
    public void btnEditGroupClick(){
        if(NetHelper.isOnline(this)) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
            pDialog.setTitleText("Espera");
            pDialog.setCancelable(false);
            pDialog.show();
            sendEditGroup();
        } else {
            showError();
        }
    }

    void sendEditGroup(){
        RequestParams params = new RequestParams();
        params.put("email", myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("name", group_name.getText().toString());
        params.put("country", "spain");
        params.put("city", locality.getText().toString());
        params.put("visibility", visibilitySpinner.getSelectedItemPosition());
        params.put("school1", school.getText().toString());
        params.put("id_group", group.id);

        RestClient.post(RestClient.URL_API + RestClient.URL_API_MANAGE_GROUP, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                android.util.Log.d("EDIT", "EDIT GROUP: " + response.toString());
                Gson gson = new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
                final Type objectCPD = new TypeToken<EditGroup>() {}.getType();
                EditGroup edit_group = gson.fromJson(response.toString(), objectCPD);

                Group group = Group.getGroupById(edit_group.data.id);

                if(group != null) {
                    Group noticeGroup = Group.load(Group.class, group.getId());
                    noticeGroup.name = group_name.getText().toString();
                    noticeGroup.save();
                }

                showResult(edit_group);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                pDialog.dismiss();
                showError();
                //com.github.snowdream.android.util.Log.d("ERROR", "Sin conexión de datos.");
            }
        });
    }

    @UiThread
    void showResult(EditGroup edit_group){
        pDialog.dismiss();
        if (edit_group.state.equals("1")) {
            Intent i = new Intent(this, SelectGroupActivity_.class);
            setResult(RESULT_OK, i);
            finish();
        } else if (edit_group.msg.equals("Name is already in use")){
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_title))
                    .setContentText(getString(R.string.name_used))
                    .setConfirmText("Aceptar")
                    .show();
        } else {
           showError();
        }
    }

    private void buildHelpDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.info_edit_group))
                .setConfirmText("Entendido")
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    private void showError() {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_title))
                .setContentText(getString(R.string.error_connection))
                .setConfirmText("Aceptar")
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.help:
                buildHelpDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
