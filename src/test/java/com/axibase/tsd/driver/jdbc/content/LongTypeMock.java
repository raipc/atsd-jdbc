package com.axibase.tsd.driver.jdbc.content;

import static com.axibase.tsd.driver.jdbc.TestConstants.SELECT_ALL_CLAUSE;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axibase.tsd.driver.jdbc.AtsdProperties;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.protocol.SdkProtocolImpl;
import com.axibase.tsd.driver.jdbc.strategies.storage.FileStoreStrategy;
import com.axibase.tsd.driver.jdbc.strategies.stream.KeepAliveStrategy;

@RunWith(PowerMockRunner.class)
public class LongTypeMock extends AtsdProperties {
	private static final Logger logger = LoggerFactory.getLogger(LongTypeMock.class);
	private static final String CONTEXT_START = "{";
	private static final String TML_TABLE = "jdbc.driver.test.metric.long";
	private static final String TML_JSON_SCHEMA = String.format("/json/%s.jsonld", TML_TABLE);
	private SdkProtocolImpl protocolImpl;
	private boolean isDefaultStrategy;

	@Before
	public void setUp() throws Exception {
		final ContentDescription cd = new ContentDescription(HTTP_ATDS_URL, SELECT_ALL_CLAUSE + TML_TABLE, LOGIN_NAME,
				LOGIN_PASSWORD, null);
		cd.setJsonScheme(getSchema());
		this.protocolImpl = PowerMockito.spy(new SdkProtocolImpl(cd));
		isDefaultStrategy = READ_STRATEGY == null || READ_STRATEGY.equalsIgnoreCase("stream");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLongType() throws Exception {
		final StatementContext context = new StatementContext();

		try (IStoreStrategy storeStrategy = PowerMockito
				.spy(isDefaultStrategy ? new KeepAliveStrategy(context) : new FileStoreStrategy(context));) {
			fetch(storeStrategy, String.format("/csv/%S.csv", TML_TABLE), 1);
		}
	}

	private String getSchema() throws IOException {
		try (final InputStream is = this.getClass().getResourceAsStream(TML_JSON_SCHEMA);
				final Scanner scanner = new Scanner(is);) {
			scanner.useDelimiter("\\A");
			String json = scanner.hasNext() ? scanner.next() : "";
			assertTrue(json != null && json.length() != 0 && json.startsWith(CONTEXT_START));
			return json;
		}
	}

	private void fetch(IStoreStrategy storeStrategy, String resource, int fetchSize)
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
}
