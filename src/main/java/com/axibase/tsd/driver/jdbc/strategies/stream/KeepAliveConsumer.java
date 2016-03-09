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
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IConsumer;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.StrategyStatus;

public class KeepAliveConsumer implements IConsumer {
	private static final LoggingFacade logger = LoggingFacade.getLogger(KeepAliveConsumer.class);

	private StrategyStatus status;
	private StatementContext context;
	private KeepAliveIterator<String[]> iterator;

	public KeepAliveConsumer(final StatementContext context, final StrategyStatus status) {
		this.context = context;
		this.status = status;
	}

	public Iterator<String[]> getIterator() throws AtsdException {
		if (iterator == null) {
			throw new AtsdException("Stream has not opened yet");
		}
		return iterator;
	}

	@Override
	public String[] open(final Channel channel) throws IOException {
		final ReadableByteChannel readChannel = (ReadableByteChannel) channel;
		iterator = new KeepAliveIterator<String[]>(readChannel, context, status);
		return iterator.next();
	}

	@Override
	public void close() throws IOException {
		if (logger.isTraceEnabled())
			logger.trace("[close]");
		if (iterator != null)
			iterator.close();
	}

	@Override
	public StatementContext getContext() {
		return context;
	}

	public class IterableConsumer implements Iterable<String[]> {
		private Iterator<String[]> iterator;

		public IterableConsumer(Iterator<String[]> iterator) {
			this.iterator = iterator;
		}

		@Override
		public Iterator<String[]> iterator() {
			return iterator;
		}
	}

}
