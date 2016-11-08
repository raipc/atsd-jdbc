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

import com.axibase.tsd.driver.jdbc.ext.AtsdRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.protocol.SdkProtocolImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DataProvider.class)
public class DataProviderExtTest extends AbstractFetchTest {
	@Before
	public void setUp() throws Exception {
		protocolImpl = PowerMockito.mock(SdkProtocolImpl.class);
	}

	@Test
	public void testStrategyOnOne() throws Exception {
		IStoreStrategy storeStrategy = null;
		try {
			storeStrategy = getMockStrategyObject();
			fetch(storeStrategy, "/csv/1.csv", 1);
		} finally {
			if (storeStrategy != null) {
				storeStrategy.close();
			}
		}
	}

	@Test
	public void testStrategyStrategyOn143() throws Exception {
		IStoreStrategy storeStrategy = null;
		try {
			storeStrategy = getMockStrategyObject();
			fetch(storeStrategy, "/csv/143.csv", 143);
		} finally {
			if (storeStrategy != null) {
				storeStrategy.close();
			}
		}
	}

	@Test
	public void testStrategyOn20001() throws Exception {
		IStoreStrategy storeStrategy = null;
		try {
			storeStrategy = getMockStrategyObject();
			fetch(storeStrategy, "/csv/20001.csv", 20001);
		} finally {
			if (storeStrategy != null) {
				storeStrategy.close();
			}
		}
	}

	@Test
	public void testStrategyOnTags() throws Exception {
		IStoreStrategy storeStrategy = null;
		try {
			storeStrategy = getMockStrategyObject();
			fetch(storeStrategy, "/csv/tags.csv", 6);
		} finally {
			if (storeStrategy != null) {
				storeStrategy.close();
			}
		}
	}

	@Test(expected = AtsdRuntimeException.class)
	public void testStrategyOnSqleWithoutRecords() throws Exception {
		IStoreStrategy storeStrategy = null;
		try {
			storeStrategy = getMockStrategyObject();
			fetch(storeStrategy, "/csv/docker.network.eth0.rxerrors.csv", 1);
		} finally {
			if (storeStrategy != null) {
				storeStrategy.close();
			}
		}
	}

	@Test(expected = AtsdRuntimeException.class)
	public void testStrategyOnSqleWithRecords() throws Exception {
		IStoreStrategy storeStrategy = null;
		try {
			storeStrategy = getMockStrategyObject();
			fetch(storeStrategy, "/csv/gc_time_persent.csv.zip", 323115);
		} finally {
			if (storeStrategy != null) {
				storeStrategy.close();
			}
		}
	}

	@Test(expected = AtsdRuntimeException.class)
	public void testStrategyOnSqleWithManyRecords() throws Exception {
		IStoreStrategy storeStrategy = null;
		try {
			storeStrategy = getMockStrategyObject();
			fetch(storeStrategy, "/csv/gc_time_persent.csv.zip", 323115);
		} finally {
			if (storeStrategy != null) {
				storeStrategy.close();
			}
		}
	}
}
