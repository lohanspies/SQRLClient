package io.barnabycolby.sqrlclient.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.barnabycolby.sqrlclient.R;

/**
 * This activity asks the user to enter the password for the selected identity, and then proceeds to verify it.
 */
public class EnterPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard Android stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);
    }
}