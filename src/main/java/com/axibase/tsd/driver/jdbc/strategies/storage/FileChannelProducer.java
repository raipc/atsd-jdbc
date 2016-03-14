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

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.axibase.tsd.driver.jdbc.intf.IProducer;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.StrategyStatus;

public class FileChannelProducer implements IProducer {
	private static final LoggingFacade logger = LoggingFacade.getLogger(FileChannelProducer.class);

	private AsynchronousFileChannel writeChannel;
	private Future<Long> taskResult;
	private ExecutorService execIn;
	private StrategyStatus status;

	public FileChannelProducer(final StrategyStatus status) {
		this.status = status;
	}

	@Override
	public void close() throws IOException {
		if (taskResult != null && (!taskResult.isDone() || !taskResult.isCancelled()))
			taskResult.cancel(true);
		if (execIn != null && !execIn.isShutdown())
			execIn.shutdownNow();
		if (writeChannel != null)
			writeChannel.close();
		if (logger.isDebugEnabled()) {
			logger.debug("[closed]");
		}
	}

	@Override
	public void produce(Path tmp, ReadableByteChannel inputChannel) throws IOException {
		writeChannel = AsynchronousFileChannel.open(tmp, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		if (logger.isDebugEnabled()) {
			logger.debug("[produce] {} -> {}", tmp, writeChannel.isOpen());
		}
		FileChannelWriter writer = new FileChannelWriter(inputChannel, writeChannel, status);
		execIn = Executors.newSingleThreadExecutor();
		taskResult = execIn.submit(writer);
	}

}
