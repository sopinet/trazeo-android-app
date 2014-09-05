package com.sopinet.trazeo.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.mediauploader.MinimalJSON;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.android.nethelper.StringHelper;
import com.sopinet.trazeo.app.gson.Login;
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

@EActivity(R.layout.register_activity)
public class RegisterActivity extends ActionBarActivity{

    @ViewById
    AutoCompleteTextView email;

    @ViewById
    EditText password;

    @ViewById
    EditText confirmPassword;

    @Pref
    MyPrefs_ myPrefs;

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

    @Click(R.id.email_register_button)
    public void btnRegisterClick(){
        int check = checkCorrectInput();

        switch(check){
            case 0:
                pdialog = new ProgressDialog(this);
                pdialog.setCancelable(false);
                pdialog.setMessage("Registrando tu usuario...");
                pdialog.show();
                sendRegistration();
                break;
            case 1:
                Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private int checkCorrectInput(){
        int check = 0;

        if(email.getText().toString().equals("") ||
                password.getText().toString().equals("") ||
                confirmPassword.getText().toString().equals(""))
            check = 1;
        else if(!password.getText().toString().equals(confirmPassword.getText().toString()))
            check = 2;

        return check;
    }

    @Background
    void sendRegistration(){
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "username=" + email.getText().toString();
        data += "&password=" + StringHelper.md5(password.getText().toString());
        String result = "";
        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_REGISTER, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        Log.d("REGISTER", "REGISTER: " + result);
        final Type objectCPD = new TypeToken<Login>() {
        }.getType();
        Login register = new Gson().fromJson(result, objectCPD);

        showResult(register);
    }

    @UiThread
    void showResult(Login register){
        pdialog.dismiss();
        if (register.state.equals("1")) {
            myPrefs.user_id().put(register.data.id);
            myPrefs.email().put(email.getText().toString());
            myPrefs.pass().put(StringHelper.md5(password.getText().toString()));
            startActivity(new Intent(this, SelectGroupActivity_.class));
            finish();
        } else {
            Toast.makeText(this, "Ya existe un usuario con ese email", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, LoginSimpleActivity_.class));
                break;
        }
        return true;
    }
}
