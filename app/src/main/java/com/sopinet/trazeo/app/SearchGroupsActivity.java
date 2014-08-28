package com.sopinet.trazeo.app;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.extractors.StringExtractor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.MinimalJSON;
import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.Cities;
import com.sopinet.trazeo.app.gson.Group;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.helpers.CityGroupAdapter;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.lang.reflect.Type;

@EActivity(R.layout.search_groups_activity)
public class SearchGroupsActivity extends ActionBarActivity {

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    Spinner citiesSpinner;

    @ViewById
    LinearLayout citiesForm;

    @ViewById
    LinearLayout groupsListLayout;

    @ViewById
    ListView groupsList;

    Groups groups;

    Cities cities;

    ProgressDialog pdialog;

    @AfterViews
    void init() {
        configureBar();
        pdialog = new ProgressDialog(this);
        pdialog.setCancelable(false);
        pdialog.setMessage("Cargando...");
        pdialog.show();
        obtainCities();
    }

    @Click(R.id.btnSearchGroups)
    public void btnSearchClick() {
        try {
            pdialog = new ProgressDialog(this);
            pdialog.setCancelable(false);
            pdialog.setMessage("Buscando...");
            pdialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        obtainGroups(true);
    }

    @Click(R.id.btnSeeAll)
    public void btnSeeAllClick() {
        try {
            pdialog = new ProgressDialog(this);
            pdialog.setCancelable(false);
            pdialog.setMessage("Buscando...");
            pdialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        obtainGroups(false);
    }

    @Background
    void obtainCities() {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email=" + myPrefs.email().get();
        data += "&pass=" + myPrefs.pass().get();
        String result = "";
        try {
            result = sc.getUrlContent(myPrefs.url_api().get() + Var.URL_API_GET_CITIES, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPD = new TypeToken<Cities>() {
        }.getType();
        this.cities = new Gson().fromJson(result, objectCPD);
        setSpinnerAdapter();
    }

    @UiThread
    void setSpinnerAdapter() {
        pdialog.dismiss();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, this.cities.cities);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        citiesSpinner.setAdapter(dataAdapter);
    }

    @Background
    void obtainGroups(boolean fromCity) {
        String city = "";

        if(fromCity) {
            city = citiesSpinner.getSelectedItem().toString().toLowerCase();
            city = city.replace("á", "a");
            city = city.replace("é", "e");
            city = city.replace("í", "i");
            city = city.replace("ó", "o");
            city = city.replace("ú", "u");
        } else {
            city = "all";
        }

        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email=" + myPrefs.email().get();
        data += "&pass=" + myPrefs.pass().get();
        data += "&city=" + city;
        data += "&object=true";
        String result = "";
        try {
            result = sc.getUrlContent(myPrefs.url_api().get() + Var.URL_API_SEARCH_GROUPS, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }
        Log.d("GROUPS", "GROUPS: " + result);
        final Type objectCPD = new TypeToken<Groups>() {
        }.getType();
        this.groups = new Gson().fromJson(result, objectCPD);
        showGroups();
    }

    @UiThread
    void showGroups() {
        pdialog.dismiss();
        groupsListLayout.setVisibility(View.VISIBLE);
        citiesForm.setVisibility(View.GONE);
        BindDictionary<Group> dict = new BindDictionary<Group>();
        dict.addStringField(R.id.name,
                new StringExtractor<Group>() {

                    @Override
                    public String getStringValue(Group group, int position) {
                        return group.name;
                    }
                });

        dict.addStringField(R.id.route,
                new StringExtractor<Group>() {

                    @Override
                    public String getStringValue(Group group, int position) {
                        return group.route.name;
                    }
                });

        dict.addStringField(R.id.creator,
                new StringExtractor<Group>() {

                    @Override
                    public String getStringValue(Group group, int position) {
                        return group.route.admin_name;
                    }
                });

        CityGroupAdapter adapter = new CityGroupAdapter(this, R.layout.city_group_list_item, groups.data);

        groupsList.setAdapter(adapter);
    }

    private void configureBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Background
    public void joinGroup(String id_group) {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email=" + myPrefs.email().get();
        data += "&pass=" + myPrefs.pass().get();
        data += "&id_group=" + id_group;
        String result = "";
        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_JOIN_GROUP, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }
        Log.d("JOIN", "JOIN: " + result);

        final Type objectCPD = new TypeToken<MinimalJSON>() {
        }.getType();

        MinimalJSON state = new Gson().fromJson(result, objectCPD);
        showJoinResult(state);
    }

    @UiThread
    void showJoinResult(MinimalJSON state){
        if (state.msg.equals("User it's already on group")){
            Toast.makeText(this, "Ya estás vinculado a este grupo", Toast.LENGTH_LONG).show();
        } else if (state.state.equals("-1")) {
            Toast.makeText(this, "Ha ocurrido un problema", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Vinculado correctamente", Toast.LENGTH_LONG).show();
        }
    }

    @Background
    public void requestGroup(String id_group) {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email=" + myPrefs.email().get();
        data += "&pass=" + myPrefs.pass().get();
        data += "&id_group=" + id_group;
        String result = "";
        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_REQUEST_GROUP, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }
        Log.d("REQUEST", "REQUEST: " + result);

        final Type objectCPD = new TypeToken<MinimalJSON>() {
        }.getType();

        MinimalJSON state = new Gson().fromJson(result, objectCPD);
        showRequestResult(state);
    }

    @UiThread
    void showRequestResult(MinimalJSON state){
        if (state.msg.equals("Join to group request has been did before")){
            Toast.makeText(this, "Ya has pedido acceso a este grupo anteriormente", Toast.LENGTH_LONG).show();
        } else if (state.state.equals("-1")) {
            Toast.makeText(this, "Ha ocurrido un problema", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Se ha enviado tu solicitud", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
