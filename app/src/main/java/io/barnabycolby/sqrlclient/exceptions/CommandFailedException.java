package io.barnabycolby.sqrlclient.exceptions;

/**
 * Signifies that the SQRL server responded with a command failed error, indicated by the tif flags.
 */
public class CommandFailedException extends SQRLException {
    public CommandFailedException(String message) {
        super(message);
    }
}
