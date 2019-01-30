package rightmesh.left.io.gpstracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Using FusedLocationProviderClient to get location updates.
 */
public class LocationTracker implements LifecycleObserver {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Activity activity;

    private int interval = 1000;
    private int fastestInterval = 500;
    private int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

    private LocationRequest locationRequest;

    private boolean isLocationRequested = false;
    private LocationCallback locationCallback;

    /**
     * Constructor
     * @param activity current activity
     * @param lifecycle get by getLifecycle() in Activity, Fragment. Used to observe lifecycle of Activity or Fragment
     */
    public LocationTracker(Activity activity, Lifecycle lifecycle) {
        this.activity = activity;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        lifecycle.addObserver(this);
    }

    /**
     * Set up the preferred the rate in millisecond to receive updates
     *
     * @param interval in milliseconds
     * @return {@link LocationTracker}
     */
    public LocationTracker setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Set up the fastest rate in milliseconds.
     * To avoid UI flicker or data flow problems when Google Play services send out updates at fastest rate
     *
     * @param fastestInterval the fastest interval in milliseconds
     * @return {@link LocationTracker}
     */
    public LocationTracker setFastestInterval(int fastestInterval) {
        this.fastestInterval = fastestInterval;
        return this;
    }

    /**
     * set up the priority of the request to help Google Play services identify which location sources to use
     *
     * @param priority LocationRequest.PRIORITY_HIGH_ACCURACY,
     *                 LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
     *                 LocationRequest.PRIORITY_LOW_POWER,
     *                 LocationRequest.PRIORITY_NO_POWER
     * @return {@link LocationTracker}
     */
    public LocationTracker setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdate(LocationCallback locationCallback) {
        if (!isLocationRequested) {
            this.locationCallback = locationCallback;
            this.locationRequest = new LocationRequest()
                    .setInterval(interval)
                    .setFastestInterval(fastestInterval)
                    .setPriority(priority);

            isLocationRequested = true;
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null);

        }
    }

    private void removeLocationUpdate(){
        isLocationRequested = false;
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            //enable GC to clean object
            locationCallback = null;
        }
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation(OnSuccessListener<Location> onSucessListener,
                                OnFailureListener onFailureListener) {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(onSucessListener)
                .addOnFailureListener(onFailureListener);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void destroy() {
        //enable GC to clean object
        activity = null;
        removeLocationUpdate();
    }
}
