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
package com.axibase.tsd.driver.jdbc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.metrics.TwoMetricsEnum;

public class ConnectionTest implements Constants {
	private static final LoggingFacade logger = LoggingFacade.getLogger(ConnectionTest.class);

	protected AtsdDriver driver;
	protected static int RETRIES = 1;
	protected static String JDBC_ATDS_URL;
	protected static String LOGIN_NAME;
	protected static String LOGIN_PASSWORD;
	protected static boolean TRUST_URL;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JDBC_ATDS_URL = JDBC_ATDS_URL_PREFIX + System.getProperty("test.url");
		LOGIN_NAME = System.getProperty("test.username");
		LOGIN_PASSWORD = System.getProperty("test.password");
		TRUST_URL = Boolean.valueOf(System.getProperty("test.trust"));
	}

	@Before
	public void setUp() throws Exception {
		DriverManager.registerDriver(new AtsdDriver());
		final Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			final Driver nextElement = drivers.nextElement();
			if (logger.isDebugEnabled())
				logger.debug("Driver: " + nextElement);
		}
		try {
			driver = (AtsdDriver) DriverManager.getDriver(JDBC_ATDS_URL);
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@After
	public void tearDown() throws Exception {
		DriverManager.deregisterDriver(driver);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	protected int checkRemoteStatement(String sql) throws AtsdException, SQLException {
		long start = System.currentTimeMillis();
		try (final Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
				final Statement statement = connection.createStatement();) {
			int count = 0;
			for (int i = 0; i < RETRIES; i++) {
				try (final ResultSet resultSet = statement.executeQuery(sql);) {
					count = printResultSet(resultSet);
				}
			}
			return count;
		} finally {
			logTime(start, new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
	}

	protected int checkRemoteStatementWithDifferentResultSets() throws AtsdException, SQLException {
		long start = System.currentTimeMillis();
		try (final Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
				final Statement statement = connection.createStatement();) {
			int count = 0;
			for (TwoMetricsEnum metric : TwoMetricsEnum.values()) {
				final String metricTable = metric.get();
				if (logger.isDebugEnabled())
					logger.debug("Metric: " + metricTable);
				try (final ResultSet resultSet = statement.executeQuery(SELECT_ALL_CLAUSE + metricTable);) {
					count += printResultSet(resultSet);
				}
			}
			return count;
		} finally {
			logTime(start, new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
	}

	protected int checkStatementWithTraversingSimultaneously()
			throws AtsdException, SQLException, InterruptedException {
		long start = System.currentTimeMillis();
		ExecutorService service = Executors.newFixedThreadPool(2);
		try (final Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
				final Statement statement = connection.createStatement();) {
			for (TwoMetricsEnum metric : TwoMetricsEnum.values()) {
				final String metricTable = metric.get();
				if (logger.isDebugEnabled())
					logger.debug("Metric: " + metricTable);
				service.submit(new Runnable() {
					public void run() {
						try (final ResultSet resultSet = statement.executeQuery(SELECT_ALL_CLAUSE + metricTable);) {
							printResultSet(resultSet);
						} catch (SQLException | AtsdException e) {
							e.printStackTrace();
							fail();
						}
					}
				});
			}
			Thread.sleep(1000);
			for (TwoMetricsEnum metric : TwoMetricsEnum.values()) {
				final String metricTable = metric.get();
				if (logger.isDebugEnabled())
					logger.debug("Metric: " + metricTable);
				try (final ResultSet resultSet = statement.executeQuery(SELECT_ALL_CLAUSE + metricTable);) {
					printResultSet(resultSet);
				} catch (SQLException | AtsdException e) {
					e.printStackTrace();
					fail();
				}
			}
		} finally {
			logTime(start, new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
		return 4;
	}

	protected int checkRemoteStatementWithAbsolute(String sql) throws AtsdException, SQLException {
		long start = System.currentTimeMillis();
		try (final Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
				final Statement statement = connection.createStatement();
				final ResultSet resultSet = statement.executeQuery(sql);) {
			resultSet.absolute(100);
			return printResultSet(resultSet);
		} finally {
			logTime(start, new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
	}

	protected int checkRemoteStatementWithRelative(String sql) throws AtsdException, SQLException {
		long start = System.currentTimeMillis();
		try (final Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
				final Statement statement = connection.createStatement();
				final ResultSet resultSet = statement.executeQuery(sql);) {
			resultSet.relative(100);
			return printResultSet(resultSet);
		} finally {
			logTime(start, new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
	}

	protected int checkRemoteStatementWithLimits(String sql, int fetchSize, int maxRows)
			throws AtsdException, SQLException {
		long start = System.currentTimeMillis();
		try (final Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
				final Statement statement = connection.createStatement();) {
			statement.setFetchSize(fetchSize);
			statement.setMaxRows(maxRows);
			try (final ResultSet resultSet = statement.executeQuery(sql);) {
				return printResultSet(resultSet);
			}
		} finally {
			logTime(start, new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
	}

	protected int checkRemotePreparedStatementNoArgs(String sql) throws AtsdException, SQLException {
		long start = System.currentTimeMillis();
		try (final Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
				final PreparedStatement prepareStatement = connection.prepareStatement(sql);
				final ResultSet resultSet = prepareStatement.executeQuery();) {
			return printResultSet(resultSet);
		} finally {
			logTime(start, new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
	}

	protected int checkRemotePreparedStatementWithLimits(String sql, String[] args, int fetchSize, int maxRows)
			throws AtsdException, SQLException {
		long start = System.currentTimeMillis();
		try (final Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
				final PreparedStatement prepareStatement = connection.prepareStatement(sql);) {
			prepareStatement.setFetchSize(fetchSize);
			prepareStatement.setMaxRows(maxRows);
			int num = 1;
			for (String arg : args) {
				prepareStatement.setString(num++, arg);
			}
			try (final ResultSet resultSet = prepareStatement.executeQuery();) {
				return printResultSet(resultSet);
			}
		} finally {
			logTime(start, new Object() {
			}.getClass().getEnclosingMethod().getName());
		}
	}

	protected int printResultSet(final ResultSet resultSet) throws AtsdException, SQLException {
		assertNotNull(resultSet);
		final ResultSetMetaData rsmd = resultSet.getMetaData();
		assertNotNull(rsmd);
		if (logger.isDebugEnabled())
			logger.debug("Columns:");
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			int type = rsmd.getColumnType(i);
			String name = rsmd.getColumnName(i);
			String typeName = rsmd.getColumnTypeName(i);
			if (logger.isDebugEnabled())
				logger.debug(String.format("%s\t%s    \t%s", type, name, typeName));
		}
		if (logger.isTraceEnabled())
			logger.trace("Data:");
		int count = 0;
		StringBuilder sb;
		while (resultSet.next()) {
			sb = new StringBuilder();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				int type = rsmd.getColumnType(i);
				if (i > 1)
					sb.append("     \t");
				sb.append(type + ":");
				switch (type) {
				case Types.CHAR:
				case Types.VARCHAR:
					sb.append("getString: " + resultSet.getString(i));
					break;
				case Types.INTEGER:
					sb.append("getInt: " + resultSet.getInt(i));
					break;
				case Types.BIGINT:
					sb.append("getLong: " + resultSet.getLong(i));
					break;
				case Types.SMALLINT:
					sb.append("getShort: " + resultSet.getShort(i));
					break;
				case Types.FLOAT:
					sb.append("getFloat: " + resultSet.getFloat(i));
					break;
				case Types.DOUBLE:
					sb.append("getDouble: " + resultSet.getDouble(i));
					break;
				case Types.TIMESTAMP:
					sb.append("getTimestamp: " + resultSet.getTimestamp(i).toString());
					break;
				default:
					throw new UnsupportedOperationException();
				}
			}
			count++;
			if (logger.isTraceEnabled()) {
				logger.trace(sb.toString());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Total: " + count);
		}
		final SQLWarning warnings = resultSet.getWarnings();
		if (warnings != null)
			warnings.printStackTrace();
		return count;
	}

	private void logTime(long start, String name) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("Test [%s] is done in %d msecs", name, (System.currentTimeMillis() - start)));
	}
}
