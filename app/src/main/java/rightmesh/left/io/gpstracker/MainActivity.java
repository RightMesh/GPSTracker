package rightmesh.left.io.gpstracker;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.widget.Toast.LENGTH_SHORT;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import io.left.rightmesh.util.Logger;
import rightmesh.left.io.gpstracker.utils.LocationTracker;
import rightmesh.left.io.gpstracker.utils.PermissionUtil;

/**
 * An activity that listens to GPS updates and reports them back to the RightMesh SuperPeer.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private TextView tvNotification;

    private LocationTracker locationUtil;
    private PermissionUtil permissionUtil;

    private MainViewModel viewModel;

    /**
     * Initializes references to androidMeshManager and sets up the GPS location listeners.
     *
     * @param savedInstanceState data that can be used for instance recreation. Null if no state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvNotification = findViewById(R.id.tv_notification);

        initViewModel(savedInstanceState);
        observeViewModel();

        Logger.log(TAG, "ON CREATE");

        locationUtil = new LocationTracker(this, getLifecycle())
                .setInterval(1000)
                .setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        askPermission();
    }

    /**
     * Init viewmodel.
     *
     * @param savedInstanceState avedInstanceState – If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it
     *                           most recently supplied in
     */
    private void initViewModel(Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if (savedInstanceState == null) {
            viewModel.init(getLifecycle());
        }
    }

    /**
     * Binding data from viewmodel to UI.
     */
    private void observeViewModel() {
        viewModel.liveDataNotificationText.observe(this, s -> {
            tvNotification.setText(s);
        });
        viewModel.liveDataMsgToast.observe(this, s -> {
            Toast.makeText(
                    getApplicationContext(),
                    s,
                    LENGTH_SHORT
            ).show();
        });
        viewModel.liveDataPeerChangeEvent.observe(this, rightMeshEvent -> {
            permissionUtil.check();
        });
    }

    /**
     * Ask ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION. If granted, request update location.
     */
    private void askPermission() {
        permissionUtil = new PermissionUtil(this)
                .addLifeCycleOwner(getLifecycle())
                .addPermissions(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
                .callback(new PermissionUtil.PermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        registerLocationListener();
                        locationUtil.getLastLocation(
                                location -> {
                                    viewModel.sendLocationToSuperPeer(location);
                                }, e -> {
                                    Log.e(TAG, e.toString());
                                });
                    }

                    @Override
                    public void onDenied(@NonNull String[] deniedPermissions) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Need permissions to run",
                                Toast.LENGTH_LONG
                        ).show();
                        MainActivity.this.finish();
                    }
                });
    }

    /**
     * Checks that GPS permissions were granted, otherwise alerts the user
     * that we need permissions to run and then finishes the activity.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        permissionUtil.handleResult(requestCode, permissions, grantResults);
    }

    /**
     * Spins on permissions checks and does not progress to registering
     * a LocationListener unless the user allows.
     * Then registers the listener (we only care about location change here).
     */
    private void registerLocationListener() {
        locationUtil.requestLocationUpdate(new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (locationAvailability.isLocationAvailable()) {
                    tvNotification.setText(getString(R.string.sending_your_gps_to_app_superpeer));
                } else {
                    tvNotification.setText(getString(R.string.location_is_unavailable));
                    if (!locationUtil.isLocationProviderAvailable(getApplicationContext())) {
                        locationUtil.showDialogEnableGPS();
                    }
                }
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    viewModel.sendLocationToSuperPeer(location);
                }
            }
        });
    }
}
