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

import java.io.IOException;
import java.sql.SQLException;

import com.axibase.tsd.driver.jdbc.ext.AtsdRuntimeException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IteratorData.class)
public class IteratorDataTest {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(IteratorDataTest.class);
	private static final String CONTENT_PART = "entity,time,value\r\n060190011,1392048000000,1.0\r\n"
			+ "060190011,1392055200000,2.0\r\n060190011,1392058800000,3.0\r\n060190011,1392062400000,4.0\r\n"
			+ "060190011,1392066000000,5.0\r\n060190011,1392069600000,6.0\r\n060190011,1392073200000,7.0\r\n"
			+ "060190011,1392076800000,8.0\r\n060190011,1392080400000,9.0\r\n060190011,1392084000000,10.0\r\n"
			+ "060190011,1392087600000,11.0\r\n060190011,1392091200000,12.0\r\n"
			+ "060190011,1392094800000,13.0\r\n060190011,1392098400000,14.0\r\n"
			+ "060190011,1392102000000,15.0\r\n060190011,1392105600000,16.0\r\n"
			+ "060190011,1392109200000,17.0\r\n060190011,1392112800000,18.0\r\n"
			+ "060190011,1392116400000,19.0\r\n060190011,1392123600000,20.0\r\n"
			+ "060190011,1392127200000,21.0\r\n060190011,1392130800000,22.0\r\n"
			+ "060190011,1392134400000,23.0\r\n060190011,1392138000000,24.0\r\n"
			+ "060190011,1392141600000,25.0\r\n060190011,1392145200000,26.0\r\n"
			+ "060190011,1392148800000,27.0\r\n060190011,1392152400000,28.0\r\n"
			+ "060190011,1392156000000,29.0\r\n060190011,1392159600000,30.0\r\n"
			+ "060190011,1392163200000,31.0\r\n060190011,1392166800000,32.0\r\n"
			+ "060190011,1392170400000,33.0\r\n060190011,1392174000000,34.0\r\n"
			+ "060190011,1392177600000,35.0\r\n060190011,1392181200000,36.0\r\n"
			+ "060190011,1392184800000,37.0\r\n060190011,1392188400000,38.0\r\n"
			+ "060190011,1392192000000,39.0\r\n060190011,1392195600000,40.0\r\n"
			+ "060190011,1392199200000,41.0\r\n060190011,1392202800000,42.0\r\n"
			+ "060190011,1392210000000,43.0\r\n060190011,1392213600000,44.0\r\n"
			+ "060190011,1392217200000,45.0\r\n060190011,1392220800000,46.0\r\n"
			+ "060190011,1392224400000,47.0\r\n060190011,1392228000000,48.0\r\n"
			+ "060190011,1392231600000,49.0\r\n060190011,1392235200000,50.0\r\n";

	private static final String EXCEPTION_IN_COMMENT = "#{\r\n#  \"errors\" : [ {\r\n"
			+ "#    \"state\" : \"22\",\r\n#    \"exception\" : [ {\r\n"
			+ "#      \"methodName\" : \"getNameById\",\r\n#      \"fileName\" : \"SqlMetaRegistry.java\",\r\n"
			+ "#      \"lineNumber\" : 101,\r\n"
			+ "#      \"className\" : \"com.axibase.tsd.service.sql.SqlMetaRegistry\",\r\n"
			+ "#      \"nativeMethod\" : false\r\n#    }, {\r\n#      \"methodName\" : \"invoke0\",\r\n"
			+ "#      \"fileName\" : \"NativeMethodAccessorImpl.java\",\r\n#      \"lineNumber\" : -2,\r\n"
			+ "#      \"className\" : \"sun.reflect.NativeMethodAccessorImpl\",\r\n"
			+ "#      \"nativeMethod\" : true\r\n#    }, {\r\n#      \"methodName\" : \"scan\",\r\n"
			+ "#      \"fileName\" : null,\r\n#      \"lineNumber\" : -1,\r\n"
			+ "#      \"className\" : \"com.sun.proxy.$Proxy51\",\r\n#      \"nativeMethod\" : false\r\n"
			+ "#    }, {\r\n#      \"methodName\" : \"runJob\",\r\n"
			+ "#      \"fileName\" : \"QueuedThreadPool.java\",\r\n#      \"lineNumber\" : 608,\r\n"
			+ "#      \"className\" : \"org.eclipse.jetty.util.thread.QueuedThreadPool\",\r\n"
			+ "#      \"nativeMethod\" : false\r\n#    }, {\r\n#      \"methodName\" : \"run\",\r\n"
			+ "#      \"fileName\" : \"QueuedThreadPool.java\",\r\n#      \"lineNumber\" : 543,\r\n"
			+ "#      \"className\" : \"org.eclipse.jetty.util.thread.QueuedThreadPool$3\",\r\n"
			+ "#      \"nativeMethod\" : false\r\n#    }, {\r\n#      \"methodName\" : \"run\",\r\n"
			+ "#      \"fileName\" : \"Thread.java\",\r\n#      \"lineNumber\" : 745,\r\n"
			+ "#      \"className\" : \"java.lang.Thread\",\r\n#      \"nativeMethod\" : false\r\n"
			+ "#    } ],\r\n#    \"message\" : \"Name not found for id=10358022, type=TAG_VALUE\"\r\n" + "#  } ]\r\n#}";
	private IteratorData data;
	private StatementContext context;

	@Rule
	public ExpectedException thrown= ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		context = new StatementContext();
		data = PowerMockito.spy(new IteratorData(context));
		data.getBuffer().put(CONTENT_PART.getBytes()).put(EXCEPTION_IN_COMMENT.getBytes());
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
		assertArrayEquals(arr, new String[] { "entity", "time", "value" });
		while ((arr = data.getNext(true)) != null) {
			assertTrue(arr.length == 3);
		}
		assertTrue(data.getSb().toString().length() == 0);
	}

	@Test
	public void testProcessComments() throws JsonParseException, JsonMappingException, IOException, SQLException {
		thrown.expect(AtsdRuntimeException.class);
		thrown.expectMessage("Name not found for id=10358022, type=TAG_VALUE");
		thrown.expectCause(IsInstanceOf.<Throwable>instanceOf(SQLException.class));
		data.processComments();
	}

}