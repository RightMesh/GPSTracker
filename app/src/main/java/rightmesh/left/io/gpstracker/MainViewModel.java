package rightmesh.left.io.gpstracker;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;

import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.util.Logger;
import io.left.rightmesh.util.RightMeshException;

import java.nio.ByteBuffer;

import rightmesh.left.io.gpstracker.utils.LocationTracker;
import rightmesh.left.io.gpstracker.utils.PermissionUtil;

public class MainViewModel extends AndroidViewModel {
    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.TESTS)
    public LocationTracker locationUtil;

    private static final String TAG = MainViewModel.class.getCanonicalName();

    private static final int DOUBLE_NUM_BYTES = Double.SIZE / Byte.SIZE;

    private RightMeshConnector rightMeshConnector;

    public MutableLiveData<String> liveDataNotificationText = new MutableLiveData<>();
    public MutableLiveData<String> liveDataMsgToast = new MutableLiveData<>();
    public MutableLiveData<MeshManager.RightMeshEvent>
            liveDataPeerChangeEvent = new MutableLiveData<>();

    private PermissionUtil permissionUtil;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(Lifecycle lifecycle) {
        initRightMeshConnector(lifecycle);
    }

    /**
     * Init {@link RightMeshConnector}.
     */
    private void initRightMeshConnector(Lifecycle lifecycle) {
        rightMeshConnector = buildRightMeshConnector(lifecycle);
        rightMeshConnector.setOnConnectSuccessListener(meshId ->
                liveDataNotificationText.setValue(
                        getApplication().getString(R.string.fetching_location))
        );
        rightMeshConnector.setOnPeerChangedListener(event -> {
            /**
             * If the mesh state change is a SUCCESS bind to our MESH_PORT.
             * Registers a listener on PEER_CHANGED event to register our LocationListener;
             * we assume at this point the Mesh service is available and running.
             * Finally we send our current location to our SuperPeer.
             */
            liveDataPeerChangeEvent.postValue(event); //postValue to UI thread
        });
        rightMeshConnector.connect(getApplication(), Constants.SUPER_PEER_URL);
    }

    /**
     * Build RightmeshConnector (to mock easier).
     *
     * @param lifecycle Activity/Fragment lifecycle
     * @return new {@link RightMeshConnector}
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    RightMeshConnector buildRightMeshConnector(Lifecycle lifecycle) {
        return new RightMeshConnector(Constants.MESH_PORT, lifecycle);
    }

    /**
     * Fills a buffer with just lat and long double values, and sends
     * to the super peer assuming it runs on the same mesh port as us.
     *
     * @param location location that will be sent over RightMesh to the SuperPeer
     */
    public void sendLocationToSuperPeer(Location location) {
        if (location == null) {
            Log.d(TAG, "location is null");
            return;
        }

        liveDataNotificationText.setValue(getApplication()
                .getString(R.string.sending_your_gps_to_app_superpeer));

        ByteBuffer buffer = ByteBuffer.allocate(DOUBLE_NUM_BYTES + DOUBLE_NUM_BYTES);
        buffer.putDouble(location.getLatitude());
        buffer.putDouble(location.getLongitude());
        try {
            // TODO: fill in with your local SuperPeer MeshId
            MeshId hardcodedSuperPeerId = MeshId.fromString(Constants.SUPER_PEER_ID);
            int dataId = rightMeshConnector.sentDataReliable(hardcodedSuperPeerId,
                    buffer.array());
            Logger.log(TAG, "Sent to dataID: " + dataId);
        } catch (RightMeshException e) {
            Logger.log(TAG, "Failed to send location: " + location.toString());
        }
        liveDataMsgToast.setValue("Sent lat: "
                + location.getLatitude()
                + ", long: "
                + location.getLongitude()
                + " to SuperPeer");
    }

    /**
     * Setter of {@link RightMeshConnector}.
     *
     * @param rightMeshConnector {@link RightMeshConnector}
     */
    public void setRightMeshConnector(RightMeshConnector rightMeshConnector) {
        this.rightMeshConnector = rightMeshConnector;
    }
}
