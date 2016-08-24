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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IContentProtocol;
import com.axibase.tsd.driver.jdbc.intf.IDataProvider;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.protocol.ProtocolFactory;
import com.axibase.tsd.driver.jdbc.protocol.SdkProtocolImpl;
import com.axibase.tsd.driver.jdbc.strategies.StrategyFactory;

public class DataProvider implements IDataProvider {
	private static final LoggingFacade logger = LoggingFacade.getLogger(DataProvider.class);
	private static final String WHERE_CLAUSE = " WHERE ";
	private static final String LIMIT_CLAUSE = " LIMIT ";
	private static final String PARAM_SEPARATOR = ";";
	private final ContentDescription contentDescription;
	private final IContentProtocol contentProtocol;
	private final StatementContext context;
	private IStoreStrategy strategy;

	public DataProvider(String url, String q, String login, String password, StatementContext context) {
		final String[] parts = url.split(PARAM_SEPARATOR);
		String[] params = new String[parts.length - 1];
		if (parts.length > 1) {
			System.arraycopy(parts, 1, params, 0, parts.length - 1);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Host: " + parts[0]);
			logger.trace("Params: " + params.length);
		}
		this.contentDescription = new ContentDescription(parts[0], q, login, password, params);
		this.contentProtocol = ProtocolFactory.create(SdkProtocolImpl.class, contentDescription);
		this.context = context;
		this.strategy = defineStrategy();
	}

	@Override
	public ContentDescription getContentDescription() {
		return this.contentDescription;
	}

	@Override
	public IStoreStrategy getStrategy() {
		return this.strategy;
	}

	@Override
	public void fetchData(long maxLimit, int timeout) throws AtsdException, GeneralSecurityException, IOException {
		contentDescription.setMaxRowsCount(maxLimit);
		final InputStream is = contentProtocol.readContent(timeout);
		this.strategy = defineStrategy();
		if (this.strategy != null) {
			this.strategy.store(is);
		}
	}

	@Override
	public void close() throws Exception {
		if (this.strategy != null) {
			this.strategy.close();
		}
	}

	private IStoreStrategy defineStrategy() {
		return StrategyFactory.create(StrategyFactory.findClassByName(this.contentDescription.getStrategyName()), this.context);
	}

}
