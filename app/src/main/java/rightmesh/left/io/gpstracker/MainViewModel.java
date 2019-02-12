package rightmesh.left.io.gpstracker;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import android.location.Location;
import androidx.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;

import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.util.Logger;
import io.left.rightmesh.util.RightMeshException;

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getCanonicalName();

    private static final int MESH_PORT = 5001;
    private static final int DOUBLE_NUM_BYTES = Double.SIZE / Byte.SIZE;
    private static final String SUPER_PEER_ID = "0x656284abf20af4192d86f2f6f3e7ce04e5718302";

    // TODO: fill in with your SuperPeer URL
    private static final String SUPER_PEER_URL = "192.168.3.151";

    private RightMeshConnector rightMeshConnector;

    public MutableLiveData<String> liveDataNotificationText = new MutableLiveData<>();
    public MutableLiveData<String> liveDataMsgToast = new MutableLiveData<>();
    public MutableLiveData<MeshManager.RightMeshEvent>
            liveDataPeerChangeEvent = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(Lifecycle lifecycle){
        initRightMeshConnector(lifecycle);
    }

    /**
     * Init {@link RightMeshConnector}.
     */
    private void initRightMeshConnector(Lifecycle lifecycle) {
        rightMeshConnector = new RightMeshConnector(MESH_PORT, lifecycle);
        rightMeshConnector.setOnConnectSuccessListener(meshId ->
                liveDataNotificationText.setValue("Connected! Fetching current location...")
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
        rightMeshConnector.connect(getApplication(), SUPER_PEER_URL);
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

        liveDataNotificationText.setValue("Sending your GPS to App SuperPeer!");

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
        liveDataMsgToast.setValue("Sent lat: "
                + location.getLatitude()
                + ", long: "
                + location.getLongitude()
                + " to SuperPeer");
    }
}