package com.axibase.tsd.driver.jdbc.content;

import com.axibase.tsd.driver.jdbc.AtsdProperties;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.protocol.SdkProtocolImpl;
import com.axibase.tsd.driver.jdbc.strategies.StrategyFactory;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertTrue;

public abstract class AbstractFetchTest extends AtsdProperties {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractTypeMock.class);
	protected SdkProtocolImpl protocolImpl;

	protected void fetch(final IStoreStrategy storeStrategy, String resource, int fetchSize)
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
			if (exception != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("SQLException: " + exception.getMessage());
				}
				throw exception;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Fetched: " + fetched.size());
			}
			assertTrue(fetched.size() == fetchSize);
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
			throw new AtsdException(e.getMessage());
		} finally {
			if (mockIs != null) {
				mockIs.close();
			}
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Test [ContentProvider->fetch] is done in " + (System.currentTimeMillis() - start) + " msecs");
			}
		}
	}

	protected IStoreStrategy getMockStrategyObject() {
		StatementContext context = new StatementContext();
		return PowerMockito.spy(StrategyFactory.create(StrategyFactory.findClassByName(READ_STRATEGY), context));
	}
}
