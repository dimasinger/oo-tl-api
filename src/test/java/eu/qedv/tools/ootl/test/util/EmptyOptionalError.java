package eu.qedv.tools.ootl.test.util;

public class EmptyOptionalError extends AssertionError {
	
	private static final long serialVersionUID = 7223125402120770719L;

	public EmptyOptionalError() {
		super("Unexpected empty Optional");
	}
}
