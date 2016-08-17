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
import java.util.concurrent.CountDownLatch;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.AbstractStrategy;

public class KeepAliveStrategy extends AbstractStrategy implements IStoreStrategy {
	private static final LoggingFacade logger = LoggingFacade.getLogger(KeepAliveStrategy.class);

	private InputStream is;

	public KeepAliveStrategy(StatementContext context) {
		super();
		consumer = new KeepAliveConsumer(context, status);
	}

	@Override
	public void close() throws Exception {
		status.setInProgress(false);
		if (consumer != null) {
			consumer.close();
		}
		if (is != null) {
			is.close();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("[close] processed " + status.getProcessed());
		}
	}

	@Override
	public String[] openToRead() throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("[openToRead] " + status.getSyncLatch().getCount());
		}
		try {
			status.getSyncLatch().await();
		} catch (InterruptedException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("[openToRead] " + e.getMessage());
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("[openToRead] " + is.hashCode() + " -> " + is.available());
		}
		final ReadableByteChannel rbc = Channels.newChannel(is);
		return consumer.open(rbc);
	}

	@Override
	public void store(InputStream is) throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("[store] " + is.hashCode() + " -> " + is.available());
		}
		this.is = is;
		final CountDownLatch syncLatch = status.getSyncLatch();
		if (syncLatch.getCount() != 0) {
			syncLatch.countDown();
		}
	}

	@Override
	public StatementContext getContext() {
		return consumer != null ? consumer.getContext() : null;
	}

}
