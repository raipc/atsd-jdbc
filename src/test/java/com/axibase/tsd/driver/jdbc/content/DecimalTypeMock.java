package com.axibase.tsd.driver.jdbc.content;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;

@RunWith(PowerMockRunner.class)
public class DecimalTypeMock extends AbstractTypeMock {
	private static final Logger logger = LoggerFactory.getLogger(DecimalTypeMock.class);
	private static final String TYPE_MOCK_TABLE = "jdbc.driver.test.metric.decimal";
	private static final String TYPE_MOCK_TABLE_200 = TYPE_MOCK_TABLE + ".200";

	@Override
	protected String getTable() {
		return TYPE_MOCK_TABLE;
	}

	@Test
	public void testBidDecimalsType() throws Exception {
		try (final IStoreStrategy storeStrategy = getMockStrategyObject()) {
			fetch(storeStrategy, String.format("/csv/%S.csv", TYPE_MOCK_TABLE_200), 200);
		}
	}
}
