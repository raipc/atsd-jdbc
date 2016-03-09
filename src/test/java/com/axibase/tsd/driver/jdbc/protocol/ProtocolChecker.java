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
package com.axibase.tsd.driver.jdbc.protocol;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axibase.tsd.driver.jdbc.Constants;
import com.axibase.tsd.driver.jdbc.content.ContentDescription;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SdkProtocolImpl.class)
public class ProtocolChecker implements Constants {
	private static final LoggingFacade logger = LoggingFacade.getLogger(ProtocolChecker.class);
	protected static String JDBC_ATDS_URL;
	protected static String LOGIN_NAME;
	protected static String LOGIN_PASSWORD;
	protected static Boolean TRUST_URL;
	private SdkProtocolImpl impl;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JDBC_ATDS_URL = JDBC_ATDS_URL_PREFIX + System.getProperty("test.url");
		LOGIN_NAME = System.getProperty("test.username");
		LOGIN_PASSWORD = System.getProperty("test.password");
		String trustProp = System.getProperty("test.trust");
		TRUST_URL = trustProp != null ? Boolean.valueOf(trustProp) : null;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ContentDescription cd = new ContentDescription(JDBC_ATDS_URL, SELECT_TVE_CLAUSE + SMALL_TABLE, LOGIN_NAME,
				LOGIN_PASSWORD, TRUST_URL != null ? new String[] { TRUST_URL.booleanValue()
						? ContentDescription.TRUST_PARAM_TRUE : ContentDescription.TRUST_PARAM_FALSE } : new String[0]);
		this.impl = PowerMockito.spy(new SdkProtocolImpl(cd));
	}

	@After
	public void tearDown() throws Exception {
		this.impl.close();
	}

	@Test
	public final void testPost() throws IOException, AtsdException, GeneralSecurityException {
		InputStream is = this.impl.executeRequest(SdkProtocolImpl.POST_METHOD);
		assertNotNull(is);
		if (logger.isTraceEnabled()) {
			printContent(is);
		}
	}

	@Test
	public final void testGet() throws IOException, AtsdException, GeneralSecurityException {
		InputStream is = this.impl.executeRequest(SdkProtocolImpl.GET_METHOD);
		assertNotNull(is);
		if (logger.isTraceEnabled()) {
			printContent(is);
		}
	}

	@Test
	public final void testHead() throws IOException, AtsdException, GeneralSecurityException {
		InputStream is = this.impl.executeRequest(SdkProtocolImpl.HEAD_METHOD);
		assertNull(is);
	}

	private void printContent(final InputStream inputStream) throws IOException {
		BufferedReader is = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = is.readLine()) != null) {
			logger.trace(line);
		}
	}

}