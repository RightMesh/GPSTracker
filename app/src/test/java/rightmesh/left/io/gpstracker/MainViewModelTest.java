package rightmesh.left.io.gpstracker;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.util.RightMeshException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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

    private MainViewModel spyMainViewModel;

    /**
     * Set up spyMianViewModel before each test.
     */
    @Before
    public void setUp() {
        MainViewModel SUT = new MainViewModel(application);
        SUT.setRightMeshConnector(rightMeshConnector);

        spyMainViewModel = spy(SUT);
    }

    @Test
    public void init_isCall() {
        doReturn(rightMeshConnector).when(spyMainViewModel).buildRightMeshConnector(any());
        when(application.getString(R.string.fetching_location)).thenReturn("fetching location");

        spyMainViewModel.init(any());

        MeshId meshId = Mockito.mock(MeshId.class);
        MeshManager.RightMeshEvent rmEvent =
                Mockito.mock(MeshManager.RightMeshEvent.class);

        // Callback is captured and invoked with stubbed MeshId
        verify(rightMeshConnector).setOnConnectSuccessListener(onConnectSuccessCaptor.capture());
        onConnectSuccessCaptor.getValue().onConnectSucess(meshId);

        verify(rightMeshConnector).setOnPeerChangedListener(onPeerChangedCaptor.capture());
        onPeerChangedCaptor.getValue().onPeerChange(rmEvent);

        Assert.assertEquals(spyMainViewModel.liveDataNotificationText.getValue(),
                application.getString(R.string.fetching_location));
        Assert.assertEquals(spyMainViewModel.liveDataPeerChangeEvent.getValue(), rmEvent);
        verify(rightMeshConnector).connect(any(), eq(Constants.SUPER_PEER_URL));
    }

    @Test
    public void sendLocationToSuperPeer_nonNullLocation() throws RightMeshException {
        doReturn("Sending GPS").when(application)
                .getString(R.string.sending_your_gps_to_app_superpeer);

        spyMainViewModel.sendLocationToSuperPeer(Mockito.mock(Location.class));

        Assert.assertSame(spyMainViewModel.liveDataNotificationText.getValue(),
                application.getString(R.string.sending_your_gps_to_app_superpeer));
        verify(rightMeshConnector).sentDataReliable(any(), any());
    }
}
