package com.axibase.tsd.driver.jdbc.ext;

public class AtsdRuntimeException extends RuntimeException {
	public AtsdRuntimeException(String message) {
		super(message);
	}

	public AtsdRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
