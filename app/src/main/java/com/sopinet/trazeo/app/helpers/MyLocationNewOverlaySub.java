package com.sopinet.trazeo.app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;

import com.sopinet.trazeo.app.R;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.util.constants.MapViewConstants;

/**
 * Created by david on 9/05/14.
 */
public class MyLocationNewOverlaySub extends MyLocationNewOverlay {

    // TODO: use dynamic calculation?
    private final static int PADDING_ACTIVE_ZOOM     = 50;

    private MapController mc;
    private Bitmap marker;
    private final Matrix mDirectionRotater = new Matrix();
    private Point currentPoint = new Point();

    private boolean centerOnCurrentLocation = true;

    private int height;
    private int width;

    private final Point mMapCoords = new Point();
    private final Matrix mMatrix = new Matrix();
    private final float[] mMatrixValues = new float[9];

    protected final double mDirectionArrowCenterX;
    protected final double mDirectionArrowCenterY;

    public MyLocationNewOverlaySub(Context context, MapView mapView) {
        super(context, mapView);
        this.mc = (MapController) mapView.getController();
        this.marker = BitmapFactory.decodeResource(context.getResources(), R.drawable.mascota_arrow);

        mCirclePaint.setARGB(0, 100, 100, 255);
        mCirclePaint.setAntiAlias(true);

        mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() / 2.0 - 0.5;
        mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() / 2.0 - 0.5;
    }

    @Override
    protected void drawMyLocation(ISafeCanvas canvas, MapView mapView, Location lastFix) {
        //super.drawMyLocation(canvas, mapView, lastFix);
        final MapView.Projection pj = mapView.getProjection();
        final int zoomDiff = MapViewConstants.MAXIMUM_ZOOMLEVEL - pj.getZoomLevel();

        if (mDrawAccuracyEnabled) {
            final float radius = lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(),
                    mapView.getZoomLevel());

            mCirclePaint.setAlpha(50);
            mCirclePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mMapCoords.x >> zoomDiff, mMapCoords.y >> zoomDiff, radius,
                    mCirclePaint);

            mCirclePaint.setAlpha(150);
            mCirclePaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mMapCoords.x >> zoomDiff, mMapCoords.y >> zoomDiff, radius,
                    mCirclePaint);
        }

        canvas.getMatrix(mMatrix);
        mMatrix.getValues(mMatrixValues);

        if (DEBUGMODE) {
            final float tx = (-mMatrixValues[Matrix.MTRANS_X] + 20)
                    / mMatrixValues[Matrix.MSCALE_X];
            final float ty = (-mMatrixValues[Matrix.MTRANS_Y] + 90)
                    / mMatrixValues[Matrix.MSCALE_Y];
            canvas.drawText("Lat: " + lastFix.getLatitude(), tx, ty + 5, mPaint);
            canvas.drawText("Lon: " + lastFix.getLongitude(), tx, ty + 20, mPaint);
            canvas.drawText("Alt: " + lastFix.getAltitude(), tx, ty + 35, mPaint);
            canvas.drawText("Acc: " + lastFix.getAccuracy(), tx, ty + 50, mPaint);
        }

        // Calculate real scale including accounting for rotation
        float scaleX = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X]
                * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MSKEW_Y]
                * mMatrixValues[Matrix.MSKEW_Y]);
        float scaleY = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_Y]
                * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MSKEW_X]
                * mMatrixValues[Matrix.MSKEW_X]);
        final double x = mMapCoords.x >> zoomDiff;
        final double y = mMapCoords.y >> zoomDiff;
        if (lastFix.hasBearing()) {
            canvas.save();
            // Rotate the icon
            canvas.rotate(lastFix.getBearing(), x, y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.scale(1 / scaleX, 1 / scaleY, x, y);
            // Draw the bitmap
            canvas.drawBitmap(this.marker, x - this.mDirectionArrowCenterX, y
                    - this.mDirectionArrowCenterY, mPaint);
            canvas.restore();
        } else {
            canvas.save();
            // Unrotate the icon if the maps are rotated so the little man stays upright
            canvas.rotate(-mMapView.getMapOrientation(), x, y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.scale(1 / scaleX, 1 / scaleY, x, y);
            // Draw the bitmap
            canvas.drawBitmap(this.marker, x - mPersonHotspot.x, y - mPersonHotspot.y, mPaint);
            canvas.restore();
        }
    }
}
