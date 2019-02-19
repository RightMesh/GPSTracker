package rightmesh.left.io.gpstracker;

import android.app.Activity;
import android.content.pm.PackageManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import rightmesh.left.io.gpstracker.utils.PermissionUtil;

@RunWith(MockitoJUnitRunner.class)
public class PermissionUtilTest {
    private static final int PERMISSION_REQUEST_CODE = 97;

    private String[] permissions = new String[]{"permission1",
            "permission2"};

    @Mock
    private Activity activity;

    @Mock
    private PermissionUtil.PermissionCallback permissionCallback;

    private PermissionUtil spyPermissionUtil;

    /**
     * Set SUT before each test case.
     */
    @Before
    public void setUp() {
        PermissionUtil underTest = new PermissionUtil(activity);

        underTest.callback(permissionCallback);

        spyPermissionUtil = Mockito.spy(underTest);
    }

    @Test
    public void handleResult_onAllGranted() {
        int[] grantResults = new int[]{PackageManager.PERMISSION_GRANTED,
                PackageManager.PERMISSION_GRANTED};

        spyPermissionUtil.handleResult(PERMISSION_REQUEST_CODE, permissions, grantResults);

        //Check if method is called
        Mockito.verify(spyPermissionUtil).handleResult(Mockito.anyInt(),
                Mockito.eq(permissions), Mockito.eq(grantResults));

        Mockito.verify(permissionCallback).onAllGranted();
        Mockito.verify(permissionCallback, Mockito.never()).onDenied(Mockito.any());
        Mockito.verify(spyPermissionUtil).handleResult(Mockito.anyInt(),
                Mockito.eq(permissions),
                Mockito.eq(grantResults));
    }

    @Test
    public void handleResult_onDenied() {
        int[] grantResults = new int[]{PackageManager.PERMISSION_GRANTED,
                PackageManager.PERMISSION_DENIED};

        spyPermissionUtil.handleResult(PERMISSION_REQUEST_CODE, permissions, grantResults);

        //Check if method is called
        Mockito.verify(spyPermissionUtil).handleResult(Mockito.anyInt(),
                Mockito.eq(permissions), Mockito.eq(grantResults));

        Mockito.verify(permissionCallback, Mockito.never()).onAllGranted();
        Mockito.verify(permissionCallback).onDenied(Mockito.any());
        Mockito.verify(spyPermissionUtil).handleResult(Mockito.anyInt(),
                Mockito.eq(permissions),
                Mockito.eq(grantResults));
    }
}
