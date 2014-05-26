package com.sopinet.trazeo.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Handler;
import android.content.Intent;
import android.widget.ImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import com.sopinet.trazeo.app.helpers.MyPrefs_;

@EActivity(R.layout.activity_init)
public class InitActivity extends Activity {
    @Pref
    MyPrefs_ myPrefs;

    String email;

    @AfterViews
    void init() {
        email = myPrefs.email().get();

        final int SPLASH_DISPLAY_LENGHT = 2000;

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                if (email.equals("")) {
                    // Primera vez - Login
                    startActivity(new Intent(getBaseContext(), LoginSimpleActivity_.class));
                    finish();
                } else {
                    // Listado de Grupos
                    startActivity(new Intent(getBaseContext(), SelectGroupActivity_.class));
                    finish();
                }
            }
        }, SPLASH_DISPLAY_LENGHT);

    }
}