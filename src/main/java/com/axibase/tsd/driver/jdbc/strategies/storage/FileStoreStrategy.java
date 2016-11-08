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

import java.io.*;
import java.util.concurrent.CountDownLatch;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.enums.Strategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.AbstractStrategy;

public class FileStoreStrategy extends AbstractStrategy {
	private static final LoggingFacade logger = LoggingFacade.getLogger(FileStoreStrategy.class);
	private static final String TMP_FILE_PREFIX = "atsd-driver";

	private File tmp;

	public FileStoreStrategy(StatementContext context) {
		super(context, Strategy.FILE);
	}

	@Override
	public void close() throws IOException {
		super.close();

		if (logger.isTraceEnabled()) {
			logger.trace("[close] " + status.getSyncLatch().getCount());
		}
		if (tmp != null) {
			final File file = tmp;
			if (file.exists()) {
				boolean deleted = file.delete();
				if (logger.isDebugEnabled()) {
					logger.debug("[close] File {} is deleted {} ", file.toString(), deleted);
				}
			}
		}
	}

	private static void copyFileUsingStream(InputStream is, File dest) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	@Override
	public void store(InputStream inputStream) throws IOException {
		tmp = File.createTempFile(TMP_FILE_PREFIX, null);
		if (logger.isDebugEnabled()) {
			logger.debug("[store] " + tmp);
		}
		copyFileUsingStream(inputStream, tmp);
		this.inputStream = new FileInputStream(tmp);
		final CountDownLatch syncLatch = status.getSyncLatch();
		if (syncLatch.getCount() != 0) {
			syncLatch.countDown();
		}
	}
}
