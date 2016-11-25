package com.axibase.tsd.driver.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.axibase.tsd.driver.jdbc.ext.AtsdConnection;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.DriverVersion;
import org.apache.calcite.avatica.Meta;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.axibase.tsd.driver.jdbc.TestConstants.JDBC_ATSD_URL_PREFIX;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*"})
@PrepareForTest(AtsdDriver.class)
public class AtsdDriverTest extends AtsdProperties {
	private AtsdDriver driver;
	private AvaticaConnection conn;

	@Rule
	public ExpectedException exception = ExpectedException.none();


	@Before
	public void setUp() throws Exception {
		this.driver = PowerMockito.spy(new AtsdDriver());
		this.conn = PowerMockito.mock(AtsdConnection.class);
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
		Properties properties = new Properties();
		properties.setProperty("user", LOGIN_NAME);
		properties.setProperty("password", LOGIN_PASSWORD);
		Connection connection = driver.connect(JDBC_ATSD_URL, properties);
		assertNotNull(connection);
		connection.close();
	}

	@Test
	public void testConnectWithoutCredentials() throws Exception {
		exception.expect(SQLException.class);
		exception.expectMessage("Wrong credentials provided");
		Connection connection = driver.connect(JDBC_ATSD_URL, new Properties());
		connection.close();
	}

	@Test
	public void testConnectToWrongUrl() throws Exception {
		exception.expect(SQLException.class);
		exception.expectMessage("Unknown host specified");
		Connection connection = driver.connect(JDBC_ATSD_URL_PREFIX + "https://unknown:443/api/sql", new Properties());
		connection.close();
	}

	@Test
	public void testAcceptsURLString() throws SQLException {
		boolean accepted = driver.acceptsURL(DriverConstants.CONNECT_URL_PREFIX);
		assertTrue(accepted);
	}

}
