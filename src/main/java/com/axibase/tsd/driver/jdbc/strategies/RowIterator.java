package com.axibase.tsd.driver.jdbc.strategies;

import com.axibase.tsd.driver.jdbc.ext.AtsdRuntimeException;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.axibase.tsd.driver.jdbc.strategies.Consumer.COMMENT_NEW_LINE;

public class RowIterator implements Iterator<String[]>, AutoCloseable {
	private final Iterator<String[]> decorated;
	private final InputStream decoratedStream;
	private String[] nextRow;
	private boolean inCommentSection;

	public RowIterator(InputStream inputStream) {
		this.decoratedStream = inputStream;
		try {
			this.decorated = org.sfm.csv.CsvParser.iterator(new InputStreamReader(inputStream));
			this.nextRow = decorated.next();
			if (nextRow != null && nextRow.length > 0 && nextRow[0].charAt(0) == COMMENT_NEW_LINE) {
				inCommentSection = true;
			}
		} catch (IOException e) {
			throw new AtsdRuntimeException(e);
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
			if (data.length > 0 && data[0].charAt(0) == COMMENT_NEW_LINE) {
				inCommentSection = true;
			}
		} catch (NoSuchElementException e) {
			data = null;
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
		if (this.decoratedStream != null) {
			this.decoratedStream.close();
		}
	}

	public CharSequence getCommentSection() {
		final StringBuilder buffer = new StringBuilder();
		if (nextRow != null && nextRow[0].charAt(0) == COMMENT_NEW_LINE) {
			addArrayToStringBuilder(nextRow, buffer);
			buffer.append('\n');
			while (decorated.hasNext()) {
				addArrayToStringBuilder(decorated.next(), buffer);
			}
		}
		return buffer;

	}

	private static void addArrayToStringBuilder(String[] array, StringBuilder buffer) {
		final int length = array.length;
		if (length > 0) {
			String firstToken = array[0];
			buffer.append(firstToken.toCharArray(), 1, firstToken.length() - 1);
			for (int i = 1;i < length; ++i) {
				buffer.append(',').append(array[i]);
			}
		}
	}
}
