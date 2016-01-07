package com.sopinet.trazeo.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import com.androidquery.service.MarketService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sopinet.trazeo.app.helpers.MyPrefs_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.IOException;


@EActivity(R.layout.activity_init)
public class InitActivity extends AppCompatActivity {

    @Pref
    public static MyPrefs_ myPrefs;

    //GCM
    public static final String PROPERTY_REG_ID = "";
    private static final String PROPERTY_APP_VERSION = "";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static GoogleCloudMessaging gcm;
    Context context;

    String regid;

    @AfterViews
    void init() {

        context = getApplicationContext();

        if(checkPlayServices()) {

            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            gcm = GoogleCloudMessaging.getInstance(context);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }

            MarketService ms = new MarketService(this);
            ms.level(MarketService.REVISION).checkVersion();

            final int SPLASH_DISPLAY_LENGHT = 2000;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getBaseContext(), LoginSimpleActivity_.class);
                    if (myPrefs.email().get().equals("")) {
                        // Primera vez - Login
                        i.putExtra("autologin", false);
                    } else {
                        // Listado de Grupos
                        i.putExtra("autologin", true);
                    }
                    startActivity(i);
                    finish();
                }
            }, SPLASH_DISPLAY_LENGHT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // GCM Methods

    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                android.util.Log.i("GCM", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        //String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        String registrationId = myPrefs.res_id().get();
        if (registrationId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        // App version changed
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }

    public SharedPreferences getGCMPreferences() {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(InitActivity_.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Background
    public void registerInBackground() {
        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(context);
        }
        try {
            // Register device
            regid = gcm.register(getString(R.string.gcm_defaultSenderId));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // You should send the registration ID to your server over HTTP,
        // so it can use GCM/HTTP or CCS to send messages to your app.
        // The request to your server should be authenticated if your app
        // is using accounts.
        //sendRegistrationIdToBackend(regid);

        myPrefs.res_id().put(regid);

        // For this demo: we don't need to send it because the device
        // will send upstream messages to a server that echo back the
        // message using the 'from' address in the message.

        // Persist the regID - no need to register again.
        storeRegistrationId(context, regid);
        //showRegisterInBackgroundResult(msg);
    }

    public void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }
}