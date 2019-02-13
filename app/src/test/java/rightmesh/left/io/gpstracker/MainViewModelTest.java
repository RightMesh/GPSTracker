package rightmesh.left.io.gpstracker;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

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

    private MainViewModel SUT;

    @Before
    public void setUp() {
        SUT = new MainViewModel(application);
        SUT.setRightMeshConnector(rightMeshConnector);
    }

    @Test
    public void sendLocationToSuperPeer_nonNullLocation() throws RightMeshException {
        SUT.sendLocationToSuperPeer(Mockito.mock(Location.class));

        Assert.assertSame(SUT.liveDataNotificationText.getValue(),
                "Sending your GPS to App SuperPeer!");

        verify(rightMeshConnector).sentDataReliable(any(), any());
    }
}
