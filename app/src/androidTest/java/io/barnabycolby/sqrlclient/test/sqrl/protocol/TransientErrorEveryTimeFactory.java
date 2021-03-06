package io.barnabycolby.sqrlclient.test.sqrl.protocol;

import io.barnabycolby.sqrlclient.exceptions.*;
import io.barnabycolby.sqrlclient.sqrl.protocol.SQRLConnection;
import io.barnabycolby.sqrlclient.sqrl.protocol.SQRLResponse;
import io.barnabycolby.sqrlclient.sqrl.factories.SQRLResponseFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.Mockito.*;

public class TransientErrorEveryTimeFactory implements SQRLResponseFactory {
    private String lastServerResponse;

    public TransientErrorEveryTimeFactory(String lastServerResponse) {
        this.lastServerResponse = lastServerResponse;
    }

    public SQRLResponse create(SQRLConnection connection) throws IOException, VersionNotSupportedException, InvalidServerResponseException, CommandFailedException, TransientErrorException {
        throw new TransientErrorException(getNut(), getQry(), getLastServerResponse());
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
