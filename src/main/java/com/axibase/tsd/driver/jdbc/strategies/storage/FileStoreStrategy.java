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

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.intf.IProducer;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.AbstractStrategy;

public class FileStoreStrategy extends AbstractStrategy {
	private static final LoggingFacade logger = LoggingFacade.getLogger(FileStoreStrategy.class);
	private static final String TMP_FILE_PREFIX = "atsd-driver";

	private final IProducer producer;
	private Path tmp;

	public FileStoreStrategy(StatementContext context) {
		super();
		consumer = new FileChannelConsumer(context, status);
		producer = new FileChannelProducer(status);
	}

	@Override
	public void close() throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("[close] " + status.getSyncLatch().getCount());
		}
		status.setInProgress(false);
		if (consumer != null) {
			consumer.close();
		}
		if (producer != null) {
			producer.close();
		}
		if (tmp != null) {
			final File file = tmp.toFile();
			if (file.exists()) {
				boolean deleted = file.delete();
				if (logger.isDebugEnabled()) {
					logger.debug("[close] File {} is deleted {} ", file.toString(), deleted);
				}
			}
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
		AsynchronousFileChannel readChannel = AsynchronousFileChannel.open(tmp, StandardOpenOption.READ);
		if (logger.isDebugEnabled()) {
			logger.debug("[openToRead] {} -> {}", tmp.toAbsolutePath(), readChannel.isOpen());
		}
		return consumer.open(readChannel);
	}

	@Override
	public void store(InputStream is) throws IOException {
		final ReadableByteChannel inputChannel = Channels.newChannel(is);
		tmp = Files.createTempFile(TMP_FILE_PREFIX, null);
		if (logger.isDebugEnabled()) {
			logger.debug("[store] " + tmp.toRealPath());
		}
		producer.produce(tmp, inputChannel);
	}

}
