package com.sopinet.trazeo.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sopinet.trazeo.app.gson.EEvent;
import com.sopinet.trazeo.app.gson.EPoint;
import com.sopinet.trazeo.app.helpers.MyLocationNewOverlaySub;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;
import com.sopinet.trazeo.app.osmlocpull.OsmLocPullService;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.log4j.lf5.util.Resource;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

@EFragment(R.layout.fragment_monitor_map)
public class MonitorMapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String Adata = "mdata";

    // TODO: Rename and change types of parameters
    private String mdata;

    @Pref
    MyPrefs_ myPrefs;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MonitorMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonitorMapFragment newInstance(String data) {
        MonitorMapFragment fragment = new MonitorMapFragment();
        Bundle args = new Bundle();
        args.putString(Adata, data);
        fragment.setArguments(args);
        return fragment;
    }
    public MonitorMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mdata = getArguments().getString(Adata);
            Log.d("TEMA", "DATA3: "+mdata);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_monitor_map, container, false);



        final Context context = getActivity();
        final Context applicationContext = context.getApplicationContext();
        final IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(applicationContext);

        MapView mapview = (MapView) root.findViewById(R.id.mapview);

        // Create a custom tile source
        String tiles[] = new String[1];
        tiles[0] = "http://tile.openstreetmap.org/";
        final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", tiles);

        mapview.setBuiltInZoomControls(true);
        mapview.setTileSource(tileSource);

        // TODO: Necesario para OSMLOCPULLRECEIVER, mirar de quitar
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        drawRoute(mapview, context);
        // Pinto Ruta preestablecida
        /**
        RoadManager roadManager = new OSRMRoadManager();
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

        ArrayList<EPoint> points = MonitorActivity.ride.data.group.route.points;
        Double latitude = Double.valueOf(points.get(0).location.latitude);
        Double longitude = Double.valueOf(points.get(0).location.longitude);
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        GeoPoint endPoint;
        Road road;
        Polyline roadOverlay;
        for(int i = 1; i < points.size(); i++) {
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

        // Pinto Ruta recorrida
        ArrayList<EEvent> events = MonitorActivity.ride.data.events;
        startPoint = null;
        endPoint = null;
        for(int o = 0; o < events.size();o++) {
            startPoint = endPoint;
            if (events.get(o).action.equals("point")) {
                String[] coords = events.get(o).data.split(",");
                Double latitudeI = Double.parseDouble(coords[0].replace("(", ""));
                Double longitudeI = Double.parseDouble(coords[1].replace(")", ""));
                endPoint = new GeoPoint(latitudeI, longitudeI);
                if (startPoint != null) {
                    waypoints = new ArrayList<GeoPoint>();
                    waypoints.add(startPoint);
                    waypoints.add(endPoint);
                    road = roadManager.getRoad(waypoints);
                    roadOverlay = RoadManager.buildRoadOverlay(road, context);
                    roadOverlay.setColor(Color.GREEN);
                    mapview.getOverlays().add(roadOverlay);
                }
            }
        }
         **/

        // Añado localización
        MyLocationNewOverlay myLocNewOver = new MyLocationNewOverlay(context, mapview);

        myLocNewOver.enableFollowLocation();
        myLocNewOver.enableMyLocation();
        myLocNewOver.setDrawAccuracyEnabled(true);
        mapview.getOverlays().add(myLocNewOver);

        // Actualizamos Mapa
        mapview.invalidate();

        // Establecemos Zoom
        mapview.getController().setZoom(14);

        // Send location
        // https://github.com/mendhak/gpslogger/blob/master/gpslogger/src/main/AndroidManifest.xml

        return root;
    }

    @Background
    void drawRoute(MapView mapview, Context context) {
        RoadManager roadManager = new OSRMRoadManager();
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

        if (MonitorActivity.ride.data.group.route != null) { // Cuando no hay ruta establecida
            ArrayList<EPoint> points = MonitorActivity.ride.data.group.route.points;

            if (points.size() > 1) {
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

    @Background
    void drawRide() {

    }
}