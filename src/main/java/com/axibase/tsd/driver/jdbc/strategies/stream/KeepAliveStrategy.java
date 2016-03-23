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
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IConsumer;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.StrategyStatus;

public class KeepAliveStrategy implements IStoreStrategy {
	private static final LoggingFacade logger = LoggingFacade.getLogger(KeepAliveStrategy.class);

	private final IConsumer consumer;
	private final StrategyStatus status;
	private InputStream is;
	private long position;

	public KeepAliveStrategy(StatementContext context) {
		status = new StrategyStatus();
		status.setInProgress(true);
		consumer = new KeepAliveConsumer(context, status);
	}

	@Override
	public void close() throws Exception {
		status.setInProgress(false);
		if (consumer != null)
			consumer.close();
		if (is != null) {
			is.close();
		}
		if (logger.isDebugEnabled())
			logger.debug("[close] processed " + status.getProcessed());
	}

	@Override
	public String[] openToRead() throws IOException {
		if (logger.isTraceEnabled())
			logger.trace("[openToRead] " + status.getSyncLatch().getCount());
		try {
			status.getSyncLatch().await();
		} catch (InterruptedException e) {
			if (logger.isDebugEnabled())
				logger.debug("[openToRead] " + e.getMessage());
		}
		if (logger.isTraceEnabled())
			logger.trace("[openToRead] " + is.hashCode() + " -> " + is.available());
		final ReadableByteChannel rbc = Channels.newChannel(is);
		return consumer.open(rbc);
	}

	@Override
	public List<String[]> fetch(long from, int limit) throws AtsdException, IOException {
		final List<String[]> list = new ArrayList<>();
		final Iterator<String[]> iterator = consumer.getIterator();
		while (iterator.hasNext()) {
			final String[] next = iterator.next();
			if (next == null) {
				if (logger.isDebugEnabled())
					logger.debug("[fetch] no more");
				break;
			}
			if (position < from) {
				if (logger.isTraceEnabled())
					logger.trace("[fetch] position less from: " + position + "->" + from);
				position++;
				continue;
			}
			list.add(next);
			if (list.size() >= limit)
				break;
		}
		if (logger.isTraceEnabled())
			logger.trace("[fetch] sublist size: " + list.size());
		position = from + list.size();
		if (logger.isTraceEnabled())
			logger.trace("[fetch] updated position: " + position);
		status.increaseProcessed(list.size());
		return Collections.unmodifiableList(list);
	}

	@Override
	public void store(InputStream is) throws IOException {
		if (logger.isTraceEnabled())
			logger.trace("[store] " + is.hashCode() + " -> " + is.available());
		this.is = is;
		final CountDownLatch syncLatch = status.getSyncLatch();
		if(syncLatch.getCount() != 0)
			syncLatch.countDown();
	}

	@Override
	public StatementContext getContext() {
		return consumer != null ? consumer.getContext() : null;
	}

}
