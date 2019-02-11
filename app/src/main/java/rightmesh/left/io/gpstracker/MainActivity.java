package rightmesh.left.io.gpstracker;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.widget.Toast.LENGTH_SHORT;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.nio.ByteBuffer;

import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.util.Logger;
import io.left.rightmesh.util.RightMeshException;
import rightmesh.left.io.gpstracker.utils.PermissionUtil;

/**
 * An activity that listens to GPS updates and reports them back to the RightMesh SuperPeer.
 */

public class MainActivity extends AppCompatActivity{
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int MESH_PORT = 5001;
    private static final int DOUBLE_NUM_BYTES = Double.SIZE / Byte.SIZE;
    private static final String SUPER_PEER_ID = "0x656284abf20af4192d86f2f6f3e7ce04e5718302";

    // TODO: fill in with your SuperPeer URL
    private static final String SUPER_PEER_URL = "192.168.3.151";

    private TextView tvNotification;

    private LocationTracker locationUtil;
    private PermissionUtil permissionUtil;

    private RightMeshConnector rightMeshConnector;

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

        Logger.log(TAG, "ON CREATE");

        initRightMeshConnector();

        locationUtil = new LocationTracker(this, getLifecycle())
                .setInterval(1000)
                .setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        askPermission();
    }

    /**
     * Init {@link RightMeshConnector}
     */
    private void initRightMeshConnector() {
        rightMeshConnector = new RightMeshConnector(MESH_PORT, getLifecycle());
        rightMeshConnector.setOnConnectSuccessListener(meshId -> {
            tvNotification.setText("Connected! Fetching current location...");
        });
        rightMeshConnector.setOnPeerChangedListener(event -> {
            /**
             * If the mesh state change is a SUCCESS bind to our MESH_PORT.
             * Registers a listener on PEER_CHANGED event to register our LocationListener;
             * we assume at this point the Mesh service is available and running.
             * Finally we send our current location to our SuperPeer.
             */
            runOnUiThread(() -> permissionUtil.check());
        });
        rightMeshConnector.connect(getApplicationContext(), SUPER_PEER_URL);
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
                                MainActivity.this::sendLocationToSuperPeer, e -> {
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
     * Fills a buffer with just lat and long double values, and sends
     * to the super peer assuming it runs on the same mesh port as us.
     *
     * @param location location that will be sent over RightMesh to the SuperPeer
     */
    private void sendLocationToSuperPeer(Location location) {
        if (location == null) {
            Log.d(TAG, "location is null");
            return;
        }

        tvNotification.setText("Sending your GPS to App SuperPeer!");

        ByteBuffer buffer = ByteBuffer.allocate(DOUBLE_NUM_BYTES + DOUBLE_NUM_BYTES);
        buffer.putDouble(location.getLatitude());
        buffer.putDouble(location.getLongitude());
        try {
            // TODO: fill in with your local SuperPeer MeshId
            MeshId hardcodedSuperPeerId = MeshId.fromString(SUPER_PEER_ID);
            int dataId = rightMeshConnector.sentDataReliable(hardcodedSuperPeerId,
                    buffer.array());
            Logger.log(TAG, "Sent to dataID: " + dataId);
        } catch (RightMeshException e) {
            Logger.log(TAG, "Failed to send location: " + location.toString());
        }
        Toast.makeText(
                getApplicationContext(),
                "Sent lat: "
                        + location.getLatitude()
                        + ", long: "
                        + location.getLongitude()
                        + " to SuperPeer",
                LENGTH_SHORT
        ).show();
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
                    tvNotification.setText("Sending your GPS to App SuperPeer!");
                } else {
                    tvNotification.setText("Location is unavailable");
                    if (!locationUtil.isLocationProviderAvailable(getApplicationContext())) {
                        locationUtil.showDialogEnableGPS();
                    }
                }
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    sendLocationToSuperPeer(location);
                }
            }
        });
    }
}
