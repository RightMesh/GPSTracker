package rightmesh.left.io.gpstracker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Activity;

import androidx.lifecycle.Lifecycle;

import com.google.android.gms.location.FusedLocationProviderClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import rightmesh.left.io.gpstracker.utils.LocationTracker;

@RunWith(MockitoJUnitRunner.class)
public class LocationTrackerTest {

    @Mock
    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationTracker spyLocationTracker;

    /**
     * Set SUT before each test case.
     */
    @Before
    public void setUp() {
        LocationTracker SUT = new LocationTracker(mock(Activity.class),
                mock(Lifecycle.class),
                fusedLocationProviderClient);

        SUT.setFusedLocationProviderClient(fusedLocationProviderClient);

        spyLocationTracker = Mockito.spy(SUT);
    }

    @Test
    public void requestLocationUpdate() {
        spyLocationTracker.requestLocationUpdate(any());

        verify(fusedLocationProviderClient).requestLocationUpdates(any(), any(), any());
        verify(spyLocationTracker).requestLocationUpdate(any());
    }
}
