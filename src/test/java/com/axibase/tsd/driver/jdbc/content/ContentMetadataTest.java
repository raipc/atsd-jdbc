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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.apache.calcite.avatica.ColumnMetaData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.axibase.tsd.driver.jdbc.ext.AtsdException;

public class ContentMetadataTest {
	private static final String CONTEXT_START = "{\n   \"@context\":[";
	private static final String MCN_JSON_SCHEMA = "/json/mpstat_cpu_busy.jsonld";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetMetadataList() {
		checkMetadataList(MCN_JSON_SCHEMA, 3);
	}

	private void checkMetadataList(String schema, int expected) {
		try (final InputStream is = this.getClass().getResourceAsStream(schema);
				final Scanner scanner = new Scanner(is);) {
			scanner.useDelimiter("\\A");
			String json = scanner.hasNext() ? scanner.next() : "";
			assertTrue(json != null && json.length() != 0 && json.startsWith(CONTEXT_START));
			final List<ColumnMetaData> metadataList = ContentMetadata.buildMetadataList(json);
			assertTrue(metadataList != null && metadataList.size() == expected);
			for (ColumnMetaData cmd : metadataList) {
				if (cmd.ordinal == 1)
					assertTrue("datetime".equals(cmd.columnName));
				else if (cmd.ordinal == 2)
					assertTrue("value".equals(cmd.columnName));
				else if (cmd.ordinal == 3)
					assertTrue("entity".equals(cmd.columnName));
			}
		} catch (final IOException | AtsdException e) {
			fail(e.getMessage());
		}
	}

}