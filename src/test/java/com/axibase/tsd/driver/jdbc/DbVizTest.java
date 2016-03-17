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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class DbVizTest extends AtsdProperties {
	private static final LoggingFacade logger = LoggingFacade.getLogger(DbVizTest.class);

	@Test
	public void checkDatabaseMetadata() throws ClassNotFoundException, SQLException {
		Class.forName("com.axibase.tsd.driver.jdbc.AtsdDriver");
		try (Connection connection = DriverManager.getConnection(JDBC_ATDS_URL, LOGIN_NAME, LOGIN_PASSWORD);) {
			assertNotNull(connection);
			if (logger.isDebugEnabled())
				logger.debug(connection.toString());
			final DatabaseMetaData metaData = connection.getMetaData();
			final String databaseProductName = metaData.getDatabaseProductName();
			final String databaseProductVersion = metaData.getDatabaseProductVersion();
			final String driverName = metaData.getDriverName();
			final String driverVersion = metaData.getDriverVersion();
			if (logger.isDebugEnabled()) {
				logger.debug("databaseProductName: " + databaseProductName);
				logger.debug("databaseProductVersion: " + databaseProductVersion);
				logger.debug("driverName: " + driverName);
				logger.debug("driverVersion: " + driverVersion);
			}
			assertNotNull(metaData);
			final ResultSet rs0 = metaData.getTypeInfo();
			assertNotNull(rs0);
			while (rs0.next()) {
				final String name = rs0.getString("TYPE_NAME");
				final int type = rs0.getInt("DATA_TYPE");
				final int precision = rs0.getInt("PRECISION");
				final boolean isCS = rs0.getBoolean("CASE_SENSITIVE");
				if (logger.isDebugEnabled()) {
					logger.debug("TypeInfo Name: " + name);
					logger.debug("TypeInfo Type: " + type);
					logger.debug("TypeInfo Precision: " + precision);
					logger.debug("TypeInfo CS: " + isCS);
				}
			}
			final ResultSet rs1 = metaData.getTableTypes();
			assertNotNull(rs1);
			while (rs1.next()) {
				final String type = rs1.getString(1);
				if (logger.isDebugEnabled())
					logger.debug("TableTypes: " + type);
			}
			final ResultSet rs2 = metaData.getCatalogs();
			assertNotNull(rs2);
			while (rs2.next()) {
				final String catalog = rs2.getString(1);
				if (logger.isDebugEnabled())
					logger.debug("Catalog: " + catalog);
				final ResultSet rs3 = metaData.getSchemas(catalog, null);
				assertNotNull(rs3);
				while (rs3.next()) {
					final String schema = rs3.getString(1);
					if (logger.isDebugEnabled())
						logger.debug("Schema: " + schema);
				}
			}

		}
	}

}
