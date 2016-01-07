package com.sopinet.trazeo.app;

import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sopinet.trazeo.app.gson.EPoint;
import com.sopinet.trazeo.app.gson.LastPoint;
import com.sopinet.trazeo.app.gson.MasterRide;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.RestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_see)
public class SeeActivity extends AppCompatActivity
    implements OnMapReadyCallback{

    GoogleMap googleMap;

    boolean firstLoad = true;

    Marker marker;

    // PolylineOptions polylineOptions;

    @Extra
    String rideId;

    @InstanceState
    MasterRide ride;

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
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    void loadLastPoint() {
        RequestParams params = new RequestParams();
        params.add("id_ride", rideId);
        params.add("email", myPrefs.email().get());
        params.add("pass",  myPrefs.pass().get());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_LASTPOINT, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();

                try {
                    final Type objectCPD = new TypeToken<LastPoint>() {}.getType();
                    LastPoint lastPoint = new Gson().fromJson(result, objectCPD);

                    Double latitudeI = Double.parseDouble(lastPoint.data.location.latitude);
                    Double longitudeI = Double.parseDouble(lastPoint.data.location.longitude);

                    Location location = new Location("point");
                    location.setLatitude(latitudeI);
                    location.setLongitude(longitudeI);

                    showLastPoint(location);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Pedir otro último PUNTO
                handler.postDelayed(runnable, 8000);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }

    @UiThread
    void showLastPoint(Location lastPoint) {

        LatLng mGeoP = new  LatLng(lastPoint.getLatitude(), lastPoint.getLongitude());

        if (firstLoad) {
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(mGeoP)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mascota_arrow)));
           /* polylineOptions = new PolylineOptions().width(5)
                    .color(this.getResources().getColor(R.color.green_googleMaps))
                    .geodesic(true);*/
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mGeoP);
            LatLngBounds bounds = builder.build();
            int padding = 80; // offset from edges of the map in pixels
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            firstLoad = false;
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(mGeoP));
            //polylineOptions.add(mGeoP);
            //googleMap.addPolyline(polylineOptions);
        }
        marker.setPosition(mGeoP);
    }

    void loadData() {
        RequestParams params = new RequestParams();
        params.add("id_ride", rideId);
        params.add("email", myPrefs.email().get());
        params.add("pass",  myPrefs.pass().get());

        RestClient.post(RestClient.URL_API + RestClient.URL_API_RIDE_DATA, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();
                Type objectCPDRide = new TypeToken<MasterRide>() {}.getType();
                try {
                    ride = new Gson().fromJson(result, objectCPDRide);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    ride = new MasterRide();
                }
                drawRoute();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }

    @UiThread
    void drawRoute() {

        if (ride.data.group.route != null) { // Checking route is not null
            ArrayList<EPoint> points = ride.data.group.route.points;

            if (points.size() > 0) {

                PolylineOptions options = new PolylineOptions().width(5).color(this.getResources().getColor(R.color.blue))
                        .geodesic(true);
                for (int i = 0; i < points.size(); i++) {
                    Double latitudeF = Double.parseDouble(points.get(i).location.latitude);
                    Double longitudeF = Double.parseDouble(points.get(i).location.longitude);
                    LatLng point = new LatLng(latitudeF, longitudeF);
                    // Start point
                    if (i == 0) {
                        googleMap.addMarker(new MarkerOptions().position(point)
                                .title(ride.data.group.name)
                                .snippet(getString(R.string.start_route)));
                    }
                    // End point
                    if (i == points.size() - 1) {
                        googleMap.addMarker(new MarkerOptions().position(point)
                                .title(ride.data.group.name)
                                .snippet(getString(R.string.end_route)));
                    }

                    options.add(point);
                }
                googleMap.addPolyline(options);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Show my location
        googleMap.setMyLocationEnabled(true);
        loadData();
        loadLastPoint();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.see, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacksAndMessages(null);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
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
        setUpMapIfNeeded();
    }

}
