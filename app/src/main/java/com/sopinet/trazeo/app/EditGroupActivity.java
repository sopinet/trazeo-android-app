package com.sopinet.trazeo.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
        } else {
            Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
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
