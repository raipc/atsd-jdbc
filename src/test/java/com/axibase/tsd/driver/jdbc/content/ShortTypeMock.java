package com.axibase.tsd.driver.jdbc.content;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
public class ShortTypeMock extends AbstractTypeMock {
	private static final Logger logger = LoggerFactory.getLogger(ShortTypeMock.class);
	private static final String TYPE_MOCK_TABLE = "jdbc.driver.test.metric.short";

	@Override
	protected String getTable() {
		return TYPE_MOCK_TABLE;
	}
}
