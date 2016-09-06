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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.axibase.tsd.driver.jdbc.enums.AtsdType;
import com.axibase.tsd.driver.jdbc.enums.timedatesyntax.EndTime;
import com.axibase.tsd.driver.jdbc.util.TimeDateExpression;
import org.apache.calcite.avatica.*;
import org.apache.calcite.avatica.remote.TypedValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.axibase.tsd.driver.jdbc.DriverConstants;
import com.axibase.tsd.driver.jdbc.content.ContentDescription;
import com.axibase.tsd.driver.jdbc.content.ContentMetadata;
import com.axibase.tsd.driver.jdbc.content.DataProvider;
import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.intf.IDataProvider;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class AtsdMeta extends MetaImpl {
	private static final LoggingFacade log = LoggingFacade.getLogger(AtsdMeta.class);
	private final AtomicInteger idGenerator = new AtomicInteger(1);
	private final Map<Integer, ContentMetadata> metaCache = new ConcurrentHashMap<>();
	private final Map<Integer, IDataProvider> providerCache = new ConcurrentHashMap<>();
	private final Map<Integer, StatementContext> contextMap = new ConcurrentHashMap<>();
	private final ReentrantLock lock = new ReentrantLock();

	public AtsdMeta(final AvaticaConnection conn) {
		super(conn);
		this.connProps.setAutoCommit(true);
		this.connProps.setReadOnly(true);
		this.connProps.setTransactionIsolation(Connection.TRANSACTION_NONE);
		this.connProps.setDirty(false);
	}

	public StatementContext getContextFromMap(StatementHandle h) {
		return contextMap.get(h.id);
	}

	@Override
	public StatementHandle prepare(ConnectionHandle connectionHandle, String query, long maxRowCount) {
		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e) {
			if (log.isDebugEnabled()) {
				log.debug("[prepare] " + e.getMessage());
			}
		}
		final int id = idGenerator.getAndIncrement();
		if (log.isTraceEnabled()) {
			log.trace("[prepare] locked: {} handle: {} query: {}", lock.getHoldCount(), id, query);
		}
		try {
			initProvider(id, query);
			Signature signature = new Signature(null, query, Collections.<AvaticaParameter>emptyList(), null,
					CursorFactory.LIST, StatementType.SELECT);
			return new StatementHandle(connectionHandle.id, id, signature);
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("[prepare]" + e.getMessage());
			}
		}
		return null;
	}

	@Override
	public ExecuteResult execute(StatementHandle statementHandle, List<TypedValue> parameterValues, long maxRowsCount)
			throws NoSuchStatementException {
		return execute(statementHandle, parameterValues, AvaticaUtils.toSaturatedInt(maxRowsCount));
	}

	@Override
	public ExecuteResult execute(StatementHandle statementHandle, List<TypedValue> parameterValues, int maxRowsInFirstFrame) throws NoSuchStatementException {
		if (log.isTraceEnabled()) {
			log.trace("[execute] maxRowsInFirstFrame: {} parameters: {} handle: {}", maxRowsInFirstFrame, parameterValues.size(),
					statementHandle.toString());
		}
		final String query = substitutePlaceholders(statementHandle.signature.sql, parameterValues);
		IDataProvider provider = null;
		try {
			provider = initProvider(statementHandle.id, query);
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("[execute]" + e.getMessage());
			}
		}
		assert provider != null;
		try {
			final Statement statement = connection.statementMap.get(statementHandle.id);
			final int maxRows = getMaxRows(statement);
			final int timeout = getQueryTimeout(statement);
			provider.fetchData(maxRows, timeout);
			final ContentMetadata contentMetadata = findMetadata(query, statementHandle.connectionId, statementHandle.id);
			return new ExecuteResult(contentMetadata.getList());
		} catch (final AtsdException | GeneralSecurityException | IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("[execute] " + e.getMessage());
			}
			throw new NoSuchStatementException(statementHandle);
		}
	}

	private static String substitutePlaceholders(String query, List<TypedValue> parameterValues) {
		if (query.contains("?")) {
			final StringBuilder buffer = new StringBuilder(query.length());
			final String[] parts = query.split("\\?", -1);
			if (parts.length != parameterValues.size() + 1) {
				throw new IndexOutOfBoundsException(
						String.format("Number of specified values [%d] does not match to number of occurences [%d]",
								parameterValues.size(), parts.length - 1));
			}
			buffer.append(parts[0]);
			int position = 0;
			for (TypedValue parameterValue : parameterValues) {
				++position;
				Object value = parameterValue.value;

				if (value instanceof Number || value instanceof TimeDateExpression || value instanceof EndTime) {
					buffer.append(value);
				} else if (value instanceof String) {
					buffer.append('\'').append((String) value).append('\'');
				} else if (value instanceof java.sql.Date) {
					buffer.append('\'').append(DATE_FORMATTER.get().format((java.sql.Date) value)).append('\'');
				} else if (value instanceof Time) {
					buffer.append('\'').append(TIME_FORMATTER.get().format((Time) value)).append('\'');
				} else if (value instanceof Timestamp) {
					buffer.append('\'').append(TIMESTAMP_FORMATTER.get().format((Timestamp) value)).append('\'');
				}

				buffer.append(parts[position]);
			}

			final String result = buffer.toString();
			if (log.isDebugEnabled()) {
				log.debug("[substitutePlaceholders] " + result);
			}
			return result;
		}
		return query;
	}

	private int getMaxRows(Statement statement) {
		int maxRows = 0;
		if (statement != null) {
			try {
				maxRows = statement.getMaxRows();
			} catch (SQLException e) {
				maxRows = 0;
			}
		}
		return maxRows;
	}

	private int getQueryTimeout(Statement statement) {
		int timeout = 0;
		if (statement != null) {
			try {
				timeout = statement.getQueryTimeout();
			} catch (SQLException e) {
				timeout = 0;
			}
		}
		return timeout;
	}

	@Override
	public ExecuteResult prepareAndExecute(StatementHandle statementHandle, String query, long maxRowCount,
										   PrepareCallback callback) throws NoSuchStatementException {
		return prepareAndExecute(statementHandle, query, maxRowCount, 0, callback);
	}

	@Override
	public ExecuteResult prepareAndExecute(StatementHandle statementHandle, String query, long maxRowCount,
										   int maxRowsInFrame, PrepareCallback callback) throws NoSuchStatementException {
		long limit = maxRowCount < 0 ? 0 : maxRowCount;
		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e) {
			if (log.isDebugEnabled()) {
				log.debug("[prepareAndExecute] " + e.getMessage());
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("[prepareAndExecute] locked: {} maxRowCount: {} handle: {} query: {}", lock.getHoldCount(),
					limit, statementHandle.toString(), query);
		}
		try {
			final IDataProvider provider = initProvider(statementHandle.id, query);
			final Statement statement = (Statement) callback.getMonitor();
			provider.fetchData(limit, statement.getQueryTimeout());
			final ContentMetadata contentMetadata = findMetadata(query, statementHandle.connectionId, statementHandle.id);
			synchronized (callback.getMonitor()) {
				// callback.clear();
				callback.assign(contentMetadata.getSign(), null, -1);
			}
			final ExecuteResult result = new ExecuteResult(contentMetadata.getList());
			callback.execute();
			return result;
		} catch (final AtsdException | IOException | SQLException | GeneralSecurityException e) {
			if (log.isDebugEnabled()) {
				log.debug("[prepareAndExecute] " + e.getMessage());
			}
			throw new NoSuchStatementException(statementHandle);
		}
	}

	@Override
	public ExecuteBatchResult prepareAndExecuteBatch(StatementHandle statementHandle, List<String> list) throws NoSuchStatementException {
		throw new UnsupportedOperationException("Batch not yet implemented");
	}

	@Override
	public ExecuteBatchResult executeBatch(StatementHandle statementHandle, List<List<TypedValue>> list) throws NoSuchStatementException {
		throw new UnsupportedOperationException("Batch not yet implemented");
	}

	@Override
	public Frame fetch(final StatementHandle statementHandle, long loffset, int fetchMaxRowCount)
			throws NoSuchStatementException, MissingResultsException {
		final int offset = (int) loffset;
		if (log.isTraceEnabled()) {
			log.trace("[fetch] fetchMaxRowCount: {} offset: {}", fetchMaxRowCount, offset);
		}
		IDataProvider provider = providerCache.get(statementHandle.id);
		assert provider != null;
		final ContentDescription cd = provider.getContentDescription();
		final IStoreStrategy strategy = provider.getStrategy();
		try {
			if (offset == 0) {
				final String[] headers = strategy.openToRead();
				if (headers == null || headers.length == 0) {
					throw new MissingResultsException(statementHandle);
				}
				cd.setHeaders(headers);
			}
			final List<String[]> subList = strategy.fetch(offset, fetchMaxRowCount);
			final List<Object> rows = getFrame(statementHandle, fetchMaxRowCount, subList);
			return new Meta.Frame(loffset, rows.size() < fetchMaxRowCount, rows);
		} catch (final AtsdException | IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("[fetch] " + e.getMessage());
			}
			throw new MissingResultsException(statementHandle);
		}

	}

	@Override
	public void closeStatement(StatementHandle statementHandle) {
		if (log.isDebugEnabled()) {
			log.debug("[closeStatement] " + statementHandle.id + "->" + statementHandle.toString());
		}
		closeProviderCaches(statementHandle);
		closeProvider(statementHandle);
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
			if (log.isTraceEnabled()) {
				log.trace("[unlocked]");
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("[closedStatement]");
		}
	}

	private void closeProviderCaches(StatementHandle h) {
		if (!metaCache.isEmpty()) {
			metaCache.remove(h.id);
		}
		if (!contextMap.isEmpty()) {
			contextMap.remove(h.id);
		}

	}

	private void closeProvider(StatementHandle statementHandle) {
		if (!providerCache.isEmpty()) {
			final IDataProvider provider = providerCache.remove(statementHandle.id);
			if (provider != null) {
				try {
					provider.close();
				} catch (final Exception e) {
					if (log.isDebugEnabled()) {
						log.debug("[closeStatement] " + e.getMessage());
					}
				}
			}
		}
	}

	public void close() {
		closeCaches();
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
			if (log.isTraceEnabled()) {
				log.trace("[unlocked]");
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("[closed]");
		}
	}

	private void closeCaches() {
		if (!metaCache.isEmpty()) {
			metaCache.clear();
		}
		if (!contextMap.isEmpty()) {
			contextMap.clear();
		}
		if (!providerCache.isEmpty()) {
			providerCache.clear();
		}
	}

	@Override
	public boolean syncResults(StatementHandle sh, QueryState state, long offset) throws NoSuchStatementException {
		if (log.isDebugEnabled()) {
			log.debug("[syncResults] " + offset);
		}
		return false;
	}

	@Override
	public Map<DatabaseProperty, Object> getDatabaseProperties(ConnectionHandle connectionHandle) {
		return super.getDatabaseProperties(connectionHandle);
	}

	@Override
	public MetaResultSet getTables(ConnectionHandle connectionHandle, String catalog, Pat schemaPattern, Pat tableNamePattern,
								   List<String> typeList) {
		return super.getTables(connectionHandle, catalog, schemaPattern, tableNamePattern, typeList);
	}

	@Override
	public MetaResultSet getSchemas(ConnectionHandle connectionHandle, String catalog, Pat schemaPattern) {
		assert connection instanceof AtsdConnection;
		final Properties info = ((AtsdConnection) connection).getInfo();
		String username = info != null ? (String) info.get("user") : "";
		final Iterable<Object> iterable = new ArrayList<Object>(
				Arrays.asList(new MetaSchema(DriverConstants.CATALOG_NAME, WordUtils.capitalize(username))));
		return getResultSet(iterable, MetaSchema.class, "TABLE_SCHEM", "TABLE_CATALOG");
	}

	@Override
	public MetaResultSet getCatalogs(ConnectionHandle ch) {
		final Iterable<Object> iterable = new ArrayList<Object>(
				Arrays.asList(new MetaCatalog(DriverConstants.CATALOG_NAME)));
		return getResultSet(iterable, MetaCatalog.class, "TABLE_CAT");
	}

	@Override
	public MetaResultSet getTableTypes(ConnectionHandle ch) {
		final Iterable<Object> iterable = new ArrayList<Object>(
				Arrays.asList(new MetaTableType("TABLE"), new MetaTableType("VIEW"), new MetaTableType("SYSTEM")));
		return getResultSet(iterable, MetaTableType.class, "TABLE_TYPE");
	}

	@Override
	public MetaResultSet getTypeInfo(ConnectionHandle ch) {
		AtsdType[] atsdTypes = AtsdType.values();
		final List<Object> list = new ArrayList<>(atsdTypes.length - 2);
		for (AtsdType type : atsdTypes) {
			if (!(type == AtsdType.LONG_DATA_TYPE || type == AtsdType.SHORT_DATA_TYPE)) {
				list.add(getTypeInfo(type.sqlType.toUpperCase(Locale.US), type.sqlTypeCode,
						type == AtsdType.STRING_DATA_TYPE, type.maxPrecision));
			}
		}
		return getResultSet(list, MetaTypeInfo.class, "TYPE_NAME", "DATA_TYPE", "PRECISION",
				"LITERAL_PREFIX", "LITERAL_SUFFIX", "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE",
				"UNSIGNED_ATTRIBUTE", "FIXED_PREC_SCALE", "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE",
				"MAXIMUM_SCALE", "NUM_PREC_RADIX");
	}

	private MetaResultSet getResultSet(Iterable<Object> iterable, Class<?> clazz, String... names) {
		final int length = names.length;
		final List<ColumnMetaData> columns = new ArrayList<>(length);
		final List<Field> fields = new ArrayList<>(length);
		final List<String> fieldNames = new ArrayList<>(length);
		for (String name : names) {
			final int index = fields.size();
			final String fieldName = AvaticaUtils.toCamelCase(name);
			final Field field;
			try {
				field = clazz.getField(fieldName);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
			columns.add(columnMetaData(name, index, field.getType()));
			fields.add(field);
			fieldNames.add(name);
		}
		return createResultSet(Collections.<String, Object>emptyMap(), columns,
				CursorFactory.record(clazz, fields, fieldNames), new Frame(0, true, iterable));
	}

	private IDataProvider initProvider(Integer id, String sql) throws UnsupportedEncodingException {
		final ConnectionConfig config = connection.config();
		assert config != null;
		assert connection instanceof AtsdConnection;
		final Properties info = ((AtsdConnection) connection).getInfo();
		final StatementContext newContext = new StatementContext();
		contextMap.put(id, newContext);
		final String login = info != null ? (String) info.get("user") : "";
		final String password = info != null ? (String) info.get("password") : "";
		final IDataProvider dataProvider = new DataProvider(config.url(), sql, login, password, newContext);
		providerCache.put(id, dataProvider);
		return dataProvider;
	}

	private ContentMetadata findMetadata(String sql, String connectionId, int statementId)
			throws AtsdException, IOException {
		ContentMetadata contentMetadata = metaCache.get(statementId);
		if (contentMetadata == null) {
			IDataProvider provider = providerCache.get(statementId);
			final String jsonScheme = provider != null ? provider.getContentDescription().getJsonScheme() : "";
			contentMetadata = new ContentMetadata(jsonScheme, sql, connectionId, statementId);
			metaCache.put(statementId, contentMetadata);
		}
		return contentMetadata;
	}

	private List<Object> getFrame(final StatementHandle handle, int fetchMaxRowCount, final List<String[]> subList) {
		IDataProvider provider = providerCache.get(handle.id);
		assert provider != null;
		final String[] headers = provider.getContentDescription().getHeaders();
		final ContentMetadata contentMetadata = metaCache.get(handle.id);
		final List<ColumnMetaData> metadataList = contentMetadata.getMetadataList();
		final List<Object> rows = new ArrayList<>(subList.size());
		for (final String[] stringArray : subList) {
			if (stringArray == null || headers == null || rows.size() == fetchMaxRowCount) {
				break;
			}
			if (stringArray.length == headers.length) {
				rows.add(getFrameRow(metadataList, stringArray));
			} else {
				if (log.isDebugEnabled()) {
					log.debug("[getFrame] array length discrepancy: " + Arrays.toString(stringArray));
				}
			}
		}
		return rows;
	}

	private List<Object> getFrameRow(final List<ColumnMetaData> metadataOrdered, final String[] strings) {
		final int length = strings.length;
		final List<Object> row = new ArrayList<>(length);
		int sqlType;
		String cell;
		for (int i = 0; i < length; i++) {
			sqlType = metadataOrdered.get(i).type.id;
			cell = strings[i];

			if (StringUtils.isEmpty(cell)) {
				row.add(sqlType == Types.VARCHAR ? cell : null);
			} else if (sqlType == Types.VARCHAR) {
				row.add(cell);
			} else if (sqlType == Types.TIMESTAMP) {
				row.add(readTimestampValue(cell));
			} else {
				row.add(readNumberValue(strings, i, sqlType));
			}
		}
		return row;
	}

	private static Object readTimestampValue(String cell) {
		Object value;
		try {
			Date date = TIMESTAMP_FORMATTER.get().parse(cell);
			value = new Timestamp(date.getTime());
		} catch (final ParseException e) {
			if (log.isDebugEnabled()) {
				log.debug("[getFrame] " + e.getMessage());
			}
			value = readShortTimestampValue(cell);
		}
		return value;
	}

	private static Object readShortTimestampValue(String cell) {
		Object value = null;
		try {
			final Date date = TIMESTAMP_SHORT_FORMATTER.get().parse(cell);
			value = new Timestamp(date.getTime());
		} catch (ParseException parseException) {
			if (log.isDebugEnabled()) {
				log.debug("[getFrame] " + parseException.getMessage());
			}
		}
		return value;
	}

	private static Object readNumberValue(String[] array, int index, int type) {
		Object value = null;
		String typeName = null;
		try {
			switch(type) {
				case Types.SMALLINT:
					typeName = "smallint";
					value = Short.valueOf(array[index]);
					break;
				case Types.INTEGER:
					typeName = "integer";
					value = Integer.valueOf(array[index]);
					break;
				case Types.BIGINT:
					typeName = "bigint";
					value = Long.valueOf(array[index]);
					break;
				case Types.FLOAT:
					typeName = "float";
					value = Double.valueOf(array[index]);
					break;
				case Types.DOUBLE:
					typeName = "double";
					value = Double.valueOf(array[index]);
					break;
				case Types.DECIMAL:
					typeName = "decimal";
					value = new BigDecimal(array[index]);
					break;
				default:
					throw new IllegalArgumentException("Numeric type expected");
			}
		} catch (final NumberFormatException e) {
			if (log.isDebugEnabled()) {
				log.debug("[getFrame] {} type mismatched: {} on {} position", typeName, Arrays.toString(array), index);
			}
		}
		return value;
	}

	private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
			sdt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdt;
		}
	};

	private static final ThreadLocal<SimpleDateFormat> TIME_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss", Locale.UK);
			sdt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdt;
		}
	};

	private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
			sdt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdt;
		}
	};

	private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_SHORT_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
			sdt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdt;
		}
	};

	private static MetaTypeInfo getTypeInfo(String name, int type, boolean isCaseSensitive, int maxPrecision) {
		return new MetaTypeInfo(name, type, maxPrecision, getLiteral(type, true), getLiteral(type, false),
				DatabaseMetaData.typeNullable, isCaseSensitive, DatabaseMetaData.typeSearchable, false, false, false, 0,
				0, 10);
	}

	private static String getLiteral(int type, boolean isPrefix) {
		switch (type) {
			case Types.VARCHAR:
				return "'";
			case Types.TIMESTAMP:
				return isPrefix ? "TIMESTAMP '" : "'";
			default:
				return null;
		}
	}

	// Since Calcite 1.6.0

	@Override
	public void commit(ConnectionHandle ch) {
		if (log.isDebugEnabled()) {
			log.debug("[commit] " + ch.id + "->" + ch.toString());
		}
	}

	@Override
	public void rollback(ConnectionHandle ch) {
		if (log.isDebugEnabled()) {
			log.debug("[rollback] " + ch.id + "->" + ch.toString());
		}
	}

}
