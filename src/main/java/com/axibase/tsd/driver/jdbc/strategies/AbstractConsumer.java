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


import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.intf.IConsumer;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

import java.io.IOException;
import java.util.Iterator;


public abstract class AbstractConsumer implements IConsumer {
	private static final LoggingFacade logger = LoggingFacade.getLogger(AbstractConsumer.class);

	protected final StrategyStatus status;
	protected final StatementContext context;
	protected AbstractIterator iterator;


	public AbstractConsumer(final StatementContext context, final StrategyStatus status) {
		this.context = context;
		this.status = status;
	}

	@Override
	public StatementContext getContext() {
		return context;
	}

	@Override
	public void close() throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("[close]");
		}
		if (iterator != null) {
			iterator.close();
		}
	}

	protected Iterator<String[]> getIterator(String source) throws AtsdException {
		if (iterator == null) {
			assert source != null;
			throw new AtsdException(source + " has not opened yet");
		}
		return iterator;
	}


}
