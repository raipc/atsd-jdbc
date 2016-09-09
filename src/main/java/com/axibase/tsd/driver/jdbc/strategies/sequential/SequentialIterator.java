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
package com.axibase.tsd.driver.jdbc.strategies.sequential;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.ext.AtsdRuntimeException;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.AbstractIterator;
import com.axibase.tsd.driver.jdbc.strategies.StrategyStatus;

public class SequentialIterator extends AbstractIterator {
	private static final LoggingFacade logger = LoggingFacade.getLogger(SequentialIterator.class);
	private static final int BUFFER_SIZE = 10 * 1024;

	private final BufferedReader reader;
	private String nextLine = null;

	public SequentialIterator(final ReadableByteChannel readChannel, final StatementContext context,
							  final StrategyStatus status) {
		super(context, status);
		reader = new BufferedReader(Channels.newReader(readChannel, "UTF-8"), BUFFER_SIZE);
		readLineFromStream();
	}

	@Override
	public boolean hasNext() {
		return nextLine != null;
	}

	@Override
	public String[] next() {
		String[] found = data.getNext(false);
		if (found != null) {
			return found;
		}

		boolean bufferOperationsSuccessful = false;
		while (!bufferOperationsSuccessful && nextLine != null) {
			byte[] line = nextLine.getBytes(Charset.forName("UTF-8"));
			readLineFromStream();
			final int limit = data.getBuffer().limit();
			final int length = line.length;
			int offset = 0;
			int bufferSize;

			while ((bufferSize = length - offset) > 0){
				int size = Math.min(bufferSize, limit);
				data.getBuffer().put(line, offset, size);
				try {
					bufferOperationsSuccessful = data.bufferOperations();
				} catch (final AtsdException  e) {
					if (logger.isDebugEnabled()) {
						logger.debug("[bufferOperations] " + e.getMessage());
					}
					status.setInProgress(false);
					return null;
				}
				found = data.getNext(false);
				if (found != null) {
					return found;
				}
				offset += size;
			}
		}

		try {
			data.processComments();
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("[processComments] " + e.getMessage());
			}
		}

		status.setInProgress(false);
		found = data.getNext(true);
		if (logger.isTraceEnabled()) {
			logger.trace("[last] " + Arrays.toString(found));
			logger.trace("[sbuf] " + data.getSb().toString());
		}
		return found;
	}

	private void readLineFromStream() {
		try {
			nextLine = reader.readLine();
		} catch (IOException e) {
			throw new AtsdRuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
		if (logger.isTraceEnabled()) {
			logger.trace("[closed]");
		}
	}

}
