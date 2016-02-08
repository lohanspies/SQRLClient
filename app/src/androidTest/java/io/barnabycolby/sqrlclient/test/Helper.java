package io.barnabycolby.sqrlclient.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.support.test.espresso.Espresso;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import io.barnabycolby.sqrlclient.activities.CreateNewIdentityActivity;
import io.barnabycolby.sqrlclient.R;
import io.barnabycolby.sqrlclient.test.activities.CreateNewIdentityActivityTest;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class Helper {

    public static void createNewIdentity(final String identityName) throws Exception {
        // Create an activity monitor so that we can retrieve an instance of any activities we start
        CreateNewIdentityActivity createNewIdentityActivity = (CreateNewIdentityActivity)monitorForActivity(CreateNewIdentityActivity.class, 5000, new Lambda() {
            public void run() throws Exception {
                onView(withId(R.id.CreateNewIdentityButton)).perform(click());
                UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
                CreateNewIdentityActivityTest.allowCameraPermissions(device);
                onView(withId(R.id.IdentityNameEditText)).perform(typeText(identityName));
                Espresso.closeSoftKeyboard();
            }
        });

        // We need to pass an instance of the initialised activity to waitForEntropyCollectionToFinish
        CreateNewIdentityActivityTest.waitForEntropyCollectionToFinish(createNewIdentityActivity);
        onView(withId(R.id.CreateNewIdentityButton)).perform(click());
    }

    /**
     * Monitors a lambda to catch a specific activity if it is launched.
     *
     * @param activityToCheckFor  The class of the activity to monitor for.
     * @param timeOut  The length of time to wait for the activity for once the lambda has finished execution.
     * @param lambda  The code to monitor.
     * @throws Exception  If the lambda throws an exception.
     */
    public static Activity monitorForActivity(Class activityToCheckFor, int timeOut, Lambda lambda) throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        ActivityMonitor activityMonitor = instrumentation.addMonitor(activityToCheckFor.getName(), null, false);

        lambda.run();

        Activity activity = instrumentation.waitForMonitorWithTimeout(activityMonitor, timeOut);
        instrumentation.removeMonitor(activityMonitor);
        return activity;
    }

    public static interface Lambda {
        public void run() throws Exception;
    }
}