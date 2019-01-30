package rightmesh.left.io.gpstracker.utils;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * A helper class to ask for permission
 *
 * @NOTE: always call handleResult() on onRequestPermissionsResult()
 */
public class PermissionUtil implements LifecycleObserver {
    private static final int PERMISSION_REQUEST_CODE = 97;

    private String[] permissions;

    private PermissionCallback callback;

    private Activity activity;
    private Lifecycle lifecycle;

    public PermissionUtil(Activity activity) {
        this.activity = activity;
    }

    public PermissionUtil addPermissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    public PermissionUtil callback(PermissionCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     *
     * @param lifecycle to observe lifecycle
     * @return {@link PermissionUtil}
     */
    public PermissionUtil addLifeCycleOwner(Lifecycle lifecycle){
        this.lifecycle = lifecycle;
        if (this.lifecycle != null) {
            this.lifecycle.addObserver(this);
        }
        return this;
    }

    private boolean isPermissionAllGranted(){
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public void check() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isPermissionAllGranted()) {
            ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
        } else {
            if (callback != null) {
                callback.onAllGranted();
            }
        }
    }

    public void handleResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            ArrayList<String> grantedPermissions = new ArrayList<>();
            ArrayList<String> deniedPermission = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permissions[i]);
                } else {
                    deniedPermission.add(permissions[i]);
                }
            }
            if (callback != null) {
                if(grantedPermissions.size() == permissions.length){
                    callback.onAllGranted();
                }else{
                    callback.onDenied(deniedPermission.toArray(new String[0]));
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        callback = null;

    }

    public interface PermissionCallback {
        void onAllGranted();
        void onDenied(@NonNull String[] deniedPermissions);
    }
}
