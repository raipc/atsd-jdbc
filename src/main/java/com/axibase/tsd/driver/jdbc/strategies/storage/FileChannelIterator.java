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
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.IteratorData;
import com.axibase.tsd.driver.jdbc.strategies.StrategyStatus;

public class FileChannelIterator<T> implements Iterator<String[]>, AutoCloseable {
	private static final LoggingFacade logger = LoggingFacade.getLogger(FileChannelIterator.class);
	private static final int PART_LENGTH = 1 * 1024 * 1024;
	private final ReentrantLock lock = new ReentrantLock();
	private final AsynchronousFileChannel readChannel;
	private final StrategyStatus status;
	private final IteratorData data;

	public FileChannelIterator(final AsynchronousFileChannel readChannel, final StatementContext context,
			final StrategyStatus status) {
		this.readChannel = readChannel;
		this.status = status;
		data = new IteratorData(context);
	}

	@Override
	public boolean hasNext() {
		if (status.isInProgress() || data.getPosition() < status.getCurrentSize())
			return true;
		if (logger.isDebugEnabled())
			logger.debug("[hasNext->false] comments: " + data.getComments().length());
		return false;
	}

	@Override
	public String[] next() {
		String[] found = data.getNext(false);
		if (found != null) {
			return found;
		}
		while (true) {
			if (!status.isInProgress() && status.getCurrentSize() <= data.getPosition()) {
				if (logger.isDebugEnabled())
					logger.debug("[next] stop iterating with " + status.isInProgress() + ' ' + status.getCurrentSize()
							+ ' ' + data.getPosition());
				return null;
			}
			while (status.getLockPosition() <= data.getPosition()) {
				if (logger.isDebugEnabled())
					logger.debug("[next] waiting for the next section: " + data.getPosition());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!status.isInProgress()) {
					break;
				}
			}
			if (logger.isTraceEnabled())
				logger.trace("[next] try read lock: " + data.getPosition());
			Future<FileLock> fileLock;
			try {
				fileLock = readChannel.lock(data.getPosition(), PART_LENGTH, true);
				while (!fileLock.isDone())
					;
			} catch (OverlappingFileLockException e) {
				if (logger.isTraceEnabled())
					logger.trace("[next] overlapped");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
			if (logger.isTraceEnabled())
				logger.trace("[next] locked on read: " + data.getPosition());
			Future<Integer> operation = readChannel.read(data.getBuffer(), data.getPosition());
			while (!operation.isDone())
				;
			lock.lock();
			try {
				if (operation.get() == -1) {
					data.processComments();
					return data.getNext(true);
				}
			} catch (ExecutionException | InterruptedException | IOException e) {
				if (logger.isDebugEnabled())
					logger.debug("[next] ExecutionInterruptedException: " + e.getMessage());
				return data.getNext(true);
			} finally {
				releaseFileLock(fileLock);
				lock.unlock();
			}
			data.bufferOperations();
			found = data.getNext(false);
			if (found != null)
				return found;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		if (readChannel != null) {
			lock.lock();
			try {
				readChannel.close();
				if (logger.isTraceEnabled())
					logger.trace("[close]");
			} finally {
				lock.unlock();
			}
		}
	}

	private void releaseFileLock(final Future<FileLock> fileLock) {
		if (fileLock == null)
			return;
		try {
			final FileLock lock = fileLock.get();
			lock.release();
			lock.close();
		} catch (final IOException | InterruptedException | ExecutionException e) {
			if (logger.isDebugEnabled())
				logger.debug("[releaseFileLock] " + e.getMessage());
		} finally {
			fileLock.cancel(true);
		}
	}

}