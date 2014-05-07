package com.sopinet.trazeo.app;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.LastPoint;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.lang.reflect.Type;
import java.util.logging.LogRecord;

@EActivity(R.layout.activity_see)
public class SeeActivity extends ActionBarActivity {

    @ViewById
    MapView mapview;

    private String data;
    private String lastPointID = "0";

    @Pref
    MyPrefs_ myPrefs;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() {
            loadLastPoint();
        }
    };

    @AfterViews
    void init() {
        data = "id_ride="+myPrefs.id_ride().get()+"&email="+myPrefs.email().get()+"&pass="+myPrefs.pass().get();

        String tiles[] = new String[1];
        tiles[0] = "http://tile.openstreetmap.org/";
        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", tiles);

        mapview.setBuiltInZoomControls(true);
        mapview.setTileSource(tileSource);

        // Añado localización
        MyLocationNewOverlay myLocNewOver = new MyLocationNewOverlay(this, mapview);

        myLocNewOver.enableFollowLocation();
        myLocNewOver.enableMyLocation();
        myLocNewOver.setDrawAccuracyEnabled(true);
        mapview.getOverlays().add(myLocNewOver);

        // Actualizamos Mapa
        mapview.invalidate();

        // Establecemos Zoom
        mapview.getController().setZoom(14);

        loadLastPoint();
    }

    @Background
    void loadLastPoint() {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String result = "";
        try {
            result = sc.postUrlContent(Var.URL_API_LASTPOINT, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPD = new TypeToken<LastPoint>(){}.getType();
        LastPoint lastPoint = new Gson().fromJson(result, objectCPD);

        if (lastPoint.data != null
                && lastPoint.data.location != null
                && !lastPoint.data.id.equals(lastPointID)) {
            lastPointID = lastPoint.data.id;
            showLastPoint(lastPoint);
        }

        // Pedir otro último PUNTO
        handler.postDelayed(runnable, 1000);
    }

    @UiThread
    void showLastPoint(LastPoint lastPoint) {
        Double latitudeI = Double.parseDouble(lastPoint.data.location.latitude);
        Double longitudeI = Double.parseDouble(lastPoint.data.location.longitude);

        GeoPoint mGeoP = new GeoPoint(latitudeI, longitudeI);

        Marker mPin = new Marker(mapview);
        mPin.setPosition(mGeoP);

        mapview.getOverlays().add(mPin);
        mapview.invalidate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.see, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
