package com.sopinet.trazeo.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.chat.ChatCreator;
import com.sopinet.trazeo.app.gson.EditGroup;
import com.sopinet.trazeo.app.chat.model.Group;
import com.sopinet.trazeo.app.gson.Location;
import com.sopinet.trazeo.app.gson.Locations;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.http.Header;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

@EActivity(R.layout.activity_new_group)
public class NewGroupActivity extends ActionBarActivity{

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    Toolbar toolbar;

    @ViewById
    AutoCompleteTextView new_group_name;

    @ViewById
    AutoCompleteTextView locality;

    @ViewById
    Spinner visibilitySpinner;

    @ViewById
    AutoCompleteTextView school;

    @StringRes
    String error_loading;

    @StringRes
    String error_connection;

    @StringRes
    String server_error;

    SweetAlertDialog pDialog;

    Locations locations;
    ArrayList<String> locationsString;

    ArrayAdapter<String> locationsAdapter;

    EditGroup new_group;

    @AfterViews
    void init(){
        setSupportActionBar(toolbar);
        configureBar();

        this.locationsString = new ArrayList<>();
        visibilitySpinner.setSelection(2);

        locality.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
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
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Click(R.id.btnNewGroup)
    public void btnNewGroupClick(){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.green_trazeo_5));
        pDialog.setTitleText("Un momento");
        pDialog.setCancelable(false);
        pDialog.show();
        sendNewGroup();
    }

    @UiThread
    public void showError(String error_message) {
        pDialog.dismiss();
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error_title))
                .setContentText(error_message)
                .show();
    }

    void sendNewGroup(){
        RequestParams params = new RequestParams();
        params.put("email",myPrefs.email().get());
        params.put("pass", myPrefs.pass().get());
        params.put("name", new_group_name.getText().toString());
        params.put("country","spain");
        params.put("city", locality.getText().toString());
        params.put("visibility", visibilitySpinner.getSelectedItemPosition());
        params.put("school1", school.getText().toString());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_MANAGE_GROUP, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();

                final Type objectCPD = new TypeToken<EditGroup>() {}.getType();
                new_group = new Gson().fromJson(result, objectCPD);

                Group group = new Group(new_group.data.id, new_group_name.getText().toString());
                group.save();

                createChat(group.id);

                showResult(new_group);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showError(error_loading);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showError(server_error);
            }
        });
    }

    @Background
    void createChat(String groupId){
        ChatCreator chatCreator = new ChatCreator(NewGroupActivity.this, true);
        chatCreator.createOrGetChat(groupId);
    }

    @UiThread
    void showResult(EditGroup new_group){
        pDialog.dismiss();
        if (new_group.state.equals("1")) {
            Intent i = new Intent(this, SelectGroupActivity_.class);
            i.putExtra("update", true);
            setResult(RESULT_OK, i);
            finish();
        } else {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_title))
                    .setContentText(getString(R.string.name_used))
                    .setConfirmText("Aceptar")
                    .show();
        }
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
                final Type objectCPD = new TypeToken<Locations>() {
                }.getType();
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
        locality.setAdapter(locationsAdapter);

        this.locationsString.clear();
    }

    private void buildHelpDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.info))
                .setContentText(getString(R.string.info_new_group))
                .setConfirmText("Entendido")
                .setCustomImage(R.drawable.mascota3)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_group, menu);
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
                return true;
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
