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
package com.axibase.tsd.driver.jdbc.strategies.stream;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Iterator;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.IteratorData;
import com.axibase.tsd.driver.jdbc.strategies.StrategyStatus;

public class KeepAliveIterator<T> implements Iterator<String[]>, AutoCloseable {
	private static final LoggingFacade logger = LoggingFacade.getLogger(KeepAliveIterator.class);
	private final ReadableByteChannel readChannel;
	private final StrategyStatus status;
	private final IteratorData data;

	public KeepAliveIterator(final ReadableByteChannel readChannel, final StatementContext context,
			final StrategyStatus status) {
		this.readChannel = readChannel;
		this.status = status;
		this.data = new IteratorData(context);
	}

	@Override
	public boolean hasNext() {
		return status.isInProgress();
	}

	@Override
	public String[] next() {
		String[] found = data.getNext(false);
		if (found != null) {
			return found;
		}
		while (readNextBuffer() != -1) {
			data.bufferOperations();
			found = data.getNext(false);
			if (found != null) {
				return found;
			}
		}
		try {
			data.processComments();
		} catch (IOException e) {
			if (logger.isDebugEnabled())
				logger.debug("[processComments] " + e.getMessage());
		}
		status.setInProgress(true);
		found = data.getNext(true);
		if (logger.isTraceEnabled()) {
			logger.trace("[last] " + Arrays.toString(found));
			logger.trace("[sbuf] " + data.getSb().toString());
		}
		return found;
	}

	private int readNextBuffer() {
		int size = -1;
		try {
			size = readChannel.read(data.getBuffer());
			if (logger.isTraceEnabled())
				logger.trace("[readNextBuffer] " + size);
		} catch (IOException e) {
			if (logger.isDebugEnabled())
				logger.debug("[readNextBuffer] " + e.getMessage());
		}
		return size;
	}

	@Override
	public void close() throws IOException {
		if (readChannel != null)
			readChannel.close();
		if (logger.isTraceEnabled())
			logger.trace("[closed]");
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}