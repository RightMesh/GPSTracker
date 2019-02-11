package rightmesh.left.io.gpstracker;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Lifecycle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.GenericTypeExtractor;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.RightMeshException;
import io.reactivex.functions.Consumer;

import static io.left.rightmesh.mesh.MeshManager.DATA_RECEIVED;
import static io.left.rightmesh.mesh.MeshManager.PEER_CHANGED;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

    private RightMeshConnector SUT;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        SUT = new RightMeshConnector(MESH_PORT, lifecycle);
        SUT.setAndroidMeshManager(androidMeshManager);
        SUT.setOnConnectSuccessListener(onConnectSuccessListener);
        SUT.setOnDataReceiveListener(onDataReceiveListener);
        SUT.setOnPeerChangedListener(onPeerChangedListener);
    }

    @Test
    public void meshStateChanged_successEvent() throws RightMeshException, ClassNotFoundException {
        RightMeshConnector spyRightMeshConnector = Mockito.spy(SUT);

        //Trigger
        spyRightMeshConnector.meshStateChanged(meshId, MeshStateListener.SUCCESS);

        //Verify
        verify(androidMeshManager).bind(MESH_PORT);
//        verify(androidMeshManager).on(DATA_RECEIVED, any(clazz));
//        verify(androidMeshManager).on(PEER_CHANGED, event -> {});
        verify(spyRightMeshConnector).meshStateChanged(meshId, MeshStateListener.SUCCESS);
    }
}