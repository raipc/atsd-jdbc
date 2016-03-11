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
package com.axibase.tsd.driver.jdbc.strategies;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axibase.tsd.driver.jdbc.content.StatementContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IteratorData.class)
public class EscapedCharactersTest {
	private static final Logger logger = LoggerFactory.getLogger(EscapedCharactersTest.class);
	private IteratorData data;
	private StatementContext context;

	@Before
	public void setUp() throws Exception {
		context = new StatementContext();
		data = PowerMockito.spy(new IteratorData(context));
		data.getBuffer().put(CONTENT_PART.getBytes());
		data.bufferOperations();
	}

	@After
	public void tearDown() throws Exception {
		data = null;
		context = null;
	}

	@Test
	public void testGetNext() {
		String[] arr = data.getNext(false);
		assertArrayEquals(arr, new String[]{"entity", "time", "value", "tags.collector-host"});
		while ((arr = data.getNext(true)) != null) {
			if (logger.isDebugEnabled())
				logger.debug(Arrays.toString(arr));
			assertTrue(arr.length == 4);
		}
		assertTrue(data.getSb().toString().length() == 0);
	}

	private static final String CONTENT_PART = "entity,time,value,tags.collector-host\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456424003535,15620.0,Mikhail's-Air\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456424188535,15620.0,Mikhail's-Air\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456424827535,15620.0,Mikhail's-Air\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456466167759,15620.0,Mikhail's-Air\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456466216759,15620.0,Mikhail's-Air\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456467148826,15620.0,Mikhail's-Air\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456731652280,1052.0,\"Mikhail's-Macbook-Air,\"local\"\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456821593554,1052.0,Mikhail's-Air\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456823266554,1052.0,Mikhail's-Air\r\n"
			+ "ac2dff755072c526a63208706a459827ae18c09122077564ac3ccdbc79066323,1456823411602,1052.0,Mikhail's-Air\r\n";
}