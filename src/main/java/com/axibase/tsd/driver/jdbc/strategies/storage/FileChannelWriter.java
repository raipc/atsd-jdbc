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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.StrategyStatus;

public class FileChannelWriter implements Callable<Long> {
	private static final LoggingFacade logger = LoggingFacade.getLogger(FileChannelWriter.class);

	private static final int PART_LENGTH = 1 * 1024 * 1024;
	private final ReadableByteChannel inputChannel;
	private final AsynchronousFileChannel writeChannel;
	private final StrategyStatus status;

	public FileChannelWriter(final ReadableByteChannel inputChannel, final AsynchronousFileChannel writeChannel,
			final StrategyStatus status) {
		this.inputChannel = inputChannel;
		this.writeChannel = writeChannel;
		this.status = status;
	}

	@Override
	public Long call() throws IOException, AtsdException {
		if (logger.isTraceEnabled())
			logger.trace("[Writer->call] " + writeChannel.size());
		status.setLockSize(PART_LENGTH);
		long nextPart = PART_LENGTH;
		FileLock fileLock = getFileLock(0L);
		final ByteBuffer buffer = ByteBuffer.allocate(256 * 1024);
		long position = 0;
		long receivedBytes = 0;
		Future<Integer> lastWrite = null;
		try {
			while ((receivedBytes = inputChannel.read(buffer)) >= 0 || buffer.position() != 0) {
				buffer.flip();
				lastWrite = writeChannel.write(buffer, position);
				position += receivedBytes;
				if (position > nextPart) {
					releaseFileLock(fileLock);
					releaseLatch();
					fileLock = getFileLock(position);
					nextPart = position + PART_LENGTH;
				}
				if (lastWrite != null)
					try {
						lastWrite.get();
					} catch (InterruptedException | ExecutionException e) {
						logger.error(e.getMessage(), e);
						throw new AtsdException(e.getMessage());
					}
				buffer.compact();
			}
		} finally {
			status.setLockPosition(Long.MAX_VALUE);
			releaseFileLock(fileLock);
			releaseLatch();
			if (logger.isTraceEnabled())
				logger.trace("[Writer->call] File size: " + writeChannel.size());
			writeChannel.close();
			inputChannel.close();
			status.setInProgress(true);
		}
		return position;
	}

	private void releaseFileLock(FileLock fileLock) throws IOException {
		fileLock.release();
		fileLock.close();
	}

	private FileLock getFileLock(long position) throws AtsdException {
		if (logger.isTraceEnabled())
			logger.trace("Try write lock: " + position);
		status.setLockPosition(position);
		Future<FileLock> lock = writeChannel.lock(position, PART_LENGTH, false);
		FileLock fileLock = null;
		try {
			fileLock = lock.get();
			if (logger.isTraceEnabled())
				logger.trace("[Writer->call] Locked " + position);
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage(), e);
			throw new AtsdException(e.getMessage());
		}
		return fileLock;
	}

	private void releaseLatch() {
		final CountDownLatch syncLatch = status.getSyncLatch();
		if(syncLatch.getCount() != 0)
			syncLatch.countDown();
	}
}