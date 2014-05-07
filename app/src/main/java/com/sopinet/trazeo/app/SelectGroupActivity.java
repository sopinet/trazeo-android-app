package com.sopinet.trazeo.app;

import android.app.Service;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import com.ami.fundapter.extractors.StringExtractor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.SimpleContent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import com.sopinet.trazeo.app.gson.CreateRide;
import com.sopinet.trazeo.app.gson.Group;
import com.sopinet.trazeo.app.gson.Groups;
import com.sopinet.trazeo.app.gson.Login;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;
import com.sopinet.trazeo.app.osmlocpull.OsmLocPullService;

import java.lang.reflect.Type;
import android.widget.ListView;

@EActivity(R.layout.activity_select_group)
public class SelectGroupActivity extends ActionBarActivity {
    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    ListView listSelectGroup;

    public static Intent intentGPS;

    @AfterViews
    void init() {
        loadData();
    }

    @Background
    void loadData() {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email="+myPrefs.email().get();
        data += "&pass="+myPrefs.pass().get();
        String result = "";
        try {
            result = sc.postUrlContent(Var.URL_API_GROUPS, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPD = new TypeToken<Groups>(){}.getType();
        Groups groups = new Gson().fromJson(result, objectCPD);

        showData(groups);
        //sc.postUrlContent()
    }

    @UiThread
    void showData(final Groups groups) {
        BindDictionary<Group> dict = new BindDictionary<Group>();
        dict.addStringField(R.id.name,
                new StringExtractor<Group>() {

                    @Override
                    public String getStringValue(Group group, int position) {
                        return group.name;
                    }
                });

        dict.addStringField(R.id.description,
                new StringExtractor<Group>() {
                    @Override
                    public String getStringValue(Group item, int position) {
                        if (item.hasRide.equals("true")) {
                            return "...Paseo en curso...";
                        } else {
                            return "Iniciar";
                        }

                    }
                }
        );

        FunDapter<Group> adapter = new FunDapter<Group>(this, groups.data,
                R.layout.group_list_item, dict);

        listSelectGroup.setAdapter(adapter);

        listSelectGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                createRide(groups.data.get((int) l).id, groups.data.get((int) l).hasRide);
            }
        });

        //Toast.makeText(this, "Espere unos segundos mientras cargamos los datos de la Ruta...", Toast.LENGTH_LONG).show();
    }


    @Background
    void createRide(String l, String hasRide) {
        SimpleContent sc = new SimpleContent(this, "trazeo", 1);
        String data = "email="+myPrefs.email().get();
        data += "&pass="+myPrefs.pass().get();
        data += "&id_group="+l;
        String result = "";

        try {
            result = sc.postUrlContent(Var.URL_API_RIDE_CREATE, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        Log.d("TEMA", result);

        final Type objectCPD = new TypeToken<CreateRide>(){}.getType();
        CreateRide createRide = new Gson().fromJson(result, objectCPD);

        myPrefs.id_ride().put(createRide.data.id_ride);

        if (hasRide.equals("false")) {
            String data_service = "email=" + myPrefs.email().get();
            data_service += "&pass=" + myPrefs.pass().get();
            data_service += "&id_ride=" + createRide.data.id_ride;

            intentGPS = new Intent(this, OsmLocPullService.class);
            intentGPS.putExtra("url", Var.URL_API_SENDPOSITION);
            intentGPS.putExtra("data", data_service);
            startService(intentGPS);

            goActivityMonitor();
        } else {
            goActivitySee();
        }
    }

    @UiThread
    void goActivityMonitor() {
        startActivity(new Intent(SelectGroupActivity.this, MonitorActivity_.class));
    }

    @UiThread
    void goActivitySee() {
        // TODO: Actividad, seguir
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_group, menu);
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
            startActivity(new Intent(this, LoginSimpleActivity_.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}