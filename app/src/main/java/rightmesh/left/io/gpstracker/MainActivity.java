package rightmesh.left.io.gpstracker;

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

import java.nio.ByteBuffer;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.Logger;
import io.left.rightmesh.util.RightMeshException;
import io.left.rightmesh.util.RightMeshException.RightMeshServiceDisconnectedException;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * An activity that listens to GPS updates and reports them back to the RightMesh SuperPeer.
 */

public class MainActivity extends AppCompatActivity implements MeshStateListener {
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int MESH_PORT = 5001;
    private static final int DOUBLE_NUM_BYTES = Double.SIZE / Byte.SIZE;

    // TODO: fill in with your SuperPeer URL
    private static final String SUPER_PEER_URL = "192.168.3.151";

    private AndroidMeshManager meshManager;

    private LocationListener locationListener;

    /**
     * Initializes references to meshManager and sets up the GPS location listeners
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.log(TAG, "ON CREATE");

        meshManager = AndroidMeshManager
                .getInstance(MainActivity.this,
                        MainActivity.this,
                        null,
                        SUPER_PEER_URL);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
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
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // We do not have permissions to access the device location
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    // Fills a buffer with just lat and long double values, and sends
    // to the super peer assuming it runs on the same mesh port as us
    private void sendLocationToSuperPeer(Location location) {
        ByteBuffer buffer = ByteBuffer.allocate(DOUBLE_NUM_BYTES + DOUBLE_NUM_BYTES);
        buffer.putDouble(location.getLatitude());
        buffer.putDouble(location.getLongitude());
        try {
            // TODO: fill in with your local SuperPeer MeshId
            MeshId hardcodedSuperPeerId = MeshId.fromString("0x656284abf20af4192d86f2f6f3e7ce04e5718302");
            meshManager.sendDataReliable(hardcodedSuperPeerId, MESH_PORT, buffer.array());
        } catch (RightMeshException e) {
            Logger.log(TAG, "Failed to send location: " + location.toString());
        }
        String text = "Sent lat: " + location.getLatitude() + ", long: " + location.getLongitude() + " to SuperPeer";
        Toast.makeText(getApplicationContext(), text, LENGTH_SHORT).show();
    }

    /**
     * Resume RightMesh connection on activity resume.
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            meshManager.resume();
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
            meshManager.stop();
        } catch (RightMeshServiceDisconnectedException e) {
            Logger.fatal(TAG, "Service disconnected before stopping AndroidMeshManager with message"
                    + e.getMessage());
        }
    }

    @Override
    public void meshStateChanged(MeshId meshId, int state) {
        if (state == SUCCESS) {
            try {
                // Attempt to bind to a port.
                meshManager.bind(MESH_PORT);
            } catch (RightMeshServiceDisconnectedException sde) {
                Logger.fatal(TAG, "Service disconnected while binding, with message: "
                        + sde.getMessage());
            } catch (RightMeshException rme) {
                Logger.fatal(TAG, "MeshPort already bound, with message: " + rme.getMessage());
            }
        }
    }
}
