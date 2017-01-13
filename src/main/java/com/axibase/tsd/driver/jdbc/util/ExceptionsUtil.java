package com.axibase.tsd.driver.jdbc.util;

import java.sql.SQLException;

import com.axibase.tsd.driver.jdbc.ext.AtsdRuntimeException;

public class ExceptionsUtil {
	private ExceptionsUtil() {}

	public static SQLException unboxException(SQLException exception) {
		final Throwable cause = exception.getCause();
		if (cause == null || !(cause instanceof RuntimeException)) {
			return exception;
		}
		Throwable finalCause = exception;
		if (cause instanceof AtsdRuntimeException) {
			Throwable inner = cause.getCause();
			if (inner instanceof SQLException) { // parsed result from ATSD
				finalCause = cause;
			}
		}
		SQLException result = new SQLException(exception.getMessage(), finalCause);
		return result;
	}
}
