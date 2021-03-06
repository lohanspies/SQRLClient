package io.barnabycolby.sqrlclient.test.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import io.barnabycolby.sqrlclient.activities.EnterPasswordActivity;
import io.barnabycolby.sqrlclient.activities.LoginActivity;
import io.barnabycolby.sqrlclient.App;
import io.barnabycolby.sqrlclient.helpers.Lambda;
import io.barnabycolby.sqrlclient.R;
import io.barnabycolby.sqrlclient.sqrl.SQRLIdentity;
import io.barnabycolby.sqrlclient.test.TestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.Test;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static io.barnabycolby.sqrlclient.test.helpers.OrientationChangeAction.orientationPortrait;
import static io.barnabycolby.sqrlclient.test.helpers.OrientationChangeAction.orientationLandscape;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class EnterPasswordActivityTest {

    private EnterPasswordActivity mActivity;
    private Uri mUri = Uri.parse("sqrl://www.grc.com/sqrl?nut=mCwPTJWrbcBNMJKc76sI8w&sfn=R1JD");

    private ViewInteraction mIdentitySpinner;
    private ViewInteraction mPasswordEditText;
    private ViewInteraction mVerifyProgressBar;
    private ViewInteraction mInformationTextView;
    private ViewInteraction mLoginButton;

    private class TestRule extends ActivityTestRule<EnterPasswordActivity> {
        public TestRule() {
            super(EnterPasswordActivity.class);
        }

        @Override
        public void beforeActivityLaunched() {
            // Set up the identities
            try {
                App.getSQRLIdentityManager().save("Harriet", new byte[32], "1hL!#0tAdhlgm4GA", null);
                App.getSQRLIdentityManager().save("Rupert", new byte[32], "SfZgFVhVE6s%t#C&", null);
                App.getSQRLIdentityManager().setCurrentIdentity("Harriet");
            } catch (Exception ex) {
                // Do nothing if an exception gets thrown
            }
        }

        @Override
        protected Intent getActivityIntent() {
            Intent intent = super.getActivityIntent();
            intent.setData(mUri);

            return intent;
        }
    }

    @Rule
    public TestRule mActivityTestRule = new TestRule();

    @Before
    public void setUp() throws Exception {
        this.mActivity = this.mActivityTestRule.getActivity();

        // Get espresso references to the UI components
        this.mIdentitySpinner = onView(withId(R.id.IdentitySpinner));
        this.mPasswordEditText = onView(withId(R.id.PasswordEditText));
        this.mVerifyProgressBar = onView(withId(R.id.VerifyProgressBar));
        this.mInformationTextView = onView(withId(R.id.InformationTextView));
        this.mLoginButton = onView(withId(R.id.LoginButton));
    }

    @After
    public void tearDown() throws Exception {
        App.getSQRLIdentityManager().removeAllIdentities();
    }

    @Test
    public void displaysCorrectInitialUIComponents() {
        this.mIdentitySpinner.check(matches(isDisplayed()));
        this.mIdentitySpinner.check(matches(isEnabled()));
        this.mPasswordEditText.check(matches(isDisplayed()));
        this.mVerifyProgressBar.check(matches(not(isDisplayed())));
        this.mInformationTextView.check(matches(isDisplayed()));
        this.mInformationTextView.check(matches(withText(R.string.enter_password_help)));
        this.mLoginButton.check(matches(isDisplayed()));
        this.mLoginButton.check(matches(not(isEnabled())));
    }

    @Test
    public void loginButtonEnabledIfPasswordNotBlank() {
        this.mPasswordEditText.perform(typeText("brains"), closeSoftKeyboard());
        this.mLoginButton.check(matches(isEnabled()));
        this.mPasswordEditText.perform(clearText());
        this.mLoginButton.check(matches(not(isEnabled())));
        this.mPasswordEditText.perform(typeText("zombies"), closeSoftKeyboard());
        this.mLoginButton.check(matches(isEnabled()));
    }

    @Test
    public void progressBarDisplayedOnceLoginClicked() {
        // Espresso waits for async tasks to complete before executing test steps
        // As we want to perform the UI check in the middle of this, we need to disable the async tasks
        this.mActivity.disableAsyncTasks();

        this.mPasswordEditText.perform(typeText("gorillas"), closeSoftKeyboard());
        this.mLoginButton.perform(click());
        this.mLoginButton.check(matches(not(isDisplayed())));
        this.mVerifyProgressBar.check(matches(isDisplayed()));
        this.mInformationTextView.check(matches(withText(R.string.verifying_password)));
        this.mPasswordEditText.check(matches(not(isEnabled())));
        this.mIdentitySpinner.check(matches(not(isEnabled())));
    }

    @Test
    public void uiStateSurvivesOrientationChange() throws Exception {
        // Espresso waits for async tasks to complete before executing test steps
        // As we want to perform the orientation change in the middle of this, we need to disable the async tasks
        this.mActivity.disableAsyncTasks();

        String password = "crocodile";
        this.mPasswordEditText.perform(typeText(password), closeSoftKeyboard());
        this.mLoginButton.perform(click());

        // Switch the orientation. As we don't know what the current orientation is then we switch to both landscape and portrait
        onView(isRoot()).perform(orientationLandscape());
        onView(isRoot()).perform(orientationPortrait());
       
        this.mPasswordEditText.check(matches(withText(password)));
        this.mLoginButton.check(matches(not(isDisplayed())));
        this.mVerifyProgressBar.check(matches(isDisplayed()));
        this.mInformationTextView.check(matches(withText(R.string.verifying_password)));
        this.mPasswordEditText.check(matches(not(isEnabled())));
        this.mIdentitySpinner.check(matches(not(isEnabled())));
    }

    @Test
    public void passwordIncorrectResetsUIAndDisplaysErrorMessage() throws Exception {
        this.mPasswordEditText.perform(typeText("liontamer"), closeSoftKeyboard());
        this.mLoginButton.perform(click());

        // The identity takes at least 5 seconds to decrypt, so we need to wait
        Thread.sleep(7000);

        this.mLoginButton.check(matches(isDisplayed()));
        this.mVerifyProgressBar.check(matches(not(isDisplayed())));
        this.mInformationTextView.check(matches(withText(R.string.incorrect_password)));
        this.mPasswordEditText.check(matches(isEnabled()));
        this.mPasswordEditText.check(matches(withText("")));
        this.mIdentitySpinner.check(matches(isEnabled()));
    }

    @Test
    public void passwordCorrectRedirectsToLoginActivity() throws Exception {
        this.mPasswordEditText.perform(typeText("1hL!#0tAdhlgm4GA"), closeSoftKeyboard());
        
        Activity loginActivity = TestHelper.monitorForActivity(LoginActivity.class, 10000, new Lambda() {
            public void run() {
                mLoginButton.perform(click());
            }
        });
        assertNotNull(loginActivity);

        // Check that the SQRLIdentity was passed along
        Bundle bundle = loginActivity.getIntent().getExtras();
        SQRLIdentity sqrlIdentity = bundle.getParcelable("sqrlIdentity");
        assertNotNull(sqrlIdentity);

        loginActivity.finish();
    }
}
