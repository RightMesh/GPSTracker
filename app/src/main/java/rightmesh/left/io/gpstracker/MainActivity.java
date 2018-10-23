package rightmesh.left.io.gpstracker;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.widget.Toast.LENGTH_SHORT;
import static io.left.rightmesh.mesh.MeshManager.PEER_CHANGED;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager.RightMeshEvent;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.Logger;
import io.left.rightmesh.util.RightMeshException;
import io.left.rightmesh.util.RightMeshException.RightMeshServiceDisconnectedException;

import java.nio.ByteBuffer;

/**
 * An activity that listens to GPS updates and reports them back to the RightMesh SuperPeer.
 */

public class MainActivity extends AppCompatActivity implements MeshStateListener {
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int MESH_PORT = 5001;
    private static final int DOUBLE_NUM_BYTES = Double.SIZE / Byte.SIZE;
    private static final int GPS_PERMISSIONS_REQUEST_CODE = 9898;
    private static final String SUPER_PEER_ID = "0x656284abf20af4192d86f2f6f3e7ce04e5718302";

    // TODO: fill in with your SuperPeer URL
    private static final String SUPER_PEER_URL = "192.168.3.151";

    private AndroidMeshManager androidMeshManager;

    /**
     * Initializes references to androidMeshManager and sets up the GPS location listeners.
     *
     * @param savedInstanceState data that can be used for instance recreation. Null if no state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.log(TAG, "ON CREATE");

        androidMeshManager = AndroidMeshManager
                .getInstance(MainActivity.this,
                        MainActivity.this,
                        null,
                        SUPER_PEER_URL);
    }

    /**
     * Checks that GPS permissions were granted, otherwise alerts the user
     * that we need permissions to run and then finishes the activity.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case GPS_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Need permissions to run",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                }
            }
            break;
            default:
                Logger.log(TAG, "Non GPS permissions request code");
                break;
        }
    }

    /**
     * Fills a buffer with just lat and long double values, and sends
     * to the super peer assuming it runs on the same mesh port as us.
     *
     * @param location location that will be sent over RightMesh to the SuperPeer
     */
    private void sendLocationToSuperPeer(Location location) {
        ByteBuffer buffer = ByteBuffer.allocate(DOUBLE_NUM_BYTES + DOUBLE_NUM_BYTES);
        buffer.putDouble(location.getLatitude());
        buffer.putDouble(location.getLongitude());
        try {
            // TODO: fill in with your local SuperPeer MeshId
            MeshId hardcodedSuperPeerId = MeshId.fromString(SUPER_PEER_ID);
            int dataId = androidMeshManager
                    .sendDataReliable(hardcodedSuperPeerId, MESH_PORT, buffer.array());
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
     * Resume RightMesh connection on activity resume.
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            androidMeshManager.resume();
        } catch (RightMeshServiceDisconnectedException e) {
            Logger.fatal(TAG, "Service disconnected before resuming AndroidMeshManager with message"
                    + e.getMessage());
        }
    }

    /**
     * Close RightMesh connection when activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            androidMeshManager.stop();
        } catch (RightMeshServiceDisconnectedException e) {
            Logger.fatal(TAG, "Service disconnected before stopping AndroidMeshManager with message"
                    + e.getMessage());
        }
    }

    /**
     * If the mesh state change is a SUCCESS bind to our MESH_PORT.
     * Registers a listener on PEER_CHANGED event to register our LocationListener;
     * we assume at this point the Mesh service is available and running.
     * Finally we send our current location to our SuperPeer.
     */
    @Override
    public void meshStateChanged(MeshId meshId, int state) {
        if (state == SUCCESS) {
            try {
                // Attempt to bind to a port.
                androidMeshManager.bind(MESH_PORT);
                androidMeshManager.on(PEER_CHANGED, (RightMeshEvent event) -> {
                    registerLocationListener();
                    sendLocationToSuperPeer(getCurrentLocation());
                });
            } catch (RightMeshServiceDisconnectedException sde) {
                Logger.fatal(TAG, "Service disconnected while binding, with message: "
                        + sde.getMessage());
            } catch (RightMeshException rme) {
                Logger.fatal(TAG, "MeshPort already bound, with message: " + rme.getMessage());
            }
        }
    }

    /**
     * Spins on permissions checks and does not progress to registering
     * a LocationListener unless the user allows.
     * Then registers the listener (we only care about location change here).
     */
    private void registerLocationListener() {
        while (ActivityCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                        this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            // We do not have permissions to access the device location
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_FINE_LOCATION,
                            ACCESS_COARSE_LOCATION},
                    GPS_PERMISSIONS_REQUEST_CODE);
        }
        ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE)).requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        sendLocationToSuperPeer(location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                });
    }

    /**
     * If the mesh state change is a SUCCESS bind to our MESH_PORT.
     * Registers a listener on PEER_CHANGED event to register our LocationListener;
     * we assume at this point the Mesh service is available and running.
     * Finally we send our current location to our SuperPeer.
     */
    private Location getCurrentLocation() {
        while (ActivityCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    GPS_PERMISSIONS_REQUEST_CODE);
        }
        return ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE))
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
}
