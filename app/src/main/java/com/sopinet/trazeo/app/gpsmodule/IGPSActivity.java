package com.sopinet.trazeo.app.gpsmodule;

import android.location.Location;
import android.os.Bundle;

/**
 * Created by david on 24/06/14.
 */
public interface IGPSActivity {
    public void locationChanged(double longitude, double latitude);
    public void gpsFirstFix(Location location);
}
