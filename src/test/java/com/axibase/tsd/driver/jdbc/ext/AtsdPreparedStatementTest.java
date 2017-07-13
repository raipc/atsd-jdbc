package com.axibase.tsd.driver.jdbc.ext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.axibase.tsd.driver.jdbc.AtsdProperties;
import org.apache.calcite.avatica.AvaticaConnection;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AtsdPreparedStatementTest extends AtsdProperties {
	private static final Map<String, Level> OVERRIDDEN_LOG_LEVELS = new HashMap<>(2);

	private static AvaticaConnection connection;

	@BeforeClass
	public static void beforeClass() throws SQLException {
		connection = (AvaticaConnection) DriverManager.getConnection(JDBC_ATSD_URL, LOGIN_NAME, LOGIN_PASSWORD);
		Assert.assertNotNull(connection);

		setLoggerLevel(Logger.ROOT_LOGGER_NAME, Level.TRACE);
		setLoggerLevel("org.apache.calcite.sql.pretty.SqlPrettyWriter", Level.INFO);
	}

	@AfterClass
	public static void afterClass() throws SQLException {
		revertLoggerLevel(Logger.ROOT_LOGGER_NAME);
		revertLoggerLevel("org.apache.calcite.sql.pretty.SqlPrettyWriter");

		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	private static void setLoggerLevel(String name, Level level) {
		Logger logger = (Logger) LoggerFactory.getLogger(name);
		OVERRIDDEN_LOG_LEVELS.put(Logger.ROOT_LOGGER_NAME, logger.getEffectiveLevel());
		logger.setLevel(level);
	}

	private static void revertLoggerLevel(String name) {
		Logger logger = (Logger) LoggerFactory.getLogger(name);
		logger.setLevel(OVERRIDDEN_LOG_LEVELS.get(Logger.ROOT_LOGGER_NAME));
	}

	@Test
	public void testGetMetaData_MetricDoesNotExist() throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM nonexistent_metric")) {
			stmt.getMetaData();
		}
	}

	@Test
	public void testGetMetaData() throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gc_time_percent ")) {
			ResultSetMetaData rsmd = stmt.getMetaData();
			Assert.assertEquals(7, rsmd.getColumnCount());
		}
	}

}