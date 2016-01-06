package io.barnabycolby.sqrlclient.test.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.barnabycolby.sqrlclient.activities.CreateNewIdentityActivity;
import io.barnabycolby.sqrlclient.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class CreateNewIdentityActivityTest {
    private Activity mActivity;
    private UiDevice mDevice;

    @Rule
    public ActivityTestRule mActivityTestRule = new ActivityTestRule(CreateNewIdentityActivity.class);

    @Before
    public void setUp() throws Exception {
        this.mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        this.mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void testExplanationTextIsDisplayed() throws Exception {
        allowCameraPermissions();

        // Get the expected explanation text
        Resources resources = mActivity.getResources();
        String expected = resources.getString(R.string.create_new_identity_explanation);

        // Assert that the appropriate TextView is visible and shows the correct text
        TextView explanationTextView = (TextView)mActivity.findViewById(R.id.CreateNewIdentityExplanationTextView);
        assertEquals(expected, explanationTextView.getText());
        assertEquals(View.VISIBLE, explanationTextView.getVisibility());
    }

    @Test
    public void testProgressBarIsDisplayed() throws Exception {
        allowCameraPermissions();

        // Find the progress bar and make sure it's visible
        ProgressBar progressBar = (ProgressBar)mActivity.findViewById(R.id.EntropyHarvesterProgressBar);
        assertNotNull(progressBar);
        assertEquals(View.VISIBLE, progressBar.getVisibility());
    }

    @Test
    public void testIdentityNameEditTextIsDisplayed() throws Exception {
        allowCameraPermissions();

        // Find the textbox and make sure it's visible
        EditText identityNameEditText = (EditText)mActivity.findViewById(R.id.IdentityNameEditText); 
        assertNotNull(identityNameEditText);
        assertEquals(View.VISIBLE, identityNameEditText.getVisibility());
    }

    private void allowCameraPermissions() throws Exception {
        UiObject allowButton = mDevice.findObject(new UiSelector()
                .text("Allow")
                .className("android.widget.Button"));
        if (allowButton.exists()) {
            allowButton.click();
        }
    }
}
