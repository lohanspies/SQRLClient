package io.barnabycolby.sqrlclient.test.sqrl.protocol;

import io.barnabycolby.sqrlclient.exceptions.*;
import io.barnabycolby.sqrlclient.sqrl.protocol.SQRLConnection;
import io.barnabycolby.sqrlclient.sqrl.protocol.SQRLResponse;
import io.barnabycolby.sqrlclient.sqrl.factories.SQRLResponseFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import static org.mockito.Mockito.*;

public class TransientErrorRetryThenSucceedFactory implements SQRLResponseFactory {
    private boolean firstCall = true;
    private SQRLConnection mockConnection;
    private OutputStream spyOutputStream;
    private String lastServerResponse;

    public TransientErrorRetryThenSucceedFactory(SQRLConnection mockConnection, String lastServerResponse) {
        this.mockConnection = mockConnection;
        this.lastServerResponse = lastServerResponse;
    }

    public SQRLResponse create(SQRLConnection connection) throws IOException, VersionNotSupportedException, InvalidServerResponseException, CommandFailedException, TransientErrorException {
        if (firstCall) {
            firstCall = false;

            // We restub the SQRLConnection.getOutputStream method, to return a brand new output stream
            // This is so that we can see the data sent AFTER the exception was thrown
            this.spyOutputStream = spy(new ByteArrayOutputStream());
            HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
            when(this.mockConnection.getConnection()).thenReturn(httpURLConnection);
            when(httpURLConnection.getOutputStream()).thenReturn(spyOutputStream);

            throw new TransientErrorException(getNut(), getQry(), getLastServerResponse());
        } else {
            return mock(SQRLResponse.class);
        }
    }

    public String getDataSentAfterException() {
        return this.spyOutputStream.toString();
    }

    public String getNut() {
        return "sqYNVbO3_OVKNtND42wd_A";
    }

    public String getQry() {
        return "/sqrl?nut=" + getNut();
    }

    public String getLastServerResponse() {
        return this.lastServerResponse;
    }
}
