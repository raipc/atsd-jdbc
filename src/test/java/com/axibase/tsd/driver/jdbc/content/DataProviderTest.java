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
package com.axibase.tsd.driver.jdbc.content;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.axibase.tsd.driver.jdbc.Constants;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class DataProviderTest implements Constants {
	private static final LoggingFacade logger = LoggingFacade.getLogger(DataProvider.class);

	protected static String HTTP_ATDS_URL;
	protected static String LOGIN_NAME;
	protected static String LOGIN_PASSWORD;
	protected static boolean TRUST_URL;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		HTTP_ATDS_URL = System.getProperty("test.url");
		LOGIN_NAME = System.getProperty("test.username");
		LOGIN_PASSWORD = System.getProperty("test.password");
		TRUST_URL = Boolean.valueOf(System.getProperty("test.trust"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public final void testSecureByDefault() throws Exception {
		try (DataProvider provider = new DataProvider(HTTP_ATDS_URL, SELECT_ALL_CLAUSE + SMALL_TABLE + SELECT_LIMIT,
				LOGIN_NAME, LOGIN_PASSWORD, new StatementContext());) {
			provider.checkScheme(provider.getContentDescription().getQuery());
		}
	}

	@Ignore
	@Test
	public final void testSecureTrusted() throws Exception {
		try (DataProvider provider = new DataProvider(HTTP_ATDS_URL + TRUST_PARAMETER_IN_QUERY,
				SELECT_ALL_CLAUSE + SMALL_TABLE + SELECT_LIMIT, LOGIN_NAME, LOGIN_PASSWORD, new StatementContext());) {
			provider.checkScheme(provider.getContentDescription().getQuery());
		}
	}

	@Ignore
	@Test
	public final void testSecureUntrusted() throws Exception {
		try (DataProvider provider = new DataProvider(HTTP_ATDS_URL + UNTRUST_PARAMETER_IN_QUERY,
				SELECT_ALL_CLAUSE + SMALL_TABLE + SELECT_LIMIT, LOGIN_NAME, LOGIN_PASSWORD, new StatementContext());) {
			provider.checkScheme(provider.getContentDescription().getQuery());
		}
	}

	@Ignore
	@Test
	public final void testCheckScheme() throws Exception {
		try (DataProvider provider = new DataProvider(HTTP_ATDS_URL, SELECT_ALL_CLAUSE + SMALL_TABLE + SELECT_LIMIT,
				LOGIN_NAME, LOGIN_PASSWORD, new StatementContext());) {
			final ContentDescription contentDescription = provider.getContentDescription();
			provider.checkScheme(contentDescription.getQuery());
		}
	}
	
	@Test
	public final void testGetContentDescription() throws Exception {
		try (DataProvider provider = new DataProvider(HTTP_ATDS_URL + UNTRUST_PARAMETER_IN_QUERY,
				SELECT_ALL_CLAUSE + SMALL_TABLE + SELECT_LIMIT, LOGIN_NAME, LOGIN_PASSWORD, new StatementContext());) {
			final ContentDescription contentDescription = provider.getContentDescription();
			assertNotNull(contentDescription);
			if (logger.isDebugEnabled()) {
				logger.debug(contentDescription.getHost());
				logger.debug(Arrays.toString(contentDescription.getParams()));
			}
		}
	}

	@Test
	public final void testClose() throws Exception {
		try (DataProvider provider = new DataProvider(HTTP_ATDS_URL, SELECT_ALL_CLAUSE + SMALL_TABLE + SELECT_LIMIT,
				LOGIN_NAME, LOGIN_PASSWORD, new StatementContext());) {
			if (logger.isDebugEnabled()) {
				logger.debug(provider.toString());
			}
		}
	}

}
