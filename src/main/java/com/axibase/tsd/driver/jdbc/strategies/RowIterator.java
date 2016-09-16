package com.axibase.tsd.driver.jdbc.strategies;

import com.axibase.tsd.driver.jdbc.ext.AtsdRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.sfm.csv.CsvParser;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.axibase.tsd.driver.jdbc.DriverConstants.ATSD_VERSION_COMPRESSED_ERRORS;

public class RowIterator implements Iterator<String[]>, AutoCloseable {
	private static final char COMMENT_SIGN = '#';
	private static final char JSON_OBJECT_START = '{';
	private static final char JSON_OBJECT_END = '}';
	private static final char JSON_LIST_START = '[';
	private static final char JSON_LIST_END = ']';
	private static final char KEY_VALUE_DELIMITER = ':';
	private static final String COMMENT_WARNINGS_START = "#{\"warnings\"";
	private static final String COMMENT_ERRORS_START = "#{\"errors\"";

	private Iterator<String[]> decorated;
	private final InputStream decoratedStream;
	private final int version;
	private final int columnCount;
	private String[] nextRow;
	private boolean[] columnIsTag;
	private boolean inCommentSection;
	private String commentSection;

	public RowIterator(InputStream inputStream, int version) {
		this.decoratedStream = inputStream;
		this.version = version;

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String firstLine = reader.readLine();
			this.decorated = CsvParser.iterator(reader);

			if (handleErrors(firstLine)) {
				this.columnCount = 0;
				return;
			}
			this.nextRow = CsvParser.reader(firstLine).iterator().next();
			this.columnCount = nextRow == null || inCommentSection ? 0 : nextRow.length;
			fillColumnIsTagArray();

		} catch (IOException e) {
			throw new AtsdRuntimeException(e);
		}
	}

	private void fillColumnIsTagArray() {
		if (this.nextRow != null) {
			final int length = this.nextRow.length;
			columnIsTag = new boolean[length];
			for (int i = 0; i < length; ++i) {
				columnIsTag[i] = this.nextRow[i].startsWith("tag");
			}
		}
	}

	@Override
	public boolean hasNext() {
		return !inCommentSection && nextRow != null;
	}

	@Override
	public String[] next() {
		if (inCommentSection) {
			return null;
		}
		String[] old = nextRow;
		String[] data;
		try {
			data = decorated.next();
			if (isCommentedJsonInBody(data)) {
				inCommentSection = true;
				fillCommentSection(data);
				data = null;
			} else {
				setEmptyTagsNull(data);
			}
		} catch (NoSuchElementException e) {
			data = null;
		}

		nextRow = data;
		return old;
	}

	private boolean handleErrors(String firstLine) {
		if (StringUtils.isNotBlank(firstLine) && firstLine.charAt(0) == COMMENT_SIGN) {
			if (version >= ATSD_VERSION_COMPRESSED_ERRORS
					&& (firstLine.startsWith(COMMENT_ERRORS_START)
					|| firstLine.startsWith(COMMENT_WARNINGS_START))) {
				commentSection = firstLine.substring(1);
			} else {
				final String[] errorsStart = {firstLine};
				fillCommentSection(errorsStart);
			}
			return true;
		}
		return false;
	}

	private boolean isCommentedJsonInBody(String[] data) {
		if (columnCount == data.length && data[0].charAt(0) != COMMENT_SIGN) {
			return false;
		}
		if (version > ATSD_VERSION_COMPRESSED_ERRORS) {
			return !decorated.hasNext() && data.length > 0
					&& (data[0].startsWith(COMMENT_WARNINGS_START)
					||  data[0].startsWith(COMMENT_ERRORS_START));
		}
		return data[0].charAt(1) == JSON_OBJECT_START;
	}

	private void setEmptyTagsNull(String[] data) {
		final int length = data.length;
		for (int i = 0; i < length; ++i) {
			if (columnIsTag[i] && "".equals(data[i])) {
				data[i] = null;
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");
	}

	@Override
	public void close() throws IOException {
		if (this.decoratedStream != null) {
			this.decoratedStream.close();
		}
	}

	public CharSequence getCommentSection() {
		return commentSection;
	}

	private void fillCommentSection(String[] lastParsedRow) {
		if (commentSection == null) {
			final StringBuilder buffer = new StringBuilder(2048);
			if (lastParsedRow != null && lastParsedRow[0].charAt(0) == COMMENT_SIGN) {
				addArrayToStringBuilder(lastParsedRow, buffer);
				while (decorated.hasNext()) {
					addArrayToStringBuilder(decorated.next(), buffer);
				}
			}
			commentSection = buffer.toString();
		}
	}

	private static void addArrayToStringBuilder(String[] array, StringBuilder buffer) {
		final int length = array.length;
		if (length == 0) {
			return;
		}
		String token = array[0];
		int lastIndex = token.length() - 1;
		buffer.append(token.toCharArray(), 1, lastIndex);
		for (int i = 1;i < length; ++i) {
			buffer.append(',');
			token = array[i];
			if (StringUtils.isEmpty(token)) {
				continue;
			}
			lastIndex = token.length() - 1;
			if (isJsonStartSymbol(token.charAt(0))) {
				buffer.append(token);
			} else {
				int indexAfterColon = token.indexOf(KEY_VALUE_DELIMITER) + 1;
				int endIndex = findEndStringIndex(token, lastIndex);
				buffer.append(token.substring(0, indexAfterColon));

				addToBuffer(buffer, token, indexAfterColon, endIndex);

				if (endIndex != lastIndex) {
					buffer.append(token.substring(endIndex + 1));
				}
			}
		}
	}

	private static void addToBuffer(StringBuilder buffer, CharSequence sequence, int startPos, int endPos) {
		final int beginIndex = findStartStringIndex(sequence, startPos);
		final int endIndex = findEndStringIndex(sequence, endPos);
		if (startPos != beginIndex) {
			buffer.append(sequence.subSequence(startPos, beginIndex));
		}
		final boolean shouldBeQuoted = Character.isAlphabetic(sequence.charAt(beginIndex));
		if (shouldBeQuoted) {
			buffer.append('"');
		}
		buffer.append(sequence.subSequence(beginIndex, endIndex + 1));
		if (shouldBeQuoted) {
			buffer.append('"');
		}
		if (endPos != endIndex) {
			buffer.append(sequence.subSequence(endIndex + 1, endPos));
		}
	}

	private static boolean isJsonStartSymbol(char symbol) {
		return symbol == JSON_OBJECT_START || symbol == JSON_LIST_START;
	}

	private static int findStartStringIndex(CharSequence sequence, int startPosition) {
		if (isJsonStartSymbol(sequence.charAt(startPosition))) {
			return StringUtils.indexOf(sequence, KEY_VALUE_DELIMITER, startPosition) + 1;
		}
		return startPosition;
	}

	private static int findEndStringIndex(CharSequence sequence, int endIndex) {
		int last = endIndex;
		char current;
		while (last >= 0 && ((current = sequence.charAt(last)) == JSON_OBJECT_END || current == JSON_LIST_END)) {
			--last;
		}
		return last;
	}
}
