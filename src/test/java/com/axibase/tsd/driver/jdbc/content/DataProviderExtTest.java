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
package com.axibase.tsd.driver.jdbc.content;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axibase.tsd.driver.jdbc.AtsdProperties;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IContentProtocol;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.protocol.SdkProtocolImpl;
import com.axibase.tsd.driver.jdbc.strategies.storage.FileStoreStrategy;
import com.axibase.tsd.driver.jdbc.strategies.stream.KeepAliveStrategy;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DataProvider.class)
public class DataProviderExtTest extends AtsdProperties {
	private static final Logger logger = LoggerFactory.getLogger(DataProviderExtTest.class);
	private IContentProtocol protocolImpl;
	private boolean isDefaultStrategy;

	@Before
	public void setUp() throws Exception {
		protocolImpl = PowerMockito.mock(SdkProtocolImpl.class);
		isDefaultStrategy = READ_STRATEGY == null || READ_STRATEGY.equalsIgnoreCase("stream");
	}

	@Test
	public void testStrategyOnOne() throws Exception {
		final StatementContext context = new StatementContext();
		try (final IStoreStrategy storeStrategy = PowerMockito
				.spy(isDefaultStrategy ? new KeepAliveStrategy(context) : new FileStoreStrategy(context));) {
			fetch(storeStrategy, "/csv/1.csv", 1);
		}
	}

	@Test
	public void testStrategyStrategyOn143() throws Exception {
		final StatementContext context = new StatementContext();
		try (final IStoreStrategy storeStrategy = PowerMockito
				.spy(isDefaultStrategy ? new KeepAliveStrategy(context) : new FileStoreStrategy(context));) {
			fetch(storeStrategy, "/csv/143.csv", 143);
		}
	}

	@Test
	public void testStrategyOn20001() throws Exception {
		final StatementContext context = new StatementContext();
		try (final IStoreStrategy storeStrategy = PowerMockito
				.spy(isDefaultStrategy ? new KeepAliveStrategy(context) : new FileStoreStrategy(context));) {
			fetch(storeStrategy, "/csv/20001.csv", 20001);
		}
	}

	@Test(expected = SQLException.class)
	public void testStrategyOnSqleWithoutRecords() throws Exception {
		final StatementContext context = new StatementContext();
		try (final IStoreStrategy storeStrategy = PowerMockito
				.spy(isDefaultStrategy ? new KeepAliveStrategy(context) : new FileStoreStrategy(context));) {
			fetch(storeStrategy, "/csv/docker.network.eth0.rxerrors.csv", 1);
		}
	}

	@Test(expected = SQLException.class)
	public void testStrategyOnSqleWithRecords() throws Exception {
		final StatementContext context = new StatementContext();
		try (final IStoreStrategy storeStrategy = PowerMockito
				.spy(isDefaultStrategy ? new KeepAliveStrategy(context) : new FileStoreStrategy(context));) {
			fetch(storeStrategy, "/csv/df.bytes.free.csv.zip", 18835);
		}
	}

	@Test(expected = SQLException.class)
	public void testStrategyOnSqleWithManyRecords() throws Exception {
		final StatementContext context = new StatementContext();
		try (final IStoreStrategy storeStrategy = PowerMockito
				.spy(isDefaultStrategy ? new KeepAliveStrategy(context) : new FileStoreStrategy(context));) {
			fetch(storeStrategy, "/csv/gc_time_persent.csv.zip", 323115);
		}
	}

	private void fetch(final IStoreStrategy storeStrategy, String resource, int fetchSize)
			throws AtsdException, IOException, GeneralSecurityException, SQLException {
		long start = System.currentTimeMillis();
		final InputStream mockIs = this.getClass().getResourceAsStream(resource);
		ZipInputStream mockZip = null;
		final boolean zipped = resource.endsWith(".zip");
		if (zipped) {
			mockZip = new ZipInputStream(mockIs);
			mockZip.getNextEntry();
		}
		try {
			PowerMockito.doReturn(zipped ? mockZip : mockIs).when(protocolImpl, "readContent");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AtsdException(e.getMessage());
		}
		try {
			final InputStream is = protocolImpl.readContent();
			storeStrategy.store(is);
			storeStrategy.openToRead();
			final List<String[]> fetched = storeStrategy.fetch(0L, fetchSize);
			final StatementContext context = storeStrategy.getContext();
			final SQLException exception = context.getException();
			if (context != null && exception != null) {
				if (logger.isDebugEnabled())
					logger.debug("SQLException: " + exception.getMessage());
				throw exception;
			}
			if (logger.isDebugEnabled())
				logger.debug("Fetched: " + fetched.size());
			assertTrue(fetched.size() == fetchSize);
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
			throw new AtsdException(e.getMessage());
		} finally {
			if (mockIs != null)
				mockIs.close();
			if (logger.isDebugEnabled())
				logger.debug(
						"Test [ContentProvider->fetch] is done in " + (System.currentTimeMillis() - start) + " msecs");
		}
	}

	@After
	public void after() throws Exception {

	}
}
