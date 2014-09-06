package com.sopinet.trazeo.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.EditGroup;
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

import io.segment.android.Analytics;
import io.segment.android.models.Props;

@EActivity(R.layout.new_group_activity)
public class NewGroupActivity extends ActionBarActivity{

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    AutoCompleteTextView new_group_name;

    @ViewById
    Spinner visibilitySpinner;

    ProgressDialog pdialog;

    @AfterViews
    void init(){
        configureBar();
        Analytics.onCreate(this);
        Analytics.track("New Group In - Android", new Props("email", myPrefs.email().get()));
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
        if(NetHelper.isOnline(this)) {
            pdialog = new ProgressDialog(this);
            pdialog.setCancelable(false);
            pdialog.setMessage("Espera...");
            pdialog.show();
            sendNewGroup();
        } else {
            Toast.makeText(this, "No hay conexión", Toast.LENGTH_SHORT).show();
        }
    }

    @Background
    void sendNewGroup(){
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email=" + myPrefs.email().get();
        data += "&pass=" + myPrefs.pass().get();
        data += "&name=" + new_group_name.getText().toString();
        data += "&visibility=" + visibilitySpinner.getSelectedItemPosition();
        String result = "";
        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_MANAGE_GROUP, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        Log.d("EDIT", "EDIT GROUP: " + result);

        final Type objectCPD = new TypeToken<EditGroup>() {
        }.getType();
        EditGroup new_group = new Gson().fromJson(result, objectCPD);

        showResult(new_group);
    }

    @UiThread
    void showResult(EditGroup new_group){
        pdialog.dismiss();
        if (new_group.state.equals("1")) {
            Toast.makeText(this, "El grupo ha sido creado correctamente", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SelectGroupActivity_.class));
            Analytics.track("New Group Created - Android", new Props("email", myPrefs.email().get()));
            finish();
        } else {
            Toast.makeText(this, "Ese nombre ya está en uso", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildHelpDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Cuando crees un nuevo grupo se creará por defecto como “Privado”, por lo que las personas que quieran unirse tendrán que solicitar tu permiso para hacerlo. Puedes cambiar la visibilidad a “Público” si quieres que cualquier usuario pueda unirse de forma sencilla. Recuerda poner un nombre significativo al grupo, para que sea fácil de encontrar por las personas que lo busquen. Poner el nombre del colegio destino y la zona de salida puede ser buena idea.")
                .setCancelable(false)
                .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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
                startActivity(new Intent(this, SelectGroupActivity_.class));
                finish();
                break;
            case R.id.help:
                buildHelpDialog();
                break;
        }
        return true;
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
