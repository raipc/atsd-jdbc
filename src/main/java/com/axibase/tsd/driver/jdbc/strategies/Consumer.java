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
package com.axibase.tsd.driver.jdbc.strategies;


import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.content.json.*;
import com.axibase.tsd.driver.jdbc.ext.AtsdRuntimeException;
import com.axibase.tsd.driver.jdbc.intf.IConsumer;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Consumer implements IConsumer {
	private static final LoggingFacade logger = LoggingFacade.getLogger(Consumer.class);
	public static final char COMMENT_NEW_LINE = '#';

	protected final StrategyStatus status;
	protected final StatementContext context;
	protected final String source;
	protected RowIterator iterator;

	public Consumer(final StatementContext context, final StrategyStatus status, String source) {
		this.context = context;
		this.status = status;
		this.source = source;
	}

	@Override
	public StatementContext getContext() {
		return context;
	}

	@Override
	public void close() throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("[close]");
		}
		if (iterator != null) {
			iterator.close();
		}
	}

	@Override
	public String[] open(InputStream inputStream) throws IOException {
		iterator = new RowIterator(inputStream);
		return iterator.next();
	}

	@Override
	public Iterator<String[]> getIterator(){
		if (iterator == null) {
			assert source != null;
			throw new IllegalStateException(source + " has not opened yet");
		}
		return iterator;
	}

	@Override
	public void fillComments() {
		processComments(context, iterator.getCommentSection());
	}

	private static void processComments(StatementContext context, CharSequence comments) {
		if (comments.length() == 0) {
			return;
		}
		final String json = comments.toString();
		if (logger.isTraceEnabled()) {
			logger.trace(json);
		}
		final ObjectMapper mapper = new ObjectMapper();
		final Comments commentsObject;
		try {
			commentsObject = mapper.readValue(json, Comments.class);
			fillWarnings(commentsObject.getWarnings(), context);
			fillErrors(commentsObject.getErrors(), context);
		} catch (IOException e){
			if (logger.isDebugEnabled()) {
				logger.debug("Wrong error format", e);
			}
		}
	}

	private static void fillWarnings(List<WarningSection> warningSections, StatementContext context) {
		if (warningSections != null) {
			for (AtsdExceptionRepresentation section : warningSections) {
				SQLWarning sqlw = new SQLWarning(section.getMessage(), section.getState());
				List<StackTraceElement> list = getStackTrace(section);
				sqlw.setStackTrace(list.toArray(new StackTraceElement[list.size()]));
				context.addWarning(sqlw);
			}
		}
	}

	private static void fillErrors(List<ErrorSection> errorSections, StatementContext context) {
		SQLException sqlException = null;
		if (errorSections != null) {
			for (ErrorSection section : errorSections) {
				sqlException = new SQLException(section.getMessage(), section.getState());
				List<StackTraceElement> list = getStackTrace(section);
				sqlException.setStackTrace(list.toArray(new StackTraceElement[list.size()]));
				context.addException(sqlException);
			}
		}
		if (sqlException != null) {
			throw new AtsdRuntimeException(sqlException.getMessage(), sqlException);
		}
	}

	private static List<StackTraceElement> getStackTrace(AtsdExceptionRepresentation section) {
		final List<ExceptionSection> exceptions = section.getException();
		final List<StackTraceElement> list = new ArrayList<>(exceptions.size());
		for (ExceptionSection exc : exceptions) {
			list.add(new StackTraceElement(exc.getClassName(), exc.getMethodName(), exc.getFileName(),
					exc.getLineNumber()));
		}
		return list;
	}


}
