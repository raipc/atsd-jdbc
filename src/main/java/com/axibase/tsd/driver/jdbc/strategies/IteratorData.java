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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.List;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.content.json.Comments;
import com.axibase.tsd.driver.jdbc.content.json.ErrorSection;
import com.axibase.tsd.driver.jdbc.content.json.ExceptionSection;
import com.axibase.tsd.driver.jdbc.content.json.WarningSection;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IteratorData {
	private static final LoggingFacade logger = LoggingFacade.getLogger(IteratorData.class);
	private static final String COMMENT_NEW_LINE = "#";
	private static final String COMMENT_NEXT_LINE = '\n' + COMMENT_NEW_LINE;
	private static final char CSV_ESCAPE_SYMBOL = '\\';
	private static final char CSV_QUOTE_SYMBOL = '"';
	private static final char CSV_SEPARATOR_SYMBOL = ',';
	private final ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
	private final StatementContext context;
	private final StringBuilder comments = new StringBuilder();
	private StringBuilder content = new StringBuilder();
	private int position = 0;

	public IteratorData(StatementContext context) {
		this.context = context;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public StringBuilder getSb() {
		return content;
	}

	public StringBuilder getComments() {
		return comments;
	}

	public int getPosition() {
		return position;
	}

	public String[] getNext(boolean stopping) {
		if (content.length() == 0)
			return null;
		int crlf = content.indexOf("\n");
		if (crlf == -1) {
			if (!stopping)
				return null;
			crlf = content.length();
		}
		final CharSequence subSequence = content.subSequence(0, crlf);
		final String line = subSequence.toString().trim();
		content = content.delete(0, crlf + 1);
		return splitLine(line);
	}

	public void bufferOperations() {
		buffer.flip();
		final byte[] tmp = new byte[buffer.limit()];
		buffer.get(tmp);
		buffer.clear();
		position += tmp.length;
		if (logger.isTraceEnabled())
			logger.trace("[position] " + position);
		final String line = new String(tmp, Charset.defaultCharset());
		if (line.startsWith(COMMENT_NEW_LINE) || comments.length() > 0) {
			comments.append(new String(line));
			return;
		}
		int commentStart = line.indexOf(COMMENT_NEXT_LINE);
		if (commentStart != -1) {
			content.append(new String(line.substring(0, commentStart)));
			comments.append(new String(line.substring(commentStart)));
		} else
			content.append(line);
	}

	public void processComments() throws JsonParseException, JsonMappingException, IOException {
		if (comments.length() == 0)
			return;
		final String json = comments.toString().replace(COMMENT_NEW_LINE, "");
		if (logger.isTraceEnabled())
			logger.trace(json);
		final ObjectMapper mapper = new ObjectMapper();
		final Comments commentsObject = mapper.readValue(json, Comments.class);
		final List<ErrorSection> errorSections = commentsObject.getErrors();
		if (errorSections != null && errorSections.size() != 0) {
			for (ErrorSection section : errorSections) {
				SQLException sqle = new SQLException(section.getMessage(), section.getState());
				final List<ExceptionSection> exceptions = section.getException();
				List<StackTraceElement> list = new ArrayList<>(exceptions.size());
				for (ExceptionSection exc : exceptions) {
					list.add(new StackTraceElement(exc.getClassName(), exc.getMethodName(), exc.getFileName(),
							exc.getLineNumber()));
				}
				sqle.setStackTrace(list.toArray(new StackTraceElement[0]));
				context.addException(sqle);
			}
		}
		final List<WarningSection> warningSections = commentsObject.getWarnings();
		if (warningSections != null && warningSections.size() != 0) {
			for (WarningSection section : warningSections) {
				SQLWarning sqlw = new SQLWarning(section.getMessage(), section.getState());
				final List<ExceptionSection> exceptions = section.getException();
				List<StackTraceElement> list = new ArrayList<>(exceptions.size());
				for (ExceptionSection exc : exceptions) {
					list.add(new StackTraceElement(exc.getClassName(), exc.getMethodName(), exc.getFileName(),
							exc.getLineNumber()));
				}
				sqlw.setStackTrace(list.toArray(new StackTraceElement[0]));
				context.addWarning(sqlw);
			}
		}
	}

	private String[] splitLine(String line) {
		final List<String> result = new ArrayList<>();
		final StringBuilder sb = new StringBuilder(line.length());
		boolean opened = false;
		boolean inside = false;
		for (int pos = 0; pos < line.length(); pos++) {
			char ch = line.charAt(pos);
			boolean nonterminal = line.length() > pos + 1;
			boolean expected = nonterminal && (opened || inside);
			char charNext = nonterminal ? line.charAt(pos + 1) : '\u0000';
			switch (ch) {
			case CSV_ESCAPE_SYMBOL:
				if (expected && isEscapeNext(charNext)) {
					sb.append(charNext);
					pos++;
				}
				break;
			case CSV_QUOTE_SYMBOL:
				if (expected && isQuoteNext(charNext)) {
					sb.append(charNext);
					pos++;
				} else {
					char charPrev = line.charAt(pos - 1);
					if (nonterminal && pos > 2 && noSeparatorAround(charPrev, charNext)) {
						sb.append(ch);
					}
					opened = !opened;
				}
				inside = !inside;
				break;
			case CSV_SEPARATOR_SYMBOL:
				if (!opened) {
					inside = false;
					result.add(sb.toString());
					sb.setLength(0);
					break;
				}
			default:
				inside = true;
				sb.append(ch);
			}
		}
		if (sb != null)
			result.add(sb.toString());
		return result.toArray(new String[result.size()]);
	}

	private static boolean isQuoteNext(char nextChar) {
		return nextChar == CSV_QUOTE_SYMBOL;
	}

	private static boolean isEscapeNext(char nextChar) {
		return nextChar == CSV_ESCAPE_SYMBOL || isQuoteNext(nextChar);
	}

	private static boolean noSeparatorAround(char prevChar, char nextChar) {
		return prevChar != CSV_SEPARATOR_SYMBOL && nextChar != CSV_SEPARATOR_SYMBOL;
	}

}