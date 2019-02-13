package rightmesh.left.io.gpstracker;

import static io.left.rightmesh.mesh.MeshManager.DATA_RECEIVED;
import static io.left.rightmesh.mesh.MeshManager.PEER_CHANGED;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.Logger;
import io.left.rightmesh.util.RightMeshException;

/**
 * To communicate with Rightmesh service.
 */
public class RightMeshConnector implements MeshStateListener, LifecycleObserver {
    private static final String TAG = RightMeshConnector.class.getCanonicalName();

    /**
     * Data Receive Listener.
     */
    public interface OnDataReceiveListener {
        void onDataReceive(MeshManager.RightMeshEvent event);
    }

    /**
     * On Peer Change Listener.
     */
    public interface OnPeerChangedListener {
        void onPeerChange(MeshManager.RightMeshEvent event);
    }

    /**
     * On my {@link MeshId} receiving listener.
     */
    public interface OnConnectSuccessListener {
        void onConnectSucess(MeshId meshId);
    }

    private int meshPort;

    // Interface object for the RightMesh library.
    private AndroidMeshManager androidMeshManager;

    //listener for data receive event
    private OnDataReceiveListener dataReceiveListener;
    //listener for peer change event
    private OnPeerChangedListener peerchangedListener;
    //listener for my MeshId receiving event
    private OnConnectSuccessListener connectSuccessListener;

    /**
     * Constructor {@link RightMeshConnector}.
     *
     * @param meshPort Rightmesh Port
     * @param lifecycle to observe life-cycle
     */
    public RightMeshConnector(int meshPort, Lifecycle lifecycle) {
        this.meshPort = meshPort;
        lifecycle.addObserver(this);
    }

    /**
     * Connect to Rightmesh.
     *
     * @param context Should pass application context
     * @param superPeerUrl Superpeer URL
     */
    public void connect(Context context, String superPeerUrl) {
        androidMeshManager = AndroidMeshManager.getInstance(context,
                this, null, superPeerUrl);
    }

    /**
     * Configures event handlers and binds to a port when the RightMesh library is ready.
     *
     * @param meshId ID of this device
     * @param state  new state of the RightMesh library
     */
    @Override
    public void meshStateChanged(MeshId meshId, int state) {
        if (state == SUCCESS) {
            try {
                // Attempt to bind to a port.
                androidMeshManager.bind(meshPort);

                // Update the peers list.
                if (connectSuccessListener != null) {
                    connectSuccessListener.onConnectSucess(meshId);
                }

                // Bind RightMesh event handlers.
                androidMeshManager.on(DATA_RECEIVED, event -> {
                    if (dataReceiveListener != null) {
                        dataReceiveListener.onDataReceive(event);
                    }
                });
                androidMeshManager.on(PEER_CHANGED, event -> {
                    if (peerchangedListener != null) {
                        peerchangedListener.onPeerChange(event);
                    }
                });
            } catch (RightMeshException.RightMeshServiceDisconnectedException sde) {
                Log.e(TAG, "Service disconnected while binding, with message: "
                        + sde.getMessage());
            } catch (RightMeshException rme) {
                Log.e(TAG, "MeshPort already bound, with message: " + rme.getMessage());
            }
        }
    }

    /**
     * Set listener for data receive event.
     *
     * @param listener a callback
     */
    public void setOnDataReceiveListener(OnDataReceiveListener listener) {
        this.dataReceiveListener = listener;
    }

    /**
     * Set listener for peer change event.
     *
     * @param listener a callback
     */
    public void setOnPeerChangedListener(OnPeerChangedListener listener) {
        this.peerchangedListener = listener;
    }

    /**
     * Set listener for my MeshId receiving event.
     *
     * @param listener a callback
     */
    public void setOnConnectSuccessListener(OnConnectSuccessListener listener) {
        this.connectSuccessListener = listener;
    }

    /**
     * Navigate to Rightmesh Wallet app.
     *
     * @throws RightMeshException can't navigate
     */
    public void toRightMeshWalletActivty() throws RightMeshException {
        this.androidMeshManager.showSettingsActivity();
    }

    /**
     * Send data to target device.
     *
     * @param targetMeshId Target meshId.
     * @param payload      data need to send.
     * @return Data Id
     * @throws RightMeshException.RightMeshServiceDisconnectedException Service disconnected.
     * @throws RightMeshException                                       Can't find next hop.
     */
    public int sentDataReliable(MeshId targetMeshId, byte[] payload) throws RightMeshException,
            RightMeshException.RightMeshServiceDisconnectedException {
        return androidMeshManager.sendDataReliable(androidMeshManager.getNextHopPeer(targetMeshId),
                meshPort, payload);
    }

    /**
     * Close RightMesh connection when activity is destroyed.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void stop() {
        try {
            androidMeshManager.stop();
        } catch (RightMeshException.RightMeshServiceDisconnectedException e) {
            Logger.fatal(TAG, "Service disconnected before stopping AndroidMeshManager "
                    + "with message" + e.getMessage());
        }
    }

    /**
     * Resume RightMesh connection on activity resume.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resumse() {
        try {
            androidMeshManager.resume();
        } catch (RightMeshException.RightMeshServiceDisconnectedException e) {
            Logger.fatal(TAG, "Service disconnected before resuming AndroidMeshManager "
                    + "with message" + e.getMessage());
        }
    }

    /**
     * {@link AndroidMeshManager} setter used to testing purpose.
     * @param androidMeshManager - should pass Mock object.
     */
    public void setAndroidMeshManager(AndroidMeshManager androidMeshManager) {
        this.androidMeshManager = androidMeshManager;
    }
}
