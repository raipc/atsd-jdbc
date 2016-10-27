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
package com.axibase.tsd.driver.jdbc.content;

import com.axibase.tsd.driver.jdbc.intf.ParserRowContext;
import com.univocity.parsers.common.ParsingContext;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

public class UnivocityParserRowContext implements ParserRowContext {
	private final ParsingContext context;
	private String cachedLine;
	private long cachedLineNumber = -1L;
	private int[] splitIndexes;
	private long cachedSplitLineNumber = -1L;

	public UnivocityParserRowContext(ParsingContext context, int size) {
		this.context = context;
		this.splitIndexes = new int[size];
		if (size > 0) {
			splitIndexes[0] = -1;
		}
	}

	@Override
	public long getLine() {
		return context.currentLine() - 1L;
	}

	@Override
	public String getRowSource() {
		if (context.currentLine() != cachedLineNumber) {
			cachedLineNumber = context.currentLine();
			cachedLine = context.currentParsedContent();
		}
		return cachedLine;
	}

	@Override
	public String getColumnSource(int column) {
		if (column < 0) {
			throw new IllegalArgumentException("Column index should be positive");
		}
		final int length = splitIndexes.length;
		if (column >= length) {
			throw new IllegalArgumentException("Provided column index is greater than number of columns");
		}
		final String rawLine = getRowSource();
		if (cachedSplitLineNumber != getLine()) {
			split(rawLine);
		}
		return getSubstring(rawLine, column, length);
	}

	@Override
	public boolean hasQuote(int column) {
		final int length = this.splitIndexes.length;
		final char border;
		if (column <= length / 2) {
			border = getLastChar(getColumnSource(column));
		} else if (getLine() == cachedSplitLineNumber) {
			border = getLastChar(getSubstring(getRowSource(), column, length));
		} else {
			border = lastCharForColumn(column, this.splitIndexes, getRowSource());
		}
		return border == '"';
	}

	private String getSubstring(String rawLine, int column, int arrayLength) {
		if (column == arrayLength - 1) {
			final int lastCommaIndex = this.splitIndexes[column];
			return lastCommaIndex == rawLine.length() - 1 ? "" :
					rawLine.substring(lastCommaIndex + 1, findLineFeedIndex(rawLine));
		}
		return rawLine.substring(this.splitIndexes[column] + 1, this.splitIndexes[column+1]);
	}

	private static int findLineFeedIndex(String line) {
		for (int i = line.length() - 1; i >= 0; --i) {
			if (!CharUtils.isAsciiControl(line.charAt(i))) {
				return i + 1;
			}
		}
		throw new IllegalArgumentException("line <<" + line + ">> is empty or contains only control characters");
	}

	private void split(String line) {
		cachedSplitLineNumber = getLine();
		boolean pairedQuote = true;
		final int splitSize = this.splitIndexes.length;
		final int lineLength = line.length();
		int column = 1;
		char currentChar;
		for (int i = 0; i != lineLength; ++i) {
			currentChar = line.charAt(i);
			if (currentChar == '"') {
				pairedQuote = !pairedQuote;
			} else if (currentChar == ',' && pairedQuote) {
				splitIndexes[column] = i;
				++column;
				if (column == splitSize) {
					break;
				}
			}
		}
	}

	private char getLastChar(String string) {
		return StringUtils.isEmpty(string) ? '\0' : string.charAt(string.length() - 1);
	}

	static char lastCharForColumn(int column, int[] splitIndexes, String line) {
		boolean pairedQuote = true;
		int currentColumn = splitIndexes.length - 1;
		final int lastLineIndex = line.length() - 1;
		char currentChar;

		for (int i = lastLineIndex; i >= 0; --i) {
			currentChar = line.charAt(i);
			if (currentChar == '"') {
				pairedQuote = !pairedQuote;
			} else if (currentChar == ',' && pairedQuote) {
				splitIndexes[currentColumn] = i;
				--currentColumn;
				continue;
			}
			if (currentColumn == column && pairedQuote && !CharUtils.isAsciiControl(currentChar)) {
				return currentChar;
			}
		}
		return '\0';
	}

}
