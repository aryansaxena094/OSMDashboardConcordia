package de.storchp.opentracks.osmplugin.maps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.layer.overlay.Marker;

import de.storchp.opentracks.osmplugin.R;
import de.storchp.opentracks.osmplugin.compass.Compass;
import de.storchp.opentracks.osmplugin.utils.ArrowMode;
import de.storchp.opentracks.osmplugin.utils.MapMode;

public class RotatableMarker extends Marker {

    private static final String TAG = RotatableMarker.class.getSimpleName();
    private final android.graphics.Bitmap markerBitmap;
    private float currentDegrees = 0;

    public RotatableMarker(LatLong latLong, android.graphics.Bitmap markerBitmap) {
        super(latLong, createRotatedMarkerBitmap(markerBitmap, 0), 0, 0);
        this.markerBitmap = markerBitmap;
    }

    private static Bitmap createRotatedMarkerBitmap(android.graphics.Bitmap markerBitmap, float degrees) {
        var matrix = new Matrix();
        matrix.postRotate(degrees);
        return new AndroidBitmap(android.graphics.Bitmap.createBitmap(markerBitmap, 0, 0, markerBitmap.getWidth(), markerBitmap.getHeight(), matrix, true));
    }

    public static android.graphics.Bitmap getBitmapFromVectorDrawable(Context context, int drawableId,String text) {


        var drawable = ContextCompat.getDrawable(context, drawableId);
        assert drawable != null;
        drawable = (DrawableCompat.wrap(drawable)).mutate();

        var bitmap = android.graphics.Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        if(text!=null){
            // Inflate the layout containing the TextView
            View view = LayoutInflater.from(context).inflate(R.layout.view_custom_marker, null);

            // Find the TextView in the inflated layout
            TextView textView = view.findViewById(R.id.numberTextView);

            // Set the text for the TextView
            textView.setText(text);

            // Measure and layout the TextView to determine its size
            textView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());

            textView.draw(canvas);
        }

        return bitmap;
    }

    private boolean rotateTo(float degrees) {
        if (Math.abs(currentDegrees - degrees) > 1) {
            // only create a new Marker Bitmap if it is at least 1 degree different
            Log.d(TAG, "CurrentDegrees=" + currentDegrees + ", degrees=" + degrees);
            setBitmap(createRotatedMarkerBitmap(markerBitmap, degrees));
            currentDegrees = degrees;
            return true;
        }
        return false;
    }

    public boolean rotateWith(ArrowMode arrowMode, MapMode mapMode, MovementDirection movementDirection, Compass compass) {
        if ((arrowMode == ArrowMode.COMPASS && mapMode == MapMode.COMPASS)
            || arrowMode == ArrowMode.NORTH) {
            return rotateTo(0);
        } else if (arrowMode == ArrowMode.DIRECTION && mapMode == MapMode.DIRECTION) {
            return rotateTo(mapMode.getHeading(movementDirection, compass));
        } else if (arrowMode == ArrowMode.DIRECTION && mapMode == MapMode.COMPASS) {
            return rotateTo(arrowMode.getDegrees(movementDirection, compass));
        } else {
            return rotateTo(arrowMode.getDegrees(movementDirection, compass) + mapMode.getHeading(movementDirection, compass) % 360);
        }
    }

}
