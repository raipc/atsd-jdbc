package com.axibase.tsd.driver.jdbc.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipInputStream;

import com.axibase.tsd.driver.jdbc.AtsdProperties;
import com.axibase.tsd.driver.jdbc.enums.AtsdType;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.protocol.SdkProtocolImpl;
import com.axibase.tsd.driver.jdbc.strategies.StrategyFactory;
import com.axibase.tsd.driver.jdbc.util.EnumUtil;
import org.apache.calcite.avatica.ColumnMetaData;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.axibase.tsd.driver.jdbc.content.ContentMetadata.getAvaticaType;
import static org.junit.Assert.assertTrue;

public abstract class AbstractFetchTest extends AtsdProperties {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractTypeMock.class);
	protected SdkProtocolImpl protocolImpl;

	private InputStream getInputStreamForResource(String resource) throws IOException {
		final InputStream mockIs = this.getClass().getResourceAsStream(resource);
		if (resource.endsWith(".zip")) {
			ZipInputStream mockZip = new ZipInputStream(mockIs);
			mockZip.getNextEntry();
			return mockZip;
		}
		return mockIs;
	}

	protected List<ColumnMetaData> prepareMetadata(String resource) {
		try (InputStream inputStream = getInputStreamForResource(resource);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))){
			String header = reader.readLine();
			if (header.startsWith("#")) {
				return Collections.singletonList(ColumnMetaData.dummy(getAvaticaType(AtsdType.STRING_DATA_TYPE), false));
			} else {
				String[] columnNames = header.split(",");
				ColumnMetaData[] meta = new ColumnMetaData[columnNames.length];
				for (int i = 0; i < columnNames.length; i++) {
					final String columnName = columnNames[i];
					meta[i] = new ContentMetadata.ColumnMetaDataBuilder()
							.withName(columnName)
							.withTitle(columnName)
							.withColumnIndex(i)
							.withNullable(columnName.startsWith("tag") ? 1 : 0)
							.withAtsdType(EnumUtil.getAtsdTypeByColumnName(columnName))
							.build();
				}
				return Arrays.asList(meta);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void fetch(final IStoreStrategy storeStrategy, String resource, int fetchSize)
			throws AtsdException, IOException, GeneralSecurityException, SQLException {
		long start = System.currentTimeMillis();
		final InputStream mockIs = getInputStreamForResource(resource);
		try {
			PowerMockito.doReturn(mockIs).when(protocolImpl, "readContent");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AtsdException(e.getMessage());
		}
		try {
			final InputStream inputStream = protocolImpl.readContent();
			storeStrategy.store(inputStream);
			storeStrategy.openToRead(prepareMetadata(resource));
			final List<Object[]> fetched = storeStrategy.fetch(0L, fetchSize);
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
