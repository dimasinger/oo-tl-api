package de.blinklan.tools.ootl.exception;

/**
 * Thrown on general testlink errors, usually used to wrap a
 * {@code TestLinkAPIException}.<br>
 * 
 * @author dimasinger
 *
 */
public class TestLinkException extends RuntimeException {

	private static final long serialVersionUID = -4411069280205607762L;

	public TestLinkException(String message) {
		super(message);
	}

	public TestLinkException(String message, Throwable cause) {
		super(message, cause);
	}
}
