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

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.TimeZone;

import org.apache.calcite.avatica.AvaticaResultSet;
import org.apache.calcite.avatica.AvaticaStatement;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.Meta.Frame;
import org.apache.calcite.avatica.Meta.Signature;
import org.apache.calcite.avatica.QueryState;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class AtdsResultSet extends AvaticaResultSet {
	private static final LoggingFacade logger = LoggingFacade.getLogger(AtdsResultSet.class);
	private final AtsdMeta meta;
	private final Meta.StatementHandle handle;
	private final StatementContext context;

	public AtdsResultSet(AvaticaStatement statement, QueryState state, Signature signature,
			ResultSetMetaData resultSetMetaData, TimeZone timeZone, Frame firstFrame) {
		super(statement, state, signature, resultSetMetaData, timeZone, firstFrame);
		final AtsdConnection connection = (AtsdConnection) statement.connection;
		this.meta = (AtsdMeta) connection.getMeta();
		this.handle = statement.handle;
		this.context = meta.getContextFromMap(statement.handle);
		if (logger.isTraceEnabled())
			logger.trace("[ctor] " + this.handle.id);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return context.getWarning();
	}

	@Override
	public void clearWarnings() throws SQLException {
		context.setWarning(null);
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		return super.absolute(row);
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		return super.relative(rows);
	}

	@Override
	public boolean previous() throws SQLException {
		return super.previous();
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return super.getBigDecimal(columnIndex, 0);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return super.getBigDecimal(columnLabel, 0);
	}

	@Override
	public boolean next() throws SQLException {
		final boolean next = super.next();
		if (!next)
			meta.closeStatement(handle);
		if (!next && context != null && context.getException() != null) {
			throw context.getException();
		}
		return next;
	}

	@Override
	public void close() {
		super.close();
		context.setWarning(null);
		context.setException(null);
		if (logger.isTraceEnabled())
			logger.trace("[closed]");

	}

}
