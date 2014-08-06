package com.sopinet.trazeo.app;

import android.app.Activity;
import android.os.Handler;
import android.content.Intent;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import com.androidquery.service.MarketService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.MasterRide;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Calendar;

@EActivity(R.layout.activity_init)
public class InitActivity extends Activity {
    @Pref
    MyPrefs_ myPrefs;

    String email;
    String timestamp;

    @AfterViews
    void init() {

        email = myPrefs.email().get();
        if(myPrefs.url_api().get().equals(""))
            myPrefs.url_api().put("http://beta.trazeo.es/");

        MarketService ms = new MarketService(this);
        ms.level(MarketService.REVISION).checkVersion();

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