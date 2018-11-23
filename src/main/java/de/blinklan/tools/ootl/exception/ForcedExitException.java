package de.blinklan.tools.ootl.exception;


/**
 * Exception used to gracefully exit the tool when a fatal error occurs
 * 
 * @author dvo
 *
 */
public final class ForcedExitException extends RuntimeException {

    private static final long serialVersionUID = 7762251629802035837L;

    public ForcedExitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForcedExitException(String message) {
        this(message, null);
    }

    public ForcedExitException(Throwable cause) {
        this(cause.getMessage(), cause);
    }
}
