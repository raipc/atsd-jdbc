package com.axibase.tsd.driver.jdbc.content;

import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.protocol.SdkProtocolImpl;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static com.axibase.tsd.driver.jdbc.TestConstants.SELECT_ALL_CLAUSE;
import static org.junit.Assert.assertTrue;

public abstract class AbstractTypeMock extends AbstractFetchTest {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractTypeMock.class);
	protected static final String CONTEXT_START = "{";
	protected boolean isDefaultStrategy;

	@Before
	public void setUp() throws Exception {
		final ContentDescription cd = new ContentDescription(HTTP_ATSD_URL, SELECT_ALL_CLAUSE + getTable(), LOGIN_NAME,
				LOGIN_PASSWORD, null);
		cd.setJsonScheme(getSchema());
		this.protocolImpl = PowerMockito.spy(new SdkProtocolImpl(cd));
		isDefaultStrategy = READ_STRATEGY == null || READ_STRATEGY.equalsIgnoreCase("memory");
	}

	@Test
	public void testType() throws Exception {
		try (final IStoreStrategy storeStrategy = getMockStrategyObject()) {
			fetch(storeStrategy, String.format("/csv/%s.csv", getTable()), 1);
		}
	}

	protected abstract String getTable();

	protected String getJsonSchema() {
		return String.format("/json/%s.jsonld", getTable());
	}

	protected String getSchema() throws IOException {
		try (final InputStream is = this.getClass().getResourceAsStream(getJsonSchema());
			 final Scanner scanner = new Scanner(is)) {
			scanner.useDelimiter("\\A");
			String json = scanner.hasNext() ? scanner.next() : "";
			assertTrue(json != null && json.length() != 0 && json.startsWith(CONTEXT_START));
			return json;
		}
	}
}
