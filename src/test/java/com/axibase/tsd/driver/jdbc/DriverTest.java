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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.Meta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.axibase.tsd.driver.jdbc.ext.AtsdConnection;
import com.axibase.tsd.driver.jdbc.ext.AtsdMeta;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class DriverTest extends AtsdProperties {
	@SuppressWarnings("unused")
	private static final LoggingFacade logger = LoggingFacade.getLogger(AtsdDriver.class);


	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDriver() {
		try {
			driver = (AtsdDriver) DriverManager.getDriver(JDBC_ATSD_URL);
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		assertNotNull(driver);
		assertTrue(driver instanceof AtsdDriver);
	}

	@Test
	public void testGetConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(JDBC_ATSD_URL, LOGIN_NAME, LOGIN_PASSWORD);
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		assertNotNull(connection);
		try {
			connection.close();
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetConnectionSecure() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(JDBC_ATSD_URL, LOGIN_NAME, LOGIN_PASSWORD);
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		assertNotNull(connection);
		assertTrue(connection instanceof AtsdConnection);
		try {
			connection.close();
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public final void testCreateMeta() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(JDBC_ATSD_URL, LOGIN_NAME, LOGIN_PASSWORD);
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		assertTrue(connection instanceof AvaticaConnection);
		final Meta meta = driver.createMeta((AvaticaConnection) connection);
		assertNotNull(meta);
		assertTrue(meta instanceof AtsdMeta);
		try {
			connection.close();
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public final void testAcceptsURL() {
		try {
			assertFalse(driver.acceptsURL(""));
			assertFalse(driver.acceptsURL(JDBC_ATSD_URL.toUpperCase()));
			assertTrue(driver.acceptsURL(JDBC_ATSD_URL));
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

}
