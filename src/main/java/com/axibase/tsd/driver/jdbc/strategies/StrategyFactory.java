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

import org.apache.commons.lang3.StringUtils;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.strategies.storage.FileStoreStrategy;
import com.axibase.tsd.driver.jdbc.strategies.stream.KeepAliveStrategy;

public class StrategyFactory {
	private static final LoggingFacade logger = LoggingFacade.getLogger(StrategyFactory.class);
	private static final String STREAM_STRATEGY = "stream";
	private static final String FILE_STRATEGY = "file";

	public static <T extends IStoreStrategy> T create(Class<T> type, StatementContext context) {
		try {
			return type.getDeclaredConstructor(StatementContext.class).newInstance(context);
		} catch (final IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
			if (logger.isErrorEnabled())
				logger.error("Cannot get a store instance from the factory: " + e.getMessage());
		}
		return null;
	}

	public static Class<? extends IStoreStrategy> findClassByName(final String name) {
		if (StringUtils.isEmpty(name)) {
			return KeepAliveStrategy.class;
		}
		switch (name) {
		case FILE_STRATEGY:
			return FileStoreStrategy.class;
		case STREAM_STRATEGY:
		default:
			return KeepAliveStrategy.class;
		}
	}
}