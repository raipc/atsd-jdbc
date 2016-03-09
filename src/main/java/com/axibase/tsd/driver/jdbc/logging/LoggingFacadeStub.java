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

public class LoggingFacadeStub extends LoggingFacade {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public void trace(String msg) {
	}

	@Override
	public void trace(String format, Object arg) {
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
	}

	@Override
	public void trace(String format, Object... arguments) {
	}

	@Override
	public void trace(String msg, Throwable t) {
	}

	@Override
	public void debug(String msg) {
	}

	@Override
	public void debug(String format, Object arg) {
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
	}

	@Override
	public void debug(String format, Object... arguments) {
	}

	@Override
	public void debug(String msg, Throwable t) {
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}
	
	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public void info(String msg) {
	}

	@Override
	public void info(String format, Object arg) {
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
	}

	@Override
	public void info(String format, Object... arguments) {
	}

	@Override
	public void info(String msg, Throwable t) {
	}

	@Override
	public boolean isWarnEnabled() {
		return false;
	}

	@Override
	public void warn(String msg) {
	}

	@Override
	public void warn(String format, Object arg) {
	}

	@Override
	public void warn(String format, Object... arguments) {
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
	}

	@Override
	public void warn(String msg, Throwable t) {
	}

	@Override
	public boolean isErrorEnabled() {
		return false;
	}

	@Override
	public void error(String msg) {
	}

	@Override
	public void error(String format, Object arg) {
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
	}

	@Override
	public void error(String format, Object... arguments) {
	}

	@Override
	public void error(String msg, Throwable t) {
	}

}