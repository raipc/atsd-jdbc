package com.axibase.tsd.driver.jdbc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.DriverVersion;
import org.apache.calcite.avatica.Meta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axibase.tsd.driver.jdbc.ext.AtsdConnection;
import com.axibase.tsd.driver.jdbc.ext.AtsdFactory;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AtsdDriver.class)
public class AtsdDriverTest {
	private static final String HTTP_HOST_API_SQL = "http://host/api/sql";
	private static final String JDBC_URL = DriverConstants.CONNECT_URL_PREFIX + HTTP_HOST_API_SQL;
	private AtsdDriver driver;
	private AvaticaConnection conn;

	@Before
	public void setUp() throws Exception {
		final AtsdFactory atsdFactory = new AtsdFactory();
		this.driver = PowerMockito.spy(new AtsdDriver());
		this.conn = PowerMockito.spy(new AtsdConnection(driver, atsdFactory, JDBC_URL, new Properties()));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateDriverVersion() {
		DriverVersion driverVersion = driver.createDriverVersion();
		assertNotNull(driverVersion);
	}

	@Test
	public void testGetConnectStringPrefix() {
		String connectStringPrefix = driver.getConnectStringPrefix();
		assertNotNull(connectStringPrefix);
	}

	@Test
	public void testCreateMetaAvaticaConnection() {
		Meta meta = driver.createMeta(conn);
		assertNotNull(meta);
	}

	@Test
	public void testConnectStringProperties() throws Exception {
		Connection connection = driver.connect(JDBC_URL, new Properties());
		assertNotNull(connection);
	}

	@Test
	public void testAcceptsURLString() throws SQLException {
		boolean accepted = driver.acceptsURL(DriverConstants.CONNECT_URL_PREFIX);
		assertTrue(accepted);
	}

}
