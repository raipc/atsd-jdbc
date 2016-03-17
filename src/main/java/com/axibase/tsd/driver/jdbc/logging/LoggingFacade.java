/*
* Copyright 2016 Axibase Corporation or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License").
* You may not use this file except in compliance with the License.
* A copy of the License is located at
*
* https://www.axibase.com/atsd/axibase-apache-2.0.pdf
*
* or in the "license" file accompanying this file. This file is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/
package com.axibase.tsd.driver.jdbc.logging;

public abstract class LoggingFacade {

	public static LoggingFacade getLogger(Class<?> clazz) {
		try {
			return (LoggingFacade) Class.forName("com.axibase.tsd.driver.jdbc.logging.LoggingSlf4jImpl")
					.getDeclaredConstructor(Class.class).newInstance(clazz);
		} catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ignored) {
		}
		return new LoggingFacadeStub();
	}

	public abstract String getName();

	public abstract boolean isTraceEnabled();

	public abstract void trace(String msg);

	public abstract void trace(String format, Object arg);

	public abstract void trace(String format, Object arg1, Object arg2);

	public abstract void trace(String format, Object... arguments);

	public abstract void trace(String msg, Throwable t);

	public abstract boolean isDebugEnabled();

	public abstract void debug(String msg);

	public abstract void debug(String format, Object arg);

	public abstract void debug(String format, Object arg1, Object arg2);

	public abstract void debug(String format, Object... arguments);

	public abstract void debug(String msg, Throwable t);

	public abstract boolean isInfoEnabled();

	public abstract void info(String msg);

	public abstract void info(String format, Object arg);

	public abstract void info(String format, Object arg1, Object arg2);

	public abstract void info(String format, Object... arguments);

	public abstract void info(String msg, Throwable t);

	public abstract boolean isWarnEnabled();

	public abstract void warn(String msg);

	public abstract void warn(String format, Object arg);

	public abstract void warn(String format, Object... arguments);

	public abstract void warn(String format, Object arg1, Object arg2);

	public abstract void warn(String msg, Throwable t);

	public abstract boolean isErrorEnabled();

	public abstract void error(String msg);

	public abstract void error(String format, Object arg);

	public abstract void error(String format, Object arg1, Object arg2);

	public abstract void error(String format, Object... arguments);

	public abstract void error(String msg, Throwable t);

}