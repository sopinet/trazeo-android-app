package com.sopinet.trazeo.app;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.EPoint;
import com.sopinet.trazeo.app.gson.LastPoint;
import com.sopinet.trazeo.app.gson.MasterRide;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by david on 21/05/14.
 */
@EFragment(R.layout.activity_see)
public class SeeMapFragment extends Fragment {
    @ViewById
    MapView mapview;

    private String data;
    private String lastPointID = "0";
    private boolean firstLoad = true;

    private boolean isStartPoint = true;
    ArrayList<GeoPoint> waypoints;
    GeoPoint startPoint = null;
    GeoPoint endPoint = null;

    @Pref
    MyPrefs_ myPrefs;

    public Runnable runnable = new Runnable() {
        public void run() {
            loadLastPoint();
        }
    };

    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_see, container, false);

        data = "id_ride="+myPrefs.id_ride().get()+"&email="+myPrefs.email().get()+"&pass="+myPrefs.pass().get();

        String tiles[] = new String[1];
        tiles[0] = "http://tile.openstreetmap.org/";
        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", tiles);
        waypoints = new ArrayList<GeoPoint>();

        mapview.setBuiltInZoomControls(true);
        mapview.setTileSource(tileSource);

        // Añado localización
        MyLocationNewOverlay myLocNewOver = new MyLocationNewOverlay(root.getContext(), mapview);

        myLocNewOver.enableFollowLocation();
        myLocNewOver.enableMyLocation();
        myLocNewOver.setDrawAccuracyEnabled(true);
        mapview.getOverlays().add(myLocNewOver);

        // Actualizamos Mapa
        mapview.invalidate();

        // Establecemos Zoom
        mapview.getController().setZoom(14);

        loadLastPoint();
        loadData();

        return root;
    }

    @Background
    void loadLastPoint() {
        SimpleContent sc = new SimpleContent(root.getContext(), "trazeo", 0);
        String result = "";
        try {
            result = sc.postUrlContent(Var.URL_API_LASTPOINT, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        // NO se devuelve ningún punto
        if (result.length() < 35) {
            // El Monitor no manda puntos
        } else {
            final Type objectCPD = new TypeToken<LastPoint>() {}.getType();
            LastPoint lastPoint = new Gson().fromJson(result, objectCPD);

            if (lastPoint.data != null
                    && lastPoint.data.location != null
                    && !lastPoint.data.id.equals(lastPointID)) {
                lastPointID = lastPoint.data.id;
                showLastPoint(lastPoint);
            }
        }

        // Pedir otro último PUNTO
        ((SeeActivity)getActivity()).handler.postDelayed(runnable, 1000);
    }

    @UiThread
    void showLastPoint(LastPoint lastPoint) {
        RoadManager roadManager = new OSRMRoadManager();
        Polyline roadOverlay;

        Double latitudeI = Double.parseDouble(lastPoint.data.location.latitude);
        Double longitudeI = Double.parseDouble(lastPoint.data.location.longitude);

        GeoPoint mGeoP = new GeoPoint(latitudeI, longitudeI);
        if(isStartPoint) {
            startPoint = new GeoPoint(latitudeI, longitudeI);
            isStartPoint = false;
        } else {
            endPoint = new GeoPoint(latitudeI, longitudeI);
            isStartPoint = true;
        }

        Marker mPin = new Marker(mapview);
        mPin.setPosition(mGeoP);
        mPin.setIcon(getResources().getDrawable(R.drawable.mascota_arrow));

        if(!firstLoad)
            mapview.getOverlays().remove(0);

        mapview.getOverlays().add(0, mPin);

        if(isStartPoint){
            Road road;
            waypoints = new ArrayList<GeoPoint>();
            waypoints.add(startPoint);
            Log.d("STARTPOINT", "STARTPOINT: " + startPoint.getLatitude());
            waypoints.add(endPoint);
            Log.d("ENDPOINT", "ENDPOINT: " + endPoint.getLatitude());
            road = roadManager.getRoad(waypoints);
            roadOverlay = RoadManager.buildRoadOverlay(road, root.getContext());
            roadOverlay.setColor(this.getResources().getColor(R.color.green_trazeo_2));
            roadOverlay.getPaint().setPathEffect(new DashPathEffect(new float[] {10,10}, 5));
            mapview.getOverlays().add(roadOverlay);

            waypoints = new ArrayList<GeoPoint>();
            startPoint = endPoint;
        }
        mapview.invalidate();

        if(firstLoad) {
            mapview.getController().animateTo(mGeoP);
            firstLoad = false;
        }
    }

    @Background
    void loadData() {
        SimpleContent sc = new SimpleContent(root.getContext(), "trazeo", 0);
        String result = "";

        try {
            result = sc.postUrlContent(Var.URL_API_RIDE_DATA, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPD = new TypeToken<MasterRide>(){}.getType();
        MonitorActivity.ride = new Gson().fromJson(result, objectCPD);

        //myPrefs.id_ride().put(createRide.data.id_ride);
        //showData();
        drawRoute(mapview, root.getContext());
    }

    @UiThread
    void drawRoute(MapView mapview, Context context) {
        RoadManager roadManager = new OSRMRoadManager();
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

        if (MonitorActivity.ride.data.group.route != null) { // Checking route is not null
            ArrayList<EPoint> points = MonitorActivity.ride.data.group.route.points;

            if (points.size() > 1) { // Checking 2 points
                Double latitude = Double.valueOf(points.get(0).location.latitude);
                Double longitude = Double.valueOf(points.get(0).location.longitude);
                GeoPoint startPoint = new GeoPoint(latitude, longitude);
                GeoPoint endPoint;
                Road road;
                Polyline roadOverlay;
                for (int i = 1; i < points.size(); i++) {
                    waypoints.add(startPoint);
                    Double latitudeF = Double.parseDouble(points.get(i).location.latitude);
                    Double longitudeF = Double.parseDouble(points.get(i).location.longitude);
                    endPoint = new GeoPoint(latitudeF, longitudeF);
                    waypoints.add(endPoint);
                    road = roadManager.getRoad(waypoints);
                    roadOverlay = RoadManager.buildRoadOverlay(road, context);
                    mapview.getOverlays().add(roadOverlay);

                    waypoints = new ArrayList<GeoPoint>();
                    startPoint = endPoint;
                }
            }
        }
    }
}
