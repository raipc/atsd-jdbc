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
package com.axibase.tsd.driver.jdbc.strategies.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IConsumer;
import com.axibase.tsd.driver.jdbc.intf.IProducer;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.StrategyStatus;

public class FileStoreStrategy implements IStoreStrategy {
	private static final LoggingFacade logger = LoggingFacade.getLogger(FileStoreStrategy.class);
	private static final String TMP_FILE_PREFIX = "atsd-driver";

	private final IProducer producer;
	private final IConsumer consumer;
	private final StrategyStatus status;
	private Path tmp;
	private long position;

	public FileStoreStrategy(StatementContext context) {
		status = new StrategyStatus();
		status.setInProgress(true);
		consumer = new FileChannelConsumer(context, status);
		producer = new FileChannelProducer(status);
	}

	@Override
	public void close() throws Exception {
		if (logger.isTraceEnabled())
			logger.trace("[close] " + status.getSyncLatch().getCount());
		status.setInProgress(false);
		if (consumer != null)
			consumer.close();
		if (producer != null)
			producer.close();
		if (tmp != null) {
			final File file = tmp.toFile();
			if (file.exists()) {
				boolean deleted = file.delete();
				if (logger.isDebugEnabled())
					logger.debug("[close] File {} is deleted {} ", file.toString(), deleted);
			}
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
		AsynchronousFileChannel readChannel = AsynchronousFileChannel.open(tmp, StandardOpenOption.READ);
		if (logger.isDebugEnabled())
			logger.debug("[openToRead] {} -> {}", tmp.toAbsolutePath(), readChannel.isOpen());
		return consumer.open(readChannel);
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
		final ReadableByteChannel inputChannel = Channels.newChannel(is);
		tmp = Files.createTempFile(TMP_FILE_PREFIX, null);
		if (logger.isDebugEnabled())
			logger.debug("[store] " + tmp.toRealPath());
		producer.produce(tmp, inputChannel);
	}

	@Override
	public StatementContext getContext() {
		return consumer != null ? consumer.getContext() : null;
	}

}
