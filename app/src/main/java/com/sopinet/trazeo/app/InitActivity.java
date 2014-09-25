package com.sopinet.trazeo.app;

import android.app.Activity;
import android.os.Handler;
import android.content.Intent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

import com.androidquery.service.MarketService;
import com.github.snowdream.android.util.Log;
import com.sopinet.androidlogmailer.Mail;
import com.sopinet.trazeo.app.helpers.MyPrefs_;

import io.segment.android.Analytics;

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

        configureLogMailer();

        MarketService ms = new MarketService(this);
        ms.level(MarketService.REVISION).checkVersion();

        final int SPLASH_DISPLAY_LENGHT = 2000;
        Analytics.onCreate(this);

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

    public void configureLogMailer() {
        Log.setEnabled(true);
        Log.setTag("TRAZEO");
        Log.setPath("/mnt/sdcard/trazeolog.txt");
        Log.setPolicy(Log.LOG_ALL_TO_FILE);

        // Este método permite obtener la excepción que ha detenido el hilo
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                sendReportEmail(Log.getStackTraceString(throwable));
            }
        });
    }

    @Background
    public void sendReportEmail(String error) {
        Mail m = new Mail("notificaciones.sopinet@gmail.com", "bloblo08");
        String[] toArr = {"davidml91dml@gmail.com"};
        m.set_to(toArr);
        m.set_from("notificaciones.sopinet@gmail.com");
        m.set_subject("ERROR LOG Trazeo (Android-LogMailer) email: " + myPrefs.email().get());
        m.setBody(error);
        try {
            m.addAttachment("/mnt/sdcard/trazeolog.txt");
            m.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
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