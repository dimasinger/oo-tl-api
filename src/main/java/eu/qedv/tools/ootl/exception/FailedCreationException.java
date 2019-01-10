package eu.qedv.tools.ootl.exception;

/**
 * thrown when creation of a testlink object failed
 * 
 * @author dimasinger
 *
 */
public class FailedCreationException extends TestLinkException {

    private static final long serialVersionUID = -8644944304402437988L;

    public FailedCreationException(String message) {
        super(message);
    }

    public FailedCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}
