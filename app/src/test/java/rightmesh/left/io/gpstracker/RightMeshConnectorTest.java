package rightmesh.left.io.gpstracker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.RightMeshException;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RightMeshConnectorTest {
    private static final int MESH_PORT = 5001;

    @Mock
    private AndroidMeshManager androidMeshManager;
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
     * Init spy of underTest before running test.
     */
    @Before
    public void setUp() {
        RightMeshConnector underTest = new RightMeshConnector(MESH_PORT);
        underTest.setAndroidMeshManager(androidMeshManager);
        underTest.setOnConnectSuccessListener(onConnectSuccessListener);
        underTest.setOnDataReceiveListener(onDataReceiveListener);
        underTest.setOnPeerChangedListener(onPeerChangedListener);

        spyRightMeshConnector = Mockito.spy(underTest);
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
    public void sendDataReliable_isCalled() throws RightMeshException {
        String payload = "abc";

        spyRightMeshConnector.sendDataReliable(meshId,
                payload.getBytes(Charset.forName("UTF-8")));

        verify(androidMeshManager).sendDataReliable(any(), anyInt(),
                eq(payload.getBytes(Charset.forName("UTF-8"))));
        verify(spyRightMeshConnector).sendDataReliable(any(),
                eq(payload.getBytes(Charset.forName("UTF-8"))));
    }
}