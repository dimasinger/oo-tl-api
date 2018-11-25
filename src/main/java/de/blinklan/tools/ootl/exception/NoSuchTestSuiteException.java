package de.blinklan.tools.ootl.exception;

public class NoSuchTestSuiteException extends RuntimeException {

	private static final long serialVersionUID = -4428041050068925535L;

	public NoSuchTestSuiteException() {}

	public NoSuchTestSuiteException(String message) {
		super(message);
	}
}
