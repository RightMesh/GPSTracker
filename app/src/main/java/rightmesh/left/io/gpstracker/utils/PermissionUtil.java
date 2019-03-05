package rightmesh.left.io.gpstracker.utils;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.ArrayList;

/**
 * A helper class to ask for permission.
 *
 * @NOTE: always call handleResult() on onRequestPermissionsResult()
 */
public class PermissionUtil implements LifecycleObserver {
    private static final int PERMISSION_REQUEST_CODE = 97;

    private String[] permissions;

    private PermissionCallback callback;

    private Activity activity;

    public PermissionUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * Add permission that need to be requested.
     *
     * @param permissions list of permission to ask for
     * @return @{@link PermissionUtil}
     */
    public PermissionUtil addPermissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Add callback to handle logic when permissions are granted or denied.
     *
     * @param callback provided
     * @return {@link PermissionUtil}
     */
    public PermissionUtil callback(PermissionCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Add a lifecyle so {@link PermissionUtil} will know when Activity/Fragment is destroyed.
     *
     * @param lifecycle to observe lifecycle
     * @return {@link PermissionUtil}
     */
    public PermissionUtil addLifeCycleOwner(Lifecycle lifecycle) {
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        return this;
    }

    /**
     * Check if permissions are all granted.
     *
     * @return true: all permissions are granted.<br>
     * false: one of permissions is denied.
     */
    private boolean isPermissionAllGranted() {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat
                        .checkSelfPermission(activity, permission) != PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Requesting permission.
     */
    public void check() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isPermissionAllGranted()) {
            ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
        } else {
            if (callback != null) {
                callback.onAllGranted();
            }
        }
    }

    /**
     * Always put this method in onRequestPermissionsResult
     * to handle result of permission requesting.
     * Pass the same provided params in onRequestPermissionsResult() to this method
     *
     * @param requestCode  The request code passed in.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     */
    public void handleResult(int requestCode,
                             @NonNull String[] permissions,
                             @NonNull int... grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            ArrayList<String> grantedPermissions = new ArrayList<>();
            ArrayList<String> deniedPermission = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    grantedPermissions.add(permissions[i]);
                } else {
                    deniedPermission.add(permissions[i]);
                }
            }
            if (callback != null) {
                if (grantedPermissions.size() == permissions.length) {
                    callback.onAllGranted();
                } else {
                    callback.onDenied(deniedPermission.toArray(
                            new String[deniedPermission.size()]));
                }
            }
        }
    }

    /**
     * Trigger automatically when Activity/Fragment is destroyed if
     * {@link Lifecycle} is provided in {@link PermissionUtil#addLifeCycleOwner(Lifecycle)}.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        callback = null;
        activity = null;
    }

    /**
     * Callback for requesting permission.
     */
    public interface PermissionCallback {
        void onAllGranted();

        void onDenied(@NonNull String... deniedPermissions);
    }
}
