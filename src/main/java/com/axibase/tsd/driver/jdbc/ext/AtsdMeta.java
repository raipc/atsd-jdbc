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
import java.security.GeneralSecurityException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.axibase.tsd.driver.jdbc.DriverConstants;
import com.axibase.tsd.driver.jdbc.content.ContentDescription;
import com.axibase.tsd.driver.jdbc.content.ContentMetadata;
import com.axibase.tsd.driver.jdbc.content.DataProvider;
import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.enums.AtsdType;
import com.axibase.tsd.driver.jdbc.enums.DefaultColumn;
import com.axibase.tsd.driver.jdbc.enums.timedatesyntax.EndTime;
import com.axibase.tsd.driver.jdbc.intf.IDataProvider;
import com.axibase.tsd.driver.jdbc.intf.IStoreStrategy;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.util.EnumUtil;
import com.axibase.tsd.driver.jdbc.util.TimeDateExpression;
import org.apache.calcite.avatica.*;
import org.apache.calcite.avatica.remote.TypedValue;

public class AtsdMeta extends MetaImpl {
	private static final LoggingFacade log = LoggingFacade.getLogger(AtsdMeta.class);
	private final AtomicInteger idGenerator = new AtomicInteger(1);
	private final Map<Integer, ContentMetadata> metaCache = new ConcurrentHashMap<>();
	private final Map<Integer, IDataProvider> providerCache = new ConcurrentHashMap<>();
	private final Map<Integer, StatementContext> contextMap = new ConcurrentHashMap<>();
	private final ReentrantLock lock = new ReentrantLock();
	private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
			sdt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdt;
		}
	};

	public static final ThreadLocal<SimpleDateFormat> TIME_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss", Locale.UK);
			sdt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdt;
		}
	};

	public static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
			sdt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdt;
		}
	};

	public static final ThreadLocal<SimpleDateFormat> TIMESTAMP_SHORT_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
			sdt.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdt;
		}
	};

	private final String schema;

	public AtsdMeta(final AvaticaConnection conn) {
		super(conn);
		this.connProps.setAutoCommit(true);
		this.connProps.setReadOnly(true);
		this.connProps.setTransactionIsolation(Connection.TRANSACTION_NONE);
		this.connProps.setDirty(false);
		this.schema = null;
	}

	public StatementContext getContextFromMap(StatementHandle statementHandle) {
		return contextMap.get(statementHandle.id);
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
		Signature signature = new Signature(null, query, Collections.<AvaticaParameter>emptyList(), null,
					CursorFactory.LIST, StatementType.SELECT);
		return new StatementHandle(connectionHandle.id, id, signature);
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
			provider = initProvider(statementHandle, query);
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
			final IDataProvider provider = initProvider(statementHandle, query);
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
		} catch (final Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("[prepareAndExecute] " + e.getMessage());
			}
			throw new AtsdRuntimeException(e);
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
		final ContentDescription contentDescription = provider.getContentDescription();
		final IStoreStrategy strategy = provider.getStrategy();
		try {
			if (offset == 0) {
				final String[] headers = strategy.openToRead();
				if (headers == null || headers.length == 0) {
					throw new MissingResultsException(statementHandle);
				}
				contentDescription.setHeaders(headers);
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

	public void cancelStatement(StatementHandle statementHandle) {
		final IDataProvider provider = providerCache.get(statementHandle.id);
		if (provider != null) {
			provider.cancelQuery();
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

	private void closeProviderCaches(StatementHandle statementHandle) {
		if (!metaCache.isEmpty()) {
			metaCache.remove(statementHandle.id);
		}
		if (!contextMap.isEmpty()) {
			contextMap.remove(statementHandle.id);
		}
		if (log.isTraceEnabled()) {
			log.trace("[closeProviderCaches]");
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
						log.debug("[closeProvider] " + e.getMessage());
					}
				}
			}
		}
	}

	public void closeConnection() {
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
		if (typeList == null || typeList.contains("TABLE")) {
			final Iterable<Object> iterable = Collections.<Object>singletonList(
					new MetaTable(DriverConstants.DEFAULT_CATALOG_NAME, this.schema, DriverConstants.DEFAULT_TABLE_NAME, "TABLE"));
			return getResultSet(iterable, MetaTable.class);
		}
		return createEmptyResultSet(MetaTable.class);

	}

	@Override
	public MetaResultSet getSchemas(ConnectionHandle connectionHandle, String catalog, Pat schemaPattern) {
		return createEmptyResultSet(MetaSchema.class);
	}

	@Override
	public MetaResultSet getCatalogs(ConnectionHandle ch) {
		final Iterable<Object> iterable = Collections.<Object>singletonList(
				new MetaCatalog(DriverConstants.DEFAULT_CATALOG_NAME));
		return getResultSet(iterable, MetaCatalog.class);
	}

	@Override
	public MetaResultSet getTableTypes(ConnectionHandle ch) {
		final Iterable<Object> iterable = Arrays.<Object>asList(
				new MetaTableType("TABLE"), new MetaTableType("VIEW"), new MetaTableType("SYSTEM"));
		return getResultSet(iterable, MetaTableType.class);
	}

	@Override
	public MetaResultSet getTypeInfo(ConnectionHandle ch) {
		AtsdType[] atsdTypes = AtsdType.values();
		final List<Object> list = new ArrayList<>(atsdTypes.length - 2);
		for (AtsdType type : atsdTypes) {
			if (!(type == AtsdType.LONG_DATA_TYPE || type == AtsdType.SHORT_DATA_TYPE)) {
				list.add(getTypeInfo(type));
			}
		}
		return getResultSet(list, MetaTypeInfo.class);
	}

	@Override
	public MetaResultSet getColumns(ConnectionHandle ch, String catalog, Pat schemaPattern, Pat tableNamePattern, Pat columnNamePattern) {
		if ((catalog != null && !DriverConstants.DEFAULT_CATALOG_NAME.equals(catalog))
				|| tableNamePattern.s != null && DriverConstants.DEFAULT_TABLE_NAME.equals(tableNamePattern.s)) {
			DefaultColumn[] columns = DefaultColumn.values();
			List<Object> columnData = new ArrayList<>(columns.length);
			int position = 1;
			for (DefaultColumn column : columns) {
				columnData.add(createColumnMetaData(column, null, position));
				++position;
			}
			return getResultSet(columnData, MetaColumn.class);
		}
		return createEmptyResultSet(MetaColumn.class);
	}

	private static Object createColumnMetaData(DefaultColumn column, String schema, int ordinal) {
		final AtsdType columnType = column.getType();
		return new MetaColumn(
				DriverConstants.DEFAULT_CATALOG_NAME,
				schema,
				DriverConstants.DEFAULT_TABLE_NAME,
				column.getColumnNamePrefix(),
				columnType.sqlTypeCode,
				columnType.sqlType,
				columnType.size,
				null,
				10,
				column.getNullable(),
				0,
				ordinal,
				column.getNullableAsString()
		);
	}

	private MetaResultSet getResultSet(Iterable<Object> iterable, Class<?> clazz) {
		final Field[] fields = clazz.getDeclaredFields();
		final int length = fields.length;
		final List<ColumnMetaData> columns = new ArrayList<>(length);
		final List<String> fieldNames = new ArrayList<>(length);
		int index = 0;
		for (Field field : fields) {
			final String name = AvaticaUtils.camelToUpper(field.getName());
			columns.add(columnMetaData(name, index, field.getType()));
			fieldNames.add(name);
			++index;
		}

		return createResultSet(Collections.<String, Object>emptyMap(), columns,
				CursorFactory.record(clazz, Arrays.asList(fields), fieldNames), new Frame(0, true, iterable));
	}

	private IDataProvider initProvider(StatementHandle statementHandle, String sql) throws UnsupportedEncodingException {
		final ConnectionConfig config = connection.config();
		assert config != null;
		assert connection instanceof AtsdConnection;
		final Properties info = ((AtsdConnection) connection).getInfo();
		final StatementContext newContext = new StatementContext(statementHandle);
		contextMap.put(statementHandle.id, newContext);
		final String login = info != null ? (String) info.get("user") : "";
		final String password = info != null ? (String) info.get("password") : "";
		try {
			final int atsdVersion = connection.getMetaData().getDatabaseMajorVersion();
			newContext.setVersion(atsdVersion);
		} catch (SQLException e) {
			if (log.isDebugEnabled()) {
				log.debug("[initProvider] Error attempting to get databaseMajorVersion", e);
			}
		}
		final IDataProvider dataProvider = new DataProvider(config.url(), sql, login, password, newContext);
		providerCache.put(statementHandle.id, dataProvider);
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
		for (int i = 0; i < length; i++) {
			int sqlType = metadataOrdered.get(i).type.id;
			AtsdType atsdType = EnumUtil.getAtsdTypeBySqlType(sqlType);
			row.add(atsdType.readValue(strings, i));
		}
		return row;
	}

	private static MetaTypeInfo getTypeInfo(AtsdType type) {
		final int sqlTypeCode = type.sqlTypeCode;
		return new MetaTypeInfo(type.sqlType.toUpperCase(Locale.US), sqlTypeCode, type.maxPrecision,
				type.getLiteral(true), type.getLiteral(false),
				DatabaseMetaData.typeNullable, type == AtsdType.STRING_DATA_TYPE,
				DatabaseMetaData.typeSearchable, false, false, false,
				0, 0, 10);
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
