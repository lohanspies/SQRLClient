package io.barnabycolby.sqrlclient.test.sqrl;

import io.barnabycolby.sqrlclient.sqrl.SQRLConnection;
import io.barnabycolby.sqrlclient.sqrl.SQRLIdentity;
import io.barnabycolby.sqrlclient.sqrl.SQRLRequest;
import io.barnabycolby.sqrlclient.sqrl.SQRLResponseFactory;

import java.net.HttpURLConnection;

/**
 * Allows easy creation and sending of a SQRL request to be sent to a server.
 */
public class SQRLTestRequest extends SQRLRequest {

    private SQRLConnection sqrlConnection;
    private SQRLIdentity sqrlIdentity;
    private SQRLResponseFactory sqrlResponseFactory;
    private HttpURLConnection connection;
    private boolean serverUnlockAndVerifyUnlockKeysRequired;

    /**
     * Constructs a new SQRLRequest object.
     *
     * @param sqrlConnection  The SQRL connection to send the request over.
     * @param sqrlIdentity  The identity to use for server communication.
     * @param sqrlResponseFactory  The factory to use when creating a new response object.
     */
    public SQRLTestRequest(SQRLConnection sqrlConnection, SQRLIdentity sqrlIdentity, SQRLResponseFactory sqrlResponseFactory, boolean serverUnlockAndVerifyUnlockKeysRequired) {
        super(sqrlConnection, sqrlIdentity, sqrlResponseFactory);
        this.serverUnlockAndVerifyUnlockKeysRequired = serverUnlockAndVerifyUnlockKeysRequired;
    }

    @Override
    protected boolean areServerUnlockAndVerifyUnlockKeysRequired() {
        return this.serverUnlockAndVerifyUnlockKeysRequired;
    }

    @Override
    protected String getCommandString() {
        return "test";
    }
}