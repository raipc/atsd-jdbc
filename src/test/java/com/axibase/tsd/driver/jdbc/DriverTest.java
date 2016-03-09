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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.axibase.tsd.driver.jdbc.ext.AtsdConnection;
import com.axibase.tsd.driver.jdbc.ext.AtsdMeta;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class DriverTest implements Constants {
	@SuppressWarnings("unused")
	private static final LoggingFacade logger = LoggingFacade.getLogger(AtsdDriver.class);
	private static AtsdDriver driver;
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
		driver = new AtsdDriver();
		Class.forName("com.axibase.tsd.driver.jdbc.AtsdDriver");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		DriverManager.deregisterDriver(driver);
	}

	@Test
	public void testDriver() {
		try {
			driver = (AtsdDriver) DriverManager.getDriver(JDBC_ATDS_URL);
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
			connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
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
			connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
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
			connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);
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
			assertFalse(driver.acceptsURL(JDBC_ATDS_URL.toUpperCase()));
			assertTrue(driver.acceptsURL(JDBC_ATDS_URL));
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

}
