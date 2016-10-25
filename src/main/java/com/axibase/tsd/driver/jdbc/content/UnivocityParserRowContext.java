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

public class UnivocityParserRowContext implements ParserRowContext {
	private final ParsingContext context;
	private String cachedLine;
	private long cachedLineNumber;
	private int[] splitIndexes;
	private long cachedSplitLineNumber;

	public UnivocityParserRowContext(ParsingContext context, int size) {
		this.context = context;
		this.splitIndexes = new int[size];
		if (size > 0) {
			splitIndexes[0] = -1;
		}
	}

	@Override
	public long getLine() {
		return context.currentLine();
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
		if (cachedSplitLineNumber != this.context.currentLine()) {
			cachedSplitLineNumber = this.context.currentLine();
			split(rawLine);
		}
		if (column == length - 1) {
			final int lastCommaIndex = this.splitIndexes[column];
			return lastCommaIndex == rawLine.length() - 1 ? "" : rawLine.substring(lastCommaIndex);
		}
		return rawLine.substring(this.splitIndexes[column] + 1, this.splitIndexes[column+1]);
	}

	private void split(String line) {
		boolean pairedQuote = true;
		final int splitSize = this.splitIndexes.length;
		final int lineLength = line.length();
		int column = 1;
		for (int i = 0; i != lineLength; ++i) {
			char c = line.charAt(i);
			if (c == '"') {
				pairedQuote = !pairedQuote;
			} else if (c == ',' && pairedQuote) {
				splitIndexes[column] = i;
				++column;
				if (column == splitSize) {
					break;
				}
			}
		}

	}

}
