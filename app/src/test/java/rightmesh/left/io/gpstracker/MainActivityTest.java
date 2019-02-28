package rightmesh.left.io.gpstracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Application;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class,
        sdk = 23)
public class MainActivityTest extends AndroidTest<MainActivity> {

    private TextView tvNotification;

    @Test
    public void checkActivityNotNull() {
        activity = Robolectric.setupActivity(MainActivity.class);
        tvNotification = findViewById(R.id.tv_notification);

        activity.getResources();
        assertNotNull(activity);
        assertEquals(tvNotification.getText(), getString(R.string.connecting_rightmesh_service));
    }
}
