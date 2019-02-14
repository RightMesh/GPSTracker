package rightmesh.left.io.gpstracker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Lifecycle;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.RightMeshException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RightMeshConnectorTest {
    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private static final int MESH_PORT = 5001;

    @Mock
    private AndroidMeshManager androidMeshManager;
    @Mock
    private Lifecycle lifecycle;
    @Mock
    private RightMeshConnector.OnDataReceiveListener onDataReceiveListener;
    @Mock
    private RightMeshConnector.OnPeerChangedListener onPeerChangedListener;
    @Mock
    private RightMeshConnector.OnConnectSuccessListener onConnectSuccessListener;
    @Mock
    private MeshId meshId;

    private RightMeshConnector spyRightMeshConnector;

    /**
     * Init spy of SUT before running test.
     */
    @Before
    public void setUp() {
        RightMeshConnector SUT = new RightMeshConnector(MESH_PORT, lifecycle);
        SUT.setAndroidMeshManager(androidMeshManager);
        SUT.setOnConnectSuccessListener(onConnectSuccessListener);
        SUT.setOnDataReceiveListener(onDataReceiveListener);
        SUT.setOnPeerChangedListener(onPeerChangedListener);

        spyRightMeshConnector = Mockito.spy(SUT);
    }

    @Test
    public void meshStateChanged_successEvent() throws RightMeshException, ClassNotFoundException {
        //Trigger
        spyRightMeshConnector.meshStateChanged(meshId, MeshStateListener.SUCCESS);

        //Verify
        verify(androidMeshManager).bind(MESH_PORT);
        verify(spyRightMeshConnector).meshStateChanged(meshId, MeshStateListener.SUCCESS);
    }

    @Test
    public void stop_isCalled() throws RightMeshException.RightMeshServiceDisconnectedException {
        spyRightMeshConnector.stop();

        verify(androidMeshManager).stop();
        verify(spyRightMeshConnector).stop();
    }

    @Test
    public void toRightMeshWalletActivty_isCalled() throws RightMeshException {
        spyRightMeshConnector.toRightMeshWalletActivty();

        verify(androidMeshManager).showSettingsActivity();
        verify(spyRightMeshConnector).toRightMeshWalletActivty();
    }

    @Test
    public void sentDataReliable_isCalled() throws RightMeshException {
        String payload = "abc";

        spyRightMeshConnector.sentDataReliable(meshId, payload.getBytes());

        verify(androidMeshManager).sendDataReliable(any(), anyInt(), eq(payload.getBytes()));
        verify(spyRightMeshConnector).sentDataReliable(any(), eq(payload.getBytes()));
    }
}