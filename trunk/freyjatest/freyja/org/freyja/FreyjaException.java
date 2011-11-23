package org.freyja;

public class FreyjaException extends RuntimeException {

	public FreyjaException() {
		super();
	}

	public FreyjaException(String message) {
		super(message);
	}

	public FreyjaException(String message, Throwable cause) {
		super(message, cause);
	}
}
