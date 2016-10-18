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
package com.axibase.tsd.driver.jdbc.ext;

import java.sql.SQLException;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.AvaticaStatement;
import org.apache.calcite.avatica.Meta.Signature;
import org.apache.calcite.avatica.Meta.StatementHandle;

import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class AtsdStatement extends AvaticaStatement {
	private static final LoggingFacade logger = LoggingFacade.getLogger(AtsdStatement.class);

	protected AtsdStatement(AvaticaConnection connection, StatementHandle h, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability) {
		super(connection, h, resultSetType, resultSetConcurrency, resultSetHoldability);
		if (logger.isTraceEnabled())
			logger.trace("[AtsdStatement#new] " + this.handle.id);
	}

	protected AtsdStatement(AvaticaConnection connection, StatementHandle statementHandle, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability, Signature signature) {
		super(connection, statementHandle, resultSetType, resultSetConcurrency, resultSetHoldability, signature);
		if (logger.isTraceEnabled())
			logger.trace("[AtsdStatement#new] " + this.handle.id);
	}

	@Override
	public synchronized void cancel() throws SQLException {
		super.cancel();
		AtsdConnection atsdConnection = (AtsdConnection) this.connection;
		atsdConnection.getMeta().closeStatement(this.handle);
		if (logger.isTraceEnabled()) {
			logger.trace("[AtsdStatement#cancel]");
		}
	}

	@Override
	public synchronized void close() throws SQLException {
		super.close();
		if (logger.isTraceEnabled())
			logger.trace("[AtsdStatement#close] " + this.handle.id);
	}

}
