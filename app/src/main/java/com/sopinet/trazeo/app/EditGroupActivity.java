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
import com.sopinet.android.mediauploader.MinimalJSON;
import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.EditGroup;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.lang.reflect.Type;

import io.segment.android.Analytics;

@EActivity(R.layout.edit_group_activity)
public class EditGroupActivity extends ActionBarActivity{

    @Pref
    MyPrefs_ myPrefs;

    @Extra
    String id_group;

    @ViewById
    AutoCompleteTextView group_name;

    @ViewById
    Spinner visibilitySpinner;

    ProgressDialog pdialog;

    @AfterViews
    void init(){
        configureBar();
        Analytics.onCreate(this);
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
            pdialog = new ProgressDialog(this);
            pdialog.setCancelable(false);
            pdialog.setMessage("Espera...");
            pdialog.show();
            sendEditGroup();
        } else {
            Toast.makeText(this, "No hay conexión", Toast.LENGTH_SHORT).show();
        }
    }

    @Background
    void sendEditGroup(){
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email=" + myPrefs.email().get();
        data += "&pass=" + myPrefs.pass().get();
        data += "&name=" + group_name.getText().toString();
        data += "&visibility=" + visibilitySpinner.getSelectedItemPosition();
        data += "&id_group=" + id_group;
        String result = "";
        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_MANAGE_GROUP, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        Log.d("EDIT", "EDIT GROUP: " + result);

        final Type objectCPD = new TypeToken<EditGroup>() {
        }.getType();
        EditGroup edit_group = new Gson().fromJson(result, objectCPD);

        showResult(edit_group);
    }

    @UiThread
    void showResult(EditGroup edit_group){
        pdialog.dismiss();
        if (edit_group.state.equals("1")) {
            Toast.makeText(this, "El grupo ha sido editado correctamente", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SelectGroupActivity_.class));
            finish();
        } else if (edit_group.msg.equals("Name is already in use")){
            Toast.makeText(this, "Este nombre ya se está usando", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildHelpDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sólo tú como administrador puedes cambiar la configuración del grupo. Si tu grupo es \"Privado\", las personas que quieran unirse tendrán que solicitar tu permiso para hacerlo. Si es \"Público\", cualquiera puede unirse de forma sencilla en cualquier momento. Recuerda poner un nombre significativo al grupo, para que sea fácil de encontrar por las personas que lo busquen. Poner el nombre del colegio destino y la zona de salida puede ser buena idea.")
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
        getMenuInflater().inflate(R.menu.edit_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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
