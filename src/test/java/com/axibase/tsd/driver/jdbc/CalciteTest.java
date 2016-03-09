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

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.calcite.linq4j.function.Function1;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class CalciteTest {
	private static final LoggingFacade logger = LoggingFacade.getLogger(AtsdDriver.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSelect() throws SQLException {
		checkSql("aximodel", "select * from AXI");
	}

	@Test
	public void testPushDownProjectDumb() throws SQLException {
		checkSql("aximodel", "explain plan for select * from AXI",
				"PLAN=EnumerableInterpreter\n" + "  BindableTableScan(table=[[AXI, AXI]])\n");
	}

	@Test
	public void testDateType() throws SQLException {
		Properties info = new Properties();
		info.put("model", jsonPath("aximodel"));
		try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
			ResultSet tables = connection.getMetaData().getTables(null, null, null, null);
			Assert.assertTrue(tables.next());
			ResultSet res = connection.getMetaData().getColumns(null, null, "AXI", "datetime");
			Assert.assertTrue(res.next());
			Assert.assertEquals(res.getInt("DATA_TYPE"), java.sql.Types.VARCHAR);

			res = connection.getMetaData().getColumns(null, null, "AXI", "value");
			Assert.assertTrue(res.next());
			Assert.assertEquals(res.getInt("DATA_TYPE"), java.sql.Types.VARCHAR);

			res = connection.getMetaData().getColumns(null, null, "AXI", "entity");
			Assert.assertTrue(res.next());
			Assert.assertEquals(res.getInt("DATA_TYPE"), java.sql.Types.VARCHAR);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select \"datetime\", \"value\", \"entity\" from \"AXI\"");
			ResultSetMetaData metaData = resultSet.getMetaData();
			int count = 0;
			while (resultSet.next()) {
				for (int i = 1; i <= 3; i++) {
					String label = metaData.getColumnLabel(i);
					if (logger.isTraceEnabled())
						logger.trace(String.format("Label:%s\tValue: %s\t", label, resultSet.getString(i)));
				}
				count++;
			}
			assertTrue(count == 143);
		}
	}

	private void checkSql(String model, String sql) throws SQLException {
		checkSql(sql, model, output());
	}

	private Function1<ResultSet, Void> output() {
		return new Function1<ResultSet, Void>() {
			public Void apply(ResultSet resultSet) {
				try {
					output(resultSet);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				return null;
			}
		};
	}

	private void collect(List<String> result, ResultSet resultSet) throws SQLException {
		final StringBuilder buf = new StringBuilder();
		while (resultSet.next()) {
			buf.setLength(0);
			int n = resultSet.getMetaData().getColumnCount();
			String sep = "";
			for (int i = 1; i <= n; i++) {
				buf.append(sep).append(resultSet.getMetaData().getColumnLabel(i)).append("=")
						.append(resultSet.getString(i));
				sep = "; ";
			}
			result.add(buf.toString().replaceAll("\r\n", "\n"));
		}
	}

	private void output(ResultSet resultSet) throws SQLException {
		if (!logger.isTraceEnabled())
			return;
		final ResultSetMetaData metaData = resultSet.getMetaData();
		final int columnCount = metaData.getColumnCount();
		while (resultSet.next()) {
			for (int i = 1;; i++) {
				logger.trace(resultSet.getString(i));
				if (i < columnCount) {
					logger.trace(", ");
				} else {
					logger.trace("\n");
					break;
				}
			}
		}
	}

	private void checkSql(String sql, String model, Function1<ResultSet, Void> fn) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		try {
			Properties info = new Properties();
			info.put("model", jsonPath(model));
			connection = DriverManager.getConnection("jdbc:calcite:", info);
			statement = connection.createStatement();
			final ResultSet resultSet = statement.executeQuery(sql);
			fn.apply(resultSet);
		} finally {
			close(connection, statement);
		}
	}

	private String jsonPath(String model) {
		final URL url = this.getClass().getResource("/models/" + model + ".json");
		String s = url.toString();
		if (s.startsWith("file:")) {
			s = s.substring("file:".length());
		}
		return s;
	}

	private void close(Connection connection, Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException ignored) {}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException ignored) {}
		}
	}

	private void checkSql(String model, String sql, final String... expected) throws SQLException {
		checkSql(sql, model, expect(expected));
	}

	private Function1<ResultSet, Void> expect(final String... expected) {
		return new Function1<ResultSet, Void>() {
			public Void apply(ResultSet resultSet) {
				try {
					final List<String> lines = new ArrayList<>();
					collect(lines, resultSet);
					Assert.assertEquals(Arrays.asList(expected), lines);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				return null;
			}
		};
	}

}
