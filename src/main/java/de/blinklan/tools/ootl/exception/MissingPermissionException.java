package de.blinklan.tools.ootl.exception;

/**
 * Thrown when trying to perform an action without the necessary permission
 * configured in {@link TestLinkConfig}
 * 
 * @author dimasinger
 *
 */
public class MissingPermissionException extends RuntimeException {

	private static final long serialVersionUID = -1825231319536970144L;

	public MissingPermissionException() {}

	public MissingPermissionException(String message) {
		super(message);
	}
}
