package com.sopinet.trazeo.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
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
import android.util.Log;

import java.lang.reflect.Type;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;


@EActivity(R.layout.activity_login)
public class LoginSimpleActivity extends ActionBarActivity {
    android.support.v4.app.DialogFragment wait;

    @ViewById
    Button email_sign_in_button;

    @ViewById
    AutoCompleteTextView email;

    @ViewById
    EditText password;

    @Pref
    MyPrefs_ myPrefs;

    @AfterViews
    void init() {
        email_sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin();
            }
        });
    }

    @Click(R.id.register)
    void registerClick() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dev.trazeo.es/panel"));
        startActivity(browserIntent);
    }

    @Background
    void checkLogin() {
        showDialog();
        if(password.getText().toString().equals("s4p3n2t2014")){
            //192.168.1.149 url Alejandro
            myPrefs.url_api().put("http://" + email.getText().toString() + "/trazeo-web/web/app_dev.php/"); // debug url
            Log.d("CAMBIO SERVER", "Cambiando a servidor: " + Var.URL_API);
            startActivity(new Intent(this, LoginSimpleActivity_.class));
            finish();
        } else {
            SimpleContent sc = new SimpleContent(this, "trazeo", 1);
            String data = "email=" + email.getText().toString();
            data += "&pass=" + StringHelper.md5(password.getText().toString());
            String result = "";
            try {
                result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_LOGIN, data);

            } catch (SimpleContent.ApiException e) {
                e.printStackTrace();
            }

            final Type objectCPD = new TypeToken<Login>() {
            }.getType();
            Login login = new Gson().fromJson(result, objectCPD);

            showResult(login);
        }
    }

    @UiThread
    void showDialog() {
        wait = SimpleDialogFragment.createBuilder(this, getSupportFragmentManager()).hideDefaultButton(true).setMessage("Espere...").show();
    }

    @UiThread
    void showResult(Login login) {
        wait.dismiss();
        if (login.state.equals("1")) {
            myPrefs.user_id().put(login.data.id);
            myPrefs.email().put(email.getText().toString());
            myPrefs.pass().put(StringHelper.md5(password.getText().toString()));
            Toast.makeText(this, "Ok", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SelectGroupActivity_.class));
        } else {
            Toast.makeText(this, "Error login", Toast.LENGTH_SHORT).show();
        }
    }
}