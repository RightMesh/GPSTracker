package rightmesh.left.io.gpstracker;

import android.app.Activity;
import android.app.Application;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.IntegerRes;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class,
        sdk = 23)
public abstract class AndroidTest<T extends Activity> {
    protected T activity;

    protected Application app;

    /**
     * Set up activity and app before each test method.
     */
    @Before
    public void setUp() {
        app = RuntimeEnvironment.application;
    }

    protected <V extends View> V findViewById(@IdRes int id) {
        return activity.findViewById(id);
    }

    /**
     * Get String from string resource.
     *
     * @param intRes String Id
     * @return string
     */
    protected String getString(@IntegerRes int intRes) {
        return app.getString(intRes);
    }
}
