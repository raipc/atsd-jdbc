package com.axibase.tsd.driver.jdbc.strategies;

import com.axibase.tsd.driver.jdbc.ext.AtsdRuntimeException;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class RowIterator implements Iterator<String[]>, AutoCloseable {
	private static final char COMMENT_SIGN = '#';
	private static final CsvParserSettings DEFAULT_PARSER_SETTINGS;
	private static final CsvParserSettings MANY_ROWS_PARSER_SETTINGS;
	static {
		DEFAULT_PARSER_SETTINGS = new CsvParserSettings();
		DEFAULT_PARSER_SETTINGS.setInputBufferSize(16 * 1024);
		DEFAULT_PARSER_SETTINGS.setReadInputOnSeparateThread(false);
		DEFAULT_PARSER_SETTINGS.setCommentCollectionEnabled(false);

		MANY_ROWS_PARSER_SETTINGS = new CsvParserSettings();
		MANY_ROWS_PARSER_SETTINGS.setReadInputOnSeparateThread(true);
		MANY_ROWS_PARSER_SETTINGS.setCommentCollectionEnabled(false);
	}

	private CsvParser decoratedParser;
	private final Reader decoratedReader;
	private String[] nextRow;
	private boolean[] columnIsTag;
	private String commentSection;

	private RowIterator(Reader reader, CsvParserSettings settings) {
		this.decoratedReader = reader;
		try {
			final int firstSymbol = reader.read();
			if (firstSymbol == COMMENT_SIGN) {
				fillCommentSectionWithReaderContent(reader);
			} else if (firstSymbol != -1) {
				CsvParser parser = new CsvParser(settings);
				this.decoratedParser = parser;
				parser.beginParsing(reader);
				this.nextRow = parser.parseNext();
				if (nextRow != null) {
					nextRow[0] = (char) firstSymbol + nextRow[0];
					fillColumnIsTagArray();
				}
			}
		} catch (IOException e) {
			throw new AtsdRuntimeException(e);
		}
	}

	public static RowIterator newDefaultIterator(InputStream inputStream) {
		Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		return newDefaultIterator(reader);
	}

	public static RowIterator newDefaultIterator(Reader reader) {
		return new RowIterator(reader, DEFAULT_PARSER_SETTINGS);
	}

	public static RowIterator newManyRowsIterator(InputStream inputStream) {
		Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		return newManyRowsIterator(reader);
	}

	public static RowIterator newManyRowsIterator(Reader reader) {
		return new RowIterator(reader, MANY_ROWS_PARSER_SETTINGS);
	}

	private void fillColumnIsTagArray() {
		final int length = this.nextRow.length;
		columnIsTag = new boolean[length];
		for (int i = 0; i < length; ++i) {
			columnIsTag[i] = this.nextRow[i].startsWith("tag");
		}
	}

	private void setEmptyTagsNull(String[] data) {
		final int length = data.length;
		for (int i = 0; i < length; ++i) {
			if (columnIsTag[i] && "".equals(data[i])) {
				data[i] = null;
			}
		}
	}

	private void fillCommentSectionWithReaderContent(Reader reader) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		StringBuilder buffer = new StringBuilder();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			buffer.append(line.charAt(0) == COMMENT_SIGN ? line.substring(1) : line);
		}
		this.commentSection = buffer.toString();
	}

	private void fillCommentSectionWithParsedComments() {
		final String comments = decoratedParser.getContext().currentParsedContent();
		int startIndex = comments.indexOf(COMMENT_SIGN) + 1;
		if (startIndex > 0) {
			final int length = comments.length();
			StringBuilder buffer = new StringBuilder(length - startIndex);
			while (startIndex != -1) {
				final int endIndex = comments.indexOf("\n#", startIndex);
				final String substring;
				if (endIndex == -1) {
					substring = comments.substring(startIndex);
					startIndex = -1;
				} else {
					substring = comments.substring(startIndex, endIndex);
					startIndex = endIndex + 2;
				}
				buffer.append(substring);
			}
			commentSection = buffer.toString();
		}
	}

	@Override
	public boolean hasNext() {
		return nextRow != null;
	}

	@Override
	public String[] next() {
		if (nextRow == null) {
			return null;
		}
		String[] old = nextRow;
		String[] data;
		data = decoratedParser.parseNext();
		if (data == null) {
			fillCommentSectionWithParsedComments();
		} else {
			setEmptyTagsNull(data);
		}

		nextRow = data;
		return old;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");
	}

	@Override
	public void close() throws IOException {
		if (this.decoratedParser != null) {
			this.decoratedParser.stopParsing();
		}
		if (this.decoratedReader != null) {
			this.decoratedReader.close();
		}
	}

	public CharSequence getCommentSection() {
		return commentSection;
	}
}
