package com.sopinet.trazeo.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

import com.sopinet.trazeo.app.helpers.MyPrefs_;

@EActivity(R.layout.activity_init)
public class InitActivity extends Activity {
    @Pref
    MyPrefs_ myPrefs;

    @AfterViews
    void init() {
        String email = myPrefs.email().get();
        if (email.equals("")) {
            // Primera vez - Login
            startActivity(new Intent(this, LoginSimpleActivity_.class));
        } else {
            // Listado de Grupos
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}