package com.axibase.tsd.driver.jdbc.util;

import java.sql.SQLException;

public class ExceptionsUtil {
	private ExceptionsUtil() {}

	public static SQLException unboxException(SQLException exception) {
		final Throwable cause = exception.getCause();
		if (cause == null || !(cause instanceof RuntimeException)) {
			return exception;
		}
		SQLException unboxed = new SQLException(exception.getMessage());
		unboxed.setStackTrace(exception.getStackTrace());
		return unboxed;
	}
}
