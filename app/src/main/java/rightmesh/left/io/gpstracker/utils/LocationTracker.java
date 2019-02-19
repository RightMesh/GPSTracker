package rightmesh.left.io.gpstracker.utils;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

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

    private LocationCallback locationCallback;

    /**
     * LocationTracker contructor.
     *
     * @param activity  current activity
     * @param lifecycle get by getLifecycle() in Activity, Fragment.
     *                  Used to observe lifecycle of Activity or Fragment
     */
    public LocationTracker(Activity activity, Lifecycle lifecycle) {
        this.activity = activity;
        lifecycle.addObserver(this);
        this.fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(activity);
    }

    /**
     * Constructor to avoid dependency (only using for testing purpose).
     *
     * @param activity                    mock activity
     * @param lifecycle                   mock Lifecycle
     * @param fusedLocationProviderClient mock FusedLocationProviderClient
     */
    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.TESTS)
    public LocationTracker(Activity activity,
                           Lifecycle lifecycle,
                           FusedLocationProviderClient fusedLocationProviderClient) {
        this.activity = activity;
        lifecycle.addObserver(this);
        this.fusedLocationProviderClient = fusedLocationProviderClient;
    }

    /**
     * Location provider setter.
     *
     * @param fusedLocationProviderClient mock object
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setFusedLocationProviderClient(FusedLocationProviderClient
                                                       fusedLocationProviderClient) {
        this.fusedLocationProviderClient = fusedLocationProviderClient;
    }

    /**
     * Set up the preferred the rate in millisecond to receive updates.
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
     * To avoid UI flicker or data flow problems when
     * Google Play services send out updates at fastest rate
     *
     * @param fastestInterval the fastest interval in milliseconds
     * @return {@link LocationTracker}
     */
    public LocationTracker setFastestInterval(int fastestInterval) {
        this.fastestInterval = fastestInterval;
        return this;
    }

    /**
     * set up the priority of the request to help Google Play services identify which
     * location sources to use.
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

    /**
     * Request location update.
     *
     * @param locationCallback callback for get updated location.
     */
    @SuppressLint("MissingPermission")
    public void requestLocationUpdate(LocationCallback locationCallback) {
        this.locationCallback = locationCallback;
        this.locationRequest = new LocationRequest()
                .setInterval(interval)
                .setFastestInterval(fastestInterval)
                .setPriority(priority);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null);
    }

    /**
     * Remove location update.
     */
    private void removeLocationUpdate() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            //enable GC to clean object
            locationCallback = null;
        }
    }

    /**
     * Get last recorded location.
     *
     * @param onSucessListener  callback for get successfully.
     * @param onFailureListener callback for failure.
     */
    @SuppressLint("MissingPermission")
    public void getLastLocation(OnSuccessListener<Location> onSucessListener,
                                OnFailureListener onFailureListener) {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(onSucessListener)
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Automatically trigger in {@link Activity#onDestroy()} or {@link Fragment#onDestroy()}.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void destroy() {
        //enable GC to clean object
        activity = null;
        removeLocationUpdate();
    }

    /**
     * Check if location provider is available.
     *
     * @param context Application context
     * @return True:= available, False:= unavailable
     */
    public boolean isLocationProviderAvailable(Context context) {
        LocationManager lm = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Show Dialog to ask for turn GPS.
     */
    public void showDialogEnableGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("GPS is not found");  // GPS not found
        builder.setMessage("Turn on GPS?"); // Want to enable?
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("NO", null);
        builder.create().show();
    }
}
