package de.blinklan.tools.ootl.util;

public class EmptyOptionalError extends AssertionError {
	
	private static final long serialVersionUID = 7223125402120770719L;

	public EmptyOptionalError() {
		super("Unexpected empty Optional");
	}
}
