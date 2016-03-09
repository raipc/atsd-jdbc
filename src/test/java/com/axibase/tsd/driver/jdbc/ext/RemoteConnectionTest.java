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
package com.axibase.tsd.driver.jdbc.ext;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Test;

import com.axibase.tsd.driver.jdbc.ConnectionTest;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.metrics.MetricsEnum;
import com.axibase.tsd.driver.jdbc.metrics.MetricsEnumLarge;
import com.axibase.tsd.driver.jdbc.metrics.MetricsEnumWithWarnings;

public class RemoteConnectionTest extends ConnectionTest {

	private static final LoggingFacade logger = LoggingFacade.getLogger(RemoteConnectionTest.class);

	@Test
	public final void smallRemoteStatement() throws AtsdException, SQLException {
		int count = checkRemoteStatement(SELECT_ALL_CLAUSE + SMALL_TABLE);
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
		assertTrue(count == 143);
	}

	@Test
	public final void mediumRemoteStatement() throws AtsdException, SQLException {
		int count = checkRemoteStatement(SELECT_ALL_CLAUSE + MEDIUM_TABLE);
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
	}

	@Test
	public final void largeRemoteStatement() throws AtsdException, SQLException {
		int count = checkRemoteStatement(SELECT_ALL_CLAUSE + LARGE_TABLE);
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
	}

	@Test(expected = SQLException.class)
	public final void hugeRemoteStatement() throws AtsdException, SQLException {
		int count = checkRemoteStatement(SELECT_ALL_CLAUSE + HUGE_TABLE);
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
	}

	@Test
	public final void testRemoteStatementWithFields() throws AtsdException, SQLException {
		int count = checkRemoteStatement(SELECT_TVE_CLAUSE + SMALL_TABLE);
		assertTrue(count == 143);
	}

	@Test
	public final void testRemoteStatementWithDates() throws AtsdException, SQLException {
		int count = checkRemoteStatement(SELECT_DVE_CLAUSE + SMALL_TABLE);
		assertTrue(count == 143);
	}

	@Test
	public final void testRemoteStatementWithJoins() throws AtsdException, SQLException {
		int count;
		count = checkRemoteStatement("SELECT * FROM cpu_busy OUTER JOIN disk_used WHERE time > now - 1 * hour");
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s-1]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
		count = checkRemoteStatement("SELECT * FROM cpu_busy JOIN cpu_idle WHERE time > now - 1 * hour");
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s-2]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
		count = checkRemoteStatement(
				"SELECT entity, time, AVG(cpu_busy.value), AVG(disk_used.value) FROM cpu_busy OUTER JOIN disk_used WHERE time > now - 1 * hour GROUP BY entity, period(15 minute)");
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s-3]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
		count = checkRemoteStatement(
				"SELECT entity, time, AVG(cpu_busy.value) FROM cpu_busy WHERE time > now - 1 * hour GROUP BY entity, period(15 minute) WITH row_number(entity, tags ORDER BY time DESC) <= 3");
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s-4]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
		count = checkRemoteStatement(
				"SELECT entity, datetime, AVG(cpu_busy.value) FROM cpu_busy WHERE time > now - 1 * hour GROUP BY entity, period(15 minute) WITH time > last_time - 30 * minute");
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s-5]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
		count = checkRemoteStatement(
				"SELECT entity, disk_used.time, cpu_busy.time, AVG(cpu_busy.value), AVG(disk_used.value), tags.* FROM cpu_busy JOIN USING entity disk_used WHERE time > now - 1 * hour GROUP BY entity, tags, period(15 minute)");
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s-6]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
	}

	@Test
	public final void testRemotePreparedStatement() throws AtsdException, SQLException {
		int count = checkRemotePreparedStatementNoArgs(SELECT_DVE_CLAUSE + SMALL_TABLE);
		assertTrue(count == 143);
	}

	@Test
	public final void testRemotePreparedStatementsWithArg() throws AtsdException, SQLException {
		int count = checkRemotePreparedStatementWithLimits(SELECT_ALL_CLAUSE + SMALL_TABLE + WHERE_CLAUSE,
				new String[] { "nurswgvml212" }, 1001, 10001);
		assertTrue(count == 143);
	}

	@Test
	public final void smallRemoteStatementTwice() throws AtsdException, SQLException {
		RETRIES = 2;
		int count = checkRemoteStatement(SELECT_ALL_CLAUSE + SMALL_TABLE);
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
		assertTrue(count == 143);
		RETRIES = 1;
	}

	@Test
	public final void testPreparedStatementsWithArgs() throws AtsdException, SQLException {
		checkRemotePreparedStatementWithLimits(
				"SELECT time, value, tags.file_system FROM df.disk_used_percent WHERE tags.file_system LIKE ? AND datetime between ? and ?",
				new String[] { "tmpfs", "2015-07-08T16:00:00Z", "2017-07-08T16:30:00Z" }, 1001, 10001);
	}

	@Test
	public final void testPreparedStatementsWithAggregation() throws AtsdException, SQLException {
		checkRemotePreparedStatementWithLimits(
				"SELECT count(*), entity, tags.*, period (30 minute) FROM df.disk_used "
						+ "WHERE entity = ? AND tags.mount_point = ? AND tags.file_system = ? "
						+ "AND datetime BETWEEN ? AND ? GROUP BY entity, tags, period (30 minute)",
				new String[] { "nurswgvml502", "/run", "tmpfs", "2015-07-08T16:00:00Z", "2017-07-08T16:30:00Z" }, 1001,
				10001);
	}

	@Test
	public final void testRemoteStatementsOnMetrics() throws AtsdException, SQLException {
		for (MetricsEnum metric : MetricsEnum.values()) {
			final String metricTable = metric.get();
			if (logger.isDebugEnabled())
				logger.debug("Metric: " + metricTable);
			checkRemoteStatementWithLimits(SELECT_ALL_CLAUSE + metricTable, 1001, 10000001);
		}
	}

	public final void testRemoteStatementsOnLargeMetrics() throws AtsdException, SQLException {
		for (MetricsEnumLarge metric : MetricsEnumLarge.values()) {
			final String metricTable = metric.get();
			if (logger.isDebugEnabled())
				logger.debug("MetricLarge: " + metricTable);
			checkRemoteStatementWithLimits(SELECT_ALL_CLAUSE + metricTable, 1001, 10000001);
		}
	}

	@Test
	public final void remoteStatementWithDifferentResultSets() throws AtsdException, SQLException {
		int count = checkRemoteStatementWithDifferentResultSets();
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
		// assertTrue(count == 16466);
	}

	public final void remoteStatementWithTraversingSimultaneously()
			throws AtsdException, SQLException, InterruptedException {
		int count = checkStatementWithTraversingSimultaneously();
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]%d", new Object() {
			}.getClass().getEnclosingMethod().getName(), count));
	}

	public final void testRemoteStatementsWithLimits() throws AtsdException, SQLException {
		final String query = SELECT_ALL_CLAUSE + SMALL_TABLE;
		int count = checkRemoteStatementWithLimits(query, 101, 10001);
		assertTrue(count == 143);
		count = checkRemoteStatementWithLimits(query, 9, 99);
		assertTrue(count == 99);
		count = checkRemoteStatementWithLimits(query, 10, 100);
		assertTrue(count == 100);
		count = checkRemoteStatementWithLimits(query, 11, 101);
		assertTrue(count == 101);
		count = checkRemoteStatementWithLimits(query, 1, 10);
		assertTrue(count == 10);
		count = checkRemoteStatementWithLimits(query, 2, 1);
		assertTrue(count == 1);
		count = checkRemoteStatementWithLimits(query, 1, 1);
		assertTrue(count == 1);
		count = checkRemoteStatementWithLimits(query, 10, 0);
		assertTrue(count == 143);
		count = checkRemoteStatementWithLimits(query, 0, 0);
		assertTrue(count == 143);
		count = checkRemoteStatementWithLimits(query, -1, 10);
		assertTrue(count == 10);
		count = checkRemoteStatementWithLimits(query, -1, 0);
		assertTrue(count == 143);
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void smallRemoteStatementWithAbsPos() throws AtsdException, SQLException {
		checkRemoteStatementWithAbsolute(SELECT_ALL_CLAUSE + SMALL_TABLE);
	}

	@Test(expected = UnsupportedOperationException.class)
	public final void smallRemoteStatementWithRelPos() throws AtsdException, SQLException {
		checkRemoteStatementWithRelative(SELECT_ALL_CLAUSE + SMALL_TABLE);
	}

	@Test(expected = SQLException.class)
	public final void wrongRemoteStatement() throws AssertionError, AtsdException, SQLException {
		try {
			checkRemoteStatement(SELECT_ALL_CLAUSE + WRONG_TABLE);
		} catch (final SQLException e) {
			throw e;
		}
	}

	@Test
	public final void wrongRemoteStatements() throws AtsdException {
		for (MetricsEnumWithWarnings metric : MetricsEnumWithWarnings.values()) {
			final String metricTable = metric.get();
			if (logger.isDebugEnabled())
				logger.debug("Metric: " + metricTable);
			try {
				checkRemoteStatementWithLimits(SELECT_ALL_CLAUSE + metricTable, 1001, 10000001);
				fail();
			} catch (final SQLException sqle) {
				if (logger.isDebugEnabled())
					logger.debug("SQLException: " + sqle.getMessage());
			}
		}
	}

}
