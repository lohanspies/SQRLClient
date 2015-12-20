package io.barnabycolby.sqrlclient.sqrl.tasks;

import android.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.barnabycolby.sqrlclient.App;
import io.barnabycolby.sqrlclient.exceptions.SQRLException;
import io.barnabycolby.sqrlclient.helpers.TestableAsyncTask;
import io.barnabycolby.sqrlclient.R;
import io.barnabycolby.sqrlclient.dialogs.CreateAccountDialogFactory;
import io.barnabycolby.sqrlclient.dialogs.CreateAccountDialogFragment;
import io.barnabycolby.sqrlclient.sqrl.IdentRequestListener;
import io.barnabycolby.sqrlclient.sqrl.SQRLQueryRequest;
import io.barnabycolby.sqrlclient.sqrl.SQRLRequestFactory;
import io.barnabycolby.sqrlclient.sqrl.SQRLResponse;

import java.io.IOException;

/**
 * An AsyncTask that querys a SQRL server to determine whether a user account already exists, setting the text of a TextView to indicate the
 * result.
 */
public class AccountExistsTask extends TestableAsyncTask<Void, Void, Boolean> {
    private static final String TAG = AccountExistsTask.class.getName();

    private SQRLRequestFactory sqrlRequestFactory;
    private TextView accountExistsTextView;
    private CreateAccountDialogFactory createAccountDialogFactory;
    private IdentRequestListener identRequestListener;

    /**
     * Constructs an instance of the AccountExistsTask.
     *
     * @param sqrlRequestFactory  The factory used to create the SQRLRequest object used to query the server.
     * @param accountExistsTextView  The text view used to indicate whether the account exists or not.
     * @param createAccountDialogFactory  The factory used to create the create account dialog if needed.
     * @param identRequestListener  The listener that should be called when proceeding with the identity request.
     */
    public AccountExistsTask(SQRLRequestFactory sqrlRequestFactory, TextView accountExistsTextView, CreateAccountDialogFactory createAccountDialogFactory, IdentRequestListener identRequestListener) {
        this.sqrlRequestFactory = sqrlRequestFactory;
        this.accountExistsTextView = accountExistsTextView;
        this.createAccountDialogFactory = createAccountDialogFactory;
        this.identRequestListener = identRequestListener;
    }

    /**
     * Sets the initial text of the text view and ensures it's visible.
     */
    protected void onPreExecute() {
        String contactingServerText = App.getApplicationResources().getString(R.string.contacting_server);
        this.accountExistsTextView.setText(contactingServerText);
        this.accountExistsTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Performs the query request and returns the result indicating whether the account exists or not.
     *
     * @return Null if an exception occurred when communicating with the server. True if the account exists and false if the account does not exist. 
     */
    protected Boolean doInBackground(Void... params) {
        try {
            // Perform the query and return the result
            SQRLQueryRequest request = this.sqrlRequestFactory.createQuery();
            SQRLResponse response = request.send();
            return Boolean.valueOf(response.currentAccountExists());
        } catch (SQRLException | IOException ex) {
            Log.e(TAG, "Account exists task failed: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Uses the result to set the text of the text view.
     *
     * @param result  The result of the execution.
     */
    @Override
    protected void onPostExecute(Boolean result) {
        String textToSet;

        if (result == null) {
            textToSet = App.getApplicationResources().getString(R.string.something_went_wrong);
        } else if (result.booleanValue()) {
            textToSet = App.getApplicationResources().getString(R.string.account_exists);
        } else {
            textToSet = App.getApplicationResources().getString(R.string.account_does_not_exist);
        }

        this.accountExistsTextView.setText(textToSet);

        if (result != null) {
            // Create the dialog based on the result
            boolean accountExists = result.booleanValue();
            if (accountExists) {
                this.identRequestListener.proceedWithIdentRequest();
            } else {
                this.createAccountDialogFactory.create();
            }
        }

        // Signal that all execution has finished
        this.executionFinished();
    }
}