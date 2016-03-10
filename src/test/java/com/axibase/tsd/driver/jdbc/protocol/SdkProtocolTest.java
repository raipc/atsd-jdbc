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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.axibase.tsd.driver.jdbc.Constants;
import com.axibase.tsd.driver.jdbc.content.ContentDescription;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IContentProtocol;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

import au.com.bytecode.opencsv.CSVReader;

public class SdkProtocolTest implements Constants {
	private static final LoggingFacade logger = LoggingFacade.getLogger(SdkProtocolTest.class);

	protected static String HTTP_ATDS_URL;
	protected static String LOGIN_NAME;
	protected static String LOGIN_PASSWORD;
	protected static Boolean TRUST_URL;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		HTTP_ATDS_URL = System.getProperty("test.url");
		LOGIN_NAME = System.getProperty("test.username");
		LOGIN_PASSWORD = System.getProperty("test.password");
		String trustProp = System.getProperty("test.trust");
		TRUST_URL = trustProp != null ? Boolean.valueOf(trustProp) : null;
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public final void testCheckContentSecure() throws IOException, AtsdException, GeneralSecurityException {
		final ContentDescription cd = new ContentDescription(HTTP_ATDS_URL,
				SELECT_TVE_CLAUSE + SMALL_TABLE + SELECT_LIMIT, LOGIN_NAME, LOGIN_PASSWORD,
				TRUST_URL != null ? new String[] { ContentDescription.TRUST_PARAM_FALSE } : new String[0]);
		IContentProtocol impl = ProtocolFactory.create(SdkProtocolImpl.class, cd);
		impl.getContentSchema();
		assertNotNull(cd.getJsonScheme());
	}

	@Ignore
	@Test
	public final void testReadContentSecure() throws IOException, AtsdException, GeneralSecurityException {
		final ContentDescription cd = new ContentDescription(HTTP_ATDS_URL,
				SELECT_TVE_CLAUSE + SMALL_TABLE + SELECT_LIMIT, LOGIN_NAME, LOGIN_PASSWORD,
				TRUST_URL != null ? new String[] { ContentDescription.TRUST_PARAM_FALSE } : new String[0]);
		IContentProtocol impl = ProtocolFactory.create(SdkProtocolImpl.class, cd);
		try (final InputStream is = impl.readContent();) {
			final Reader reader = new BufferedReader(new InputStreamReader(is));
			assertNotNull(cd.getJsonScheme());
			String[] nextLine;
			try (final CSVReader csvReader = new CSVReader(reader);) {
				while ((nextLine = csvReader.readNext()) != null) {
					String next = Arrays.toString(nextLine);
					assertNotNull(next);
					if (logger.isTraceEnabled())
						logger.trace(next);
				}
			}
		}
	}

}