package com.axibase.tsd.driver.jdbc.content.json;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class BuildInfoTest {
	private BuildInfo mock;
	
	@Before
	public void setUp() throws Exception {
		this.mock = PowerMockito.spy(new BuildInfo("", "", ""));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHashCode() {
		assertTrue(mock.hashCode() != 0);
	}

	@Test
	public void testBuildInfo() {
		new BuildInfo();
	}

	@Test
	public void testBuildInfoStringStringString() {
		new BuildInfo("", "", "");
	}

	@Test
	public void testGetRevisionNumber() {
		assertNotNull(mock.getRevisionNumber());
	}

	@Test
	public void testSetRevisionNumber() {
		mock.setRevisionNumber("");
	}

	@Test
	public void testWithRevisionNumber() {
		assertNotNull(mock.withRevisionNumber(""));
	}

	@Test
	public void testGetBuildNumber() {
		assertNotNull(mock.getBuildNumber());
	}

	@Test
	public void testSetBuildNumber() {
		mock.setBuildNumber("");
	}

	@Test
	public void testWithBuildNumber() {
		assertNotNull(mock.withBuildNumber(""));
	}

	@Test
	public void testGetBuildId() {
		assertNotNull(mock.getBuildId());
	}

	@Test
	public void testSetBuildId() {
		mock.setBuildId("");
	}

	@Test
	public void testWithBuildId() {
		assertNotNull(mock.withBuildId(""));
	}

	@Test
	public void testGetAdditionalProperties() {
		assertNotNull(mock.getAdditionalProperties());
	}

	@Test
	public void testSetAdditionalProperty() {
		mock.setAdditionalProperty("", "");
	}

	@Test
	public void testWithAdditionalProperty() {
		assertNotNull(mock.withAdditionalProperty("", ""));
	}

	@Test
	public void testEqualsObject() {
		assertTrue(mock.equals(mock));
	}

	@Test
	public void testToString() {
		assertNotNull(mock.toString());
	}

}
