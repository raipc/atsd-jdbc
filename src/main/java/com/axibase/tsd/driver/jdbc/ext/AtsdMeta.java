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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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
	private static final int TIMESTAMP_LENGTH = "2016-01-01T00:00:00.000".length();
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
	public StatementHandle prepare(ConnectionHandle ch, String query, long maxRowCount) {
		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e) {
			if (log.isDebugEnabled())
				log.debug("[prepare] " + e.getMessage());
		}
		final int id = idGenerator.getAndIncrement();
		if (log.isTraceEnabled()) {
			log.trace("[prepare] locked: {} handle: {} query: {}", lock.getHoldCount(), id, query);
		}
		try {
			final IDataProvider provider = initProvider(id, query);
			provider.checkScheme(query);
			final ContentMetadata contentMetadata = findMetadata(query, ch.id, id);
			return new StatementHandle(ch.id, id, contentMetadata.getSign());
		} catch (final AtsdException | GeneralSecurityException | IOException e) {
			if (log.isDebugEnabled())
				log.debug("[prepare]" + e.getMessage());
		}
		return null;
	}

	@Override
	public ExecuteResult execute(StatementHandle statementHandle, List<TypedValue> parameterValues, long maxRowCount)
			throws NoSuchStatementException {
		if (log.isTraceEnabled()) {
			log.trace("[execute] maxRowCount: {} parameters: {} handle: {}", maxRowCount, parameterValues.size(),
					statementHandle.toString());
		}
		final IDataProvider provider = providerCache.get(statementHandle.id);
		assert provider != null;
		final String query = statementHandle.signature.sql;
		if (query.contains("?")) {
			final Iterator<TypedValue> iterator = parameterValues.iterator();
			final StringBuilder sb = new StringBuilder();
			final String[] parts = query.split("\\?", -1);
			if (parts.length != parameterValues.size() + 1) {
				throw new IndexOutOfBoundsException(
						String.format("Number of specified values [%d] does not match to number of occurences [%d]",
								parameterValues.size(), parts.length));
			}
			for (String part : parts) {
				sb.append(part);
				if (!iterator.hasNext())
					break;
				final TypedValue next = iterator.next();
				if (next.value instanceof Number)
					sb.append(next.value);
				else if (next.value instanceof String)
					sb.append('\'').append((String) next.value).append('\'');
				else if (next.value instanceof java.sql.Date) {
					sb.append('\'').append(DATE_FORMATTER.get().format((java.sql.Date) next.value)).append('\'');
				} else if (next.value instanceof java.sql.Time) {
					sb.append('\'').append(TIME_FORMATTER.get().format((java.sql.Time) next.value)).append('\'');
				} else if (next.value instanceof Timestamp) {
					sb.append('\'').append(TIMESTAMP_FORMATTER.get().format((Timestamp) next.value)).append('\'');
				}
			}
			if (log.isDebugEnabled())
				log.debug("[execute] " + sb.toString());
			provider.getContentDescription().setQuery(sb.toString());
		}
		try {
			final int timeout = getQueryTimeout(statementHandle.id);
			provider.fetchData(maxRowCount, timeout);
			final ContentMetadata contentMetadata = findMetadata(query, statementHandle.connectionId, statementHandle.id);
			return new ExecuteResult(contentMetadata.getList());
		} catch (final AtsdException | GeneralSecurityException | IOException e) {
			if (log.isDebugEnabled())
				log.debug("[execute] " + e.getMessage());
			throw new NoSuchStatementException(statementHandle);
		}
	}

	private int getQueryTimeout(int statementHandleId) {
		int timeout = 0;
		final Statement statement = connection.statementMap.get(statementHandleId);
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
	public ExecuteResult prepareAndExecute(final StatementHandle statementHandle, String query, long maxRowCount,
			final PrepareCallback callback) throws NoSuchStatementException {
		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e) {
			if (log.isDebugEnabled())
				log.debug("[prepareAndExecute] " + e.getMessage());
		}
		if (log.isTraceEnabled()) {
			log.trace("[prepareAndExecute] locked: {} maxRowCount: {} handle: {} query: {}", lock.getHoldCount(),
					maxRowCount, statementHandle.toString(), query);
		}
		try {
			final IDataProvider provider = initProvider(statementHandle.id, query);
			final Statement statement = (Statement)callback.getMonitor();
			provider.fetchData(maxRowCount, statement.getQueryTimeout());
			final ContentMetadata contentMetadata = findMetadata(query, statementHandle.connectionId, statementHandle.id);
			synchronized (callback.getMonitor()) {
				// callback.clear();
				callback.assign(contentMetadata.getSign(), null, -1);
			}
			final ExecuteResult result = new ExecuteResult(contentMetadata.getList());
			callback.execute();
			return result;
		} catch (final AtsdException | IOException | SQLException | GeneralSecurityException e) {
			if (log.isDebugEnabled())
				log.debug("[prepareAndExecute] " + e.getMessage());
			throw new NoSuchStatementException(statementHandle);
		}
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
				if (headers == null || headers.length == 0)
					throw new MissingResultsException(statementHandle);
				cd.setHeaders(headers);
			}
			final List<String[]> subList = strategy.fetch(offset, fetchMaxRowCount);
			final List<Object> rows = getFrame(statementHandle, fetchMaxRowCount, subList);
			return new Meta.Frame(loffset, rows.size() < fetchMaxRowCount, rows);
		} catch (final AtsdException | IOException e) {
			if (log.isDebugEnabled())
				log.debug("[fetch] " + e.getMessage());
			throw new MissingResultsException(statementHandle);
		}

	}

	@Override
	public void closeStatement(StatementHandle statementHandle) {
		if (log.isDebugEnabled())
			log.debug("[closeStatement] " + statementHandle.id + "->" + statementHandle.toString());
		closeProviderCaches(statementHandle);
		closeProvider(statementHandle);
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
			if (log.isTraceEnabled())
				log.trace("[unlocked]");
		}
		if (log.isTraceEnabled())
			log.trace("[closedStatement]");
	}

	private void closeProviderCaches(StatementHandle h) {
		if (metaCache != null && !metaCache.isEmpty())
			metaCache.remove(h.id);
		if (contextMap != null && !contextMap.isEmpty())
			contextMap.remove(h.id);

	}

	private void closeProvider(StatementHandle statementHandle) {
		if (providerCache != null && !providerCache.isEmpty()) {
			final IDataProvider provider = providerCache.remove(statementHandle.id);
			if (provider != null)
				try {
					provider.close();
				} catch (final Exception e) {
					if (log.isDebugEnabled())
						log.debug("[closeStatement] " + e.getMessage());
				}
		}
	}

	public void close() {
		closeCaches();
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
			if (log.isTraceEnabled())
				log.trace("[unlocked]");
		}
		if (log.isTraceEnabled())
			log.trace("[closed]");
	}

	private void closeCaches() {
		if (metaCache != null && !metaCache.isEmpty())
			metaCache.clear();
		if (contextMap != null && !contextMap.isEmpty())
			contextMap.clear();
		if (providerCache != null && !providerCache.isEmpty())
			providerCache.clear();
	}

	@Override
	public boolean syncResults(StatementHandle sh, QueryState state, long offset) throws NoSuchStatementException {
		if (log.isDebugEnabled())
			log.debug("[syncResults] " + offset);
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
		final List<Object> list = new ArrayList<Object>();
		list.add(getTypeInfo("DECIMAL", Types.DECIMAL, false));
		list.add(getTypeInfo("DOUBLE", Types.DOUBLE, false));
		list.add(getTypeInfo("FLOAT", Types.FLOAT, false));
		list.add(getTypeInfo("INTEGER", Types.INTEGER, false));
		list.add(getTypeInfo("LONG", Types.BIGINT, false));
		list.add(getTypeInfo("SHORT", Types.SMALLINT, false));
		list.add(getTypeInfo("STRING", Types.VARCHAR, true));
		list.add(getTypeInfo("TIMESTAMP", Types.TIMESTAMP, false));
		return getResultSet(list, MetaTypeInfo.class, "TYPE_NAME", "DATA_TYPE", "PRECISION",
				"LITERAL_PREFIX", "LITERAL_SUFFIX", "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE",
				"UNSIGNED_ATTRIBUTE", "FIXED_PREC_SCALE", "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE",
				"MAXIMUM_SCALE", "NUM_PREC_RADIX");
	}

	private <E> MetaResultSet getResultSet(Iterable<Object> iterable, Class<?> clazz, String... names) {
		final List<ColumnMetaData> columns = new ArrayList<>();
		final List<Field> fields = new ArrayList<>();
		final List<String> fieldNames = new ArrayList<>();
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
		return createResultSet(Collections.<String, Object> emptyMap(), columns,
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
		final IDataProvider dataProvider = new DataProvider(config.url(), sql, login, password,	newContext);
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

	private List<Object> getFrame(final StatementHandle h, int fetchMaxRowCount, final List<String[]> subList) {
		IDataProvider provider = providerCache.get(h.id);
		assert provider != null;
		final String[] headers = provider.getContentDescription().getHeaders();
		final ContentMetadata contentMetadata = metaCache.get(h.id);
		final List<ColumnMetaData> metadataList = contentMetadata.getMetadataList();
		final List<Object> rows = new ArrayList<>();
		for (final String[] sarray : subList) {
			if (sarray == null || headers == null || rows.size() == fetchMaxRowCount) {
				break;
			}
			if (sarray.length != headers.length) {
				if (log.isDebugEnabled())
					log.debug("[getFrame] array length discrepancy: " + Arrays.toString(sarray));
				continue;
			}
			rows.add(getFrameRow(metadataList, sarray));
		}
		return rows;
	}

	private List<Object> getFrameRow(final List<ColumnMetaData> metadataList, final String[] sarray) {
		final List<Object> row = new ArrayList<>();
		for (int i = 0; i < sarray.length; i++) {
			for (ColumnMetaData columnMetaData : metadataList) {
				if (i != columnMetaData.ordinal)
					continue;
				if (StringUtils.isEmpty(sarray[i])) {
					row.add(columnMetaData.type.id == Types.VARCHAR ? sarray[i] : null);
					continue;
				}
				switch (columnMetaData.type.id) {
				case Types.SMALLINT:
					Short s = null;
					try {
						s = Short.valueOf(sarray[i]);
					} catch (final NumberFormatException nfe) {
						if (log.isDebugEnabled())
							log.debug("[getFrame] short type mismatched: {} on {} position", Arrays.toString(sarray),
									i);
					}
					row.add(s);
					break;
				case Types.INTEGER:
					Integer n = null;
					try {
						n = Integer.valueOf(sarray[i]);
					} catch (final NumberFormatException nfe) {
						if (log.isDebugEnabled())
							log.debug("[getFrame] int type mismatched: {} on {} position", Arrays.toString(sarray), i);
					}
					row.add(n);
					break;
				case Types.BIGINT:
					Long l = null;
					try {
						l = Long.valueOf(sarray[i]);
					} catch (final NumberFormatException nfe) {
						if (log.isDebugEnabled())
							log.debug("[getFrame] long type mismatched: {} on {} position", Arrays.toString(sarray), i);
					}
					row.add(l);
					break;
				case Types.FLOAT:
				case Types.DOUBLE:
					Double d = null;
					try {
						d = Double.valueOf(sarray[i]);
					} catch (final NumberFormatException nfe) {
						if (log.isDebugEnabled())
							log.debug("[getFrame] double type mismatched: {} on {} position", Arrays.toString(sarray),
									i);
					}
					row.add(d);
					break;
				case Types.DECIMAL:
					BigDecimal bd = null;
					try {
						bd = new BigDecimal(sarray[i]);
					} catch (final NumberFormatException nfe) {
						if (log.isDebugEnabled())
							log.debug("[getFrame] decimal type mismatched: {} on {} position", Arrays.toString(sarray),
									i);
					}
					row.add(bd);
					break;
				case Types.TIMESTAMP:
					Timestamp ts = null;
					try {
						Date dt = TIMESTAMP_FORMATTER.get().parse(sarray[i]);
						ts = new Timestamp(dt.getTime());
					} catch (final ParseException e) {
						if (log.isDebugEnabled())
							log.debug("[getFrame] " + e.getMessage());
						try {
							Date dt = TIMESTAMP_SHORT_FORMATTER.get().parse(sarray[i]);
							ts = new Timestamp(dt.getTime());
						} catch (ParseException e1) {
							if (log.isDebugEnabled())
								log.debug("[getFrame] " + e1.getMessage());
						}
					}
					row.add(ts);
					break;
				case Types.VARCHAR:
				default:
					row.add(sarray[i]);
				}
				break;
			}
		}
		return row;
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

	private static MetaTypeInfo getTypeInfo(String name, int type, boolean isCaseSensitive) {
		return new MetaTypeInfo(name, type, getMaxPrecision(type), getLiteral(type, true), getLiteral(type, false),
				DatabaseMetaData.typeNullable, isCaseSensitive, DatabaseMetaData.typeSearchable, false, false, false, 0,
				0, 10);
	}

	public static String getLiteral(int type, boolean isPrefix) {
		switch (type) {
		case Types.VARCHAR:
			return "'";
		case Types.TIMESTAMP:
			return isPrefix ? "TIMESTAMP '" : "'";
		default:
			return null;
		}
	}

	public static int getMaxPrecision(int type) {
		switch (type) {
		case Types.VARCHAR:
			return 2147483647;
		case Types.TIMESTAMP:
			return TIMESTAMP_LENGTH;
		case Types.SMALLINT:
			return 5;
		case Types.INTEGER:
			return 10;
		case Types.BIGINT:
			return 19;
		case Types.FLOAT:
			return 23;
		case Types.DOUBLE:
			return 52;
		default:
			return -1;
		}
	}

	// Since Calcite 1.6.0

	@Override
	public void commit(ConnectionHandle ch) {
		if (log.isDebugEnabled())
			log.debug("[commit] " + ch.id + "->" + ch.toString());
	}

	@Override
	public void rollback(ConnectionHandle ch) {
		if (log.isDebugEnabled())
			log.debug("[rollback] " + ch.id + "->" + ch.toString());
	}

}
