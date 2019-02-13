package rightmesh.left.io.gpstracker;

import android.app.Application;
import android.location.Location;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.util.RightMeshException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MainViewModelTest {
    // Executes each task synchronously using Architecture Components.
    //Using for testing Android ViewModel
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock
    private Application application;
    @Mock
    private RightMeshConnector rightMeshConnector;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<RightMeshConnector.OnConnectSuccessListener> onConnectSuccessCaptor;
    @Captor
    private ArgumentCaptor<RightMeshConnector.OnPeerChangedListener> onPeerChangedCaptor;

    private MainViewModel SUT;

    @Before
    public void setUp(){
        SUT = new MainViewModel(application);
        SUT.setRightMeshConnector(rightMeshConnector);
    }

//    @Test
//    public void init_isCall(){
//        MainViewModel spyMainViewModel = spy(SUT);
//
//        spyMainViewModel.init(Mockito.mock(Lifecycle.class));
//
//        MeshId meshId = Mockito.mock(MeshId.class);
//        MeshManager.RightMeshEvent rmEvent =
//                Mockito.mock(MeshManager.RightMeshEvent.class);
//
//        // Callback is captured and invoked with stubbed MeshId
//        verify(rightMeshConnector).setOnConnectSuccessListener(onConnectSuccessCaptor.capture());
//        onConnectSuccessCaptor.getValue().onConnectSucess(meshId);
//
//        verify(rightMeshConnector).setOnPeerChangedListener(onPeerChangedCaptor.capture());
//        onPeerChangedCaptor.getValue().onPeerChange(rmEvent);
//
//        Assert.assertSame(SUT.liveDataNotificationText.getValue(),
//                "Connected! Fetching current location...");
//        Assert.assertNotNull(SUT.liveDataPeerChangeEvent);
//        verify(rightMeshConnector).connect(any(), eq(Constants.SUPER_PEER_URL));
//    }

    @Test
    public void sendLocationToSuperPeer_nonNullLocation() throws RightMeshException {
        SUT.sendLocationToSuperPeer(Mockito.mock(Location.class));

        Assert.assertSame(SUT.liveDataNotificationText.getValue(),
                "Sending your GPS to App SuperPeer!");

        verify(rightMeshConnector).sentDataReliable(any(), any());
    }
}