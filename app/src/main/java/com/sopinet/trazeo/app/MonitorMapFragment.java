package com.sopinet.trazeo.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sopinet.trazeo.app.gson.EPoint;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.Var;
import com.sopinet.trazeo.app.osmlocpull.OsmLocPullService;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.sharedpreferences.Pref;
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
        Log.d("TEMA", "DATA2: "+data);
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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RoadManager roadManager = new OSRMRoadManager();

        //RoadManager roadManager = new MapQuestRoadManager("Fmjtd%7Cluur2q0rnq%2Cba%3Do5-9aaw00");
        //roadManager.addRequestOption("routeType=bicycle");
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

            startPoint = endPoint;
        }

        //Add MyLocationNewOverlay
        MyLocationNewOverlay myLocNewOver = new MyLocationNewOverlay(context, mapview);

        myLocNewOver.enableFollowLocation();
        myLocNewOver.enableMyLocation();
        myLocNewOver.setDrawAccuracyEnabled(true);
        mapview.getOverlays().add(myLocNewOver);

        //MyLoc myLoc = new MyLoc(myLocNewOver);

        // Actualizar
        mapview.invalidate();

        mapview.getController().setZoom(14);

        Intent intent = new Intent(context, OsmLocPullService.class);
        //intent.putExtra("com.sopinet.trazeo.app.helpers.MyLoc", myLoc);
        intent.putExtra("url", Var.URL_API_SENDPOSITION);
        intent.putExtra("data", mdata);
        Log.d("GPSLOG", "Fragment: "+mdata);
        context.startService(intent);

        // Send location
        // https://github.com/mendhak/gpslogger/blob/master/gpslogger/src/main/AndroidManifest.xml

        return root;
    }
}