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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.AvaticaUtils;
import org.apache.calcite.avatica.ColumnMetaData;
import org.apache.calcite.avatica.ConnectionConfig;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.MetaImpl;
import org.apache.calcite.avatica.MissingResultsException;
import org.apache.calcite.avatica.NoSuchStatementException;
import org.apache.calcite.avatica.QueryState;
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
	private static final LoggingFacade logger = LoggingFacade.getLogger(AtsdMeta.class);
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
			if (logger.isDebugEnabled())
				logger.debug("[prepare] " + e.getMessage());
		}
		final int id = idGenerator.getAndIncrement();
		if (logger.isTraceEnabled()) {
			logger.trace("[prepare] locked: {} handle: {} query: {}", lock.getHoldCount(), id, query);
		}
		try {
			final IDataProvider provider = initProvider(id, query);
			provider.checkScheme(query);
			final ContentMetadata contentMetadata = findMetadata(query, ch.id, id);
			return new StatementHandle(ch.id, id, contentMetadata.getSign());
		} catch (final AtsdException | GeneralSecurityException | IOException e) {
			if (logger.isDebugEnabled())
				logger.debug("[prepare]" + e.getMessage());
		}
		return null;
	}

	@Override
	public ExecuteResult execute(StatementHandle h, List<TypedValue> parameterValues, long maxRowCount)
			throws NoSuchStatementException {
		if (logger.isTraceEnabled()) {
			logger.trace("[execute] maxRowCount: {} parameters: {} handle: {}", maxRowCount, parameterValues.size(),
					h.toString());
		}
		final IDataProvider provider = providerCache.get(h.id);
		assert provider != null;
		String query = new String(h.signature.sql);
		if (query.contains("?")) {
			final Iterator<TypedValue> iterator = parameterValues.iterator();
			final StringBuilder sb = new StringBuilder();
			final String[] parts = query.split("\\?", -1);
			assert parts.length == parameterValues.size() + 1;
			if (parts.length != parameterValues.size() + 1) {
				throw new IndexOutOfBoundsException(
						String.format("Number of specified values [%d] is not match to number of occurences [%d]",
								parameterValues.size(), parts.length));
			}
			for (String part : parts) {
				sb.append(part);
				if (!iterator.hasNext())
					break;
				final TypedValue next = iterator.next();
				if (next.value instanceof Number)
					sb.append((Number) next.value);
				else if (next.value instanceof String)
					sb.append('\'').append((String) next.value).append('\'');
				else if (next.value instanceof java.sql.Date) {
					sb.append('\'').append(DATE_FORMATTER.get().format((java.sql.Date) next.value)).append('\'');
				} else if (next.value instanceof java.sql.Time) {
					sb.append('\'').append(TIME_FORMATTER.get().format((java.sql.Time) next.value)).append('\'');
				} else if (next.value instanceof java.sql.Timestamp) {
					sb.append('\'').append(TIMESTAMP_FORMATTER.get().format((java.sql.Timestamp) next.value))
							.append('\'');
				}
			}
			if (logger.isDebugEnabled())
				logger.debug("[execute] " + sb.toString());
			provider.getContentDescription().setQuery(sb.toString());
		}
		try {
			provider.fetchData(-1);
			final ContentMetadata contentMetadata = findMetadata(query, h.connectionId, h.id);
			return new ExecuteResult(contentMetadata.getList());
		} catch (final AtsdException | GeneralSecurityException | IOException e) {
			if (logger.isDebugEnabled())
				logger.debug("[execute] " + e.getMessage());
			throw new NoSuchStatementException(h);
		}
	}

	@Override
	public ExecuteResult prepareAndExecute(final StatementHandle h, String query, long maxRowCount,
			final PrepareCallback callback) throws NoSuchStatementException {
		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e) {
			if (logger.isDebugEnabled())
				logger.debug("[prepareAndExecute] " + e.getMessage());
		}
		if (logger.isTraceEnabled()) {
			logger.trace("[prepareAndExecute] locked: {} maxRowCount: {} handle: {} query: {}", lock.getHoldCount(),
					maxRowCount, h.toString(), query);
		}
		try {
			final IDataProvider provider = initProvider(h.id, query);
			provider.fetchData(maxRowCount);
			final ContentMetadata contentMetadata = findMetadata(query, h.connectionId, h.id);
			synchronized (callback.getMonitor()) {
				// callback.clear();
				callback.assign(contentMetadata.getSign(), null, -1);
			}
			final ExecuteResult result = new ExecuteResult(contentMetadata.getList());
			callback.execute();
			return result;
		} catch (final AtsdException | IOException | SQLException | GeneralSecurityException e) {
			if (logger.isDebugEnabled())
				logger.debug("[prepareAndExecute] " + e.getMessage());
			throw new NoSuchStatementException(h);
		}
	}

	@Override
	public Frame fetch(final StatementHandle h, long loffset, int fetchMaxRowCount)
			throws NoSuchStatementException, MissingResultsException {
		final int offset = (int) loffset;
		if (logger.isTraceEnabled()) {
			logger.trace("[fetch] fetchMaxRowCount: {} offset: {}", fetchMaxRowCount, offset);
		}
		IDataProvider provider = providerCache.get(h.id);
		assert provider != null;
		final ContentDescription cd = provider.getContentDescription();
		final IStoreStrategy strategy = provider.getStrategy();
		try {
			if (offset == 0) {
				final String[] headers = strategy.openToRead();
				if (headers == null || headers.length == 0)
					throw new MissingResultsException(h);
				cd.setHeaders(headers);
			}
			final List<String[]> subList = strategy.fetch(offset, fetchMaxRowCount);
			final List<Object> rows = getFrame(h, loffset, fetchMaxRowCount, subList);
			return new Meta.Frame(loffset, rows.size() < fetchMaxRowCount, rows);
		} catch (final AtsdException | IOException e) {
			if (logger.isDebugEnabled())
				logger.debug("[fetch] " + e.getMessage());
			throw new MissingResultsException(h);
		}

	}

	@Override
	public void closeStatement(StatementHandle h) {
		if (logger.isDebugEnabled())
			logger.debug("[closeStatement] " + h.id + "->" + h.toString());
		if (metaCache != null && metaCache.size() != 0)
			metaCache.remove(h.id);
		if (contextMap != null && contextMap.size() != 0)
			contextMap.remove(h.id);
		if (providerCache == null || providerCache.size() == 0)
			return;
		final IDataProvider provider = providerCache.remove(h.id);
		if (provider != null)
			try {
				provider.close();
			} catch (final Exception e) {
				if (logger.isDebugEnabled())
					logger.debug("[closeStatement] " + e.getMessage());
			}
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
			if (logger.isTraceEnabled())
				logger.trace("[unlocked]");
		}
		if (logger.isTraceEnabled())
			logger.trace("[closedStatement]");
	}

	public void close() {
		if (metaCache != null && metaCache.size() != 0)
			metaCache.clear();
		if (providerCache != null && providerCache.size() != 0)
			providerCache.clear();
		if (contextMap != null && contextMap.size() != 0)
			contextMap.clear();
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
			if (logger.isTraceEnabled())
				logger.trace("[unlocked]");
		}
		if (logger.isTraceEnabled())
			logger.trace("[closed]");
	}

	@Override
	public boolean syncResults(StatementHandle sh, QueryState state, long offset) throws NoSuchStatementException {
		if (logger.isDebugEnabled())
			logger.debug("[syncResults] " + offset);
		return false;
	}

	@Override
	public Map<DatabaseProperty, Object> getDatabaseProperties(ConnectionHandle ch) {
		return super.getDatabaseProperties(ch);
	}

	@Override
	public MetaResultSet getTables(ConnectionHandle ch, String catalog, Pat schemaPattern, Pat tableNamePattern,
			List<String> typeList) {
		return super.getTables(ch, catalog, schemaPattern, tableNamePattern, typeList);
	}

	@Override
	public MetaResultSet getSchemas(ConnectionHandle ch, String catalog, Pat schemaPattern) {
		assert connection instanceof AtsdConnection;
		final Properties info = ((AtsdConnection) connection).getInfo();
		String username = info != null ? (String) info.get("user") : "";
		final Iterable<Object> iterable = (Iterable<Object>) new ArrayList<Object>(
				Arrays.asList(new MetaSchema(DriverConstants.CATALOG_NAME, WordUtils.capitalize(username))));
		return getResultSet(iterable, MetaSchema.class, "TABLE_SCHEM", "TABLE_CATALOG");
	}

	@Override
	public MetaResultSet getCatalogs(ConnectionHandle ch) {
		final Iterable<Object> iterable = (Iterable<Object>) new ArrayList<Object>(
				Arrays.asList(new MetaCatalog(DriverConstants.CATALOG_NAME)));
		return getResultSet(iterable, MetaCatalog.class, "TABLE_CAT");
	}

	@Override
	public MetaResultSet getTableTypes(ConnectionHandle ch) {
		final Iterable<Object> iterable = (Iterable<Object>) new ArrayList<Object>(
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
		return getResultSet((Iterable<Object>) list, MetaTypeInfo.class, "TYPE_NAME", "DATA_TYPE", "PRECISION",
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
		final IDataProvider dataProvider = new DataProvider(config.url(), sql,
				info != null ? (String) info.get("user") : "", info != null ? (String) info.get("password") : "",
				newContext);
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

	private List<Object> getFrame(final StatementHandle h, long loffset, int fetchMaxRowCount,
			final List<String[]> subList) {
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
				if (logger.isDebugEnabled())
					logger.debug("[getFrame] array length discrepancy: " + Arrays.toString(sarray));
				continue;
			}
			final List<Object> row = new ArrayList<>();
			for (int i = 0; i < sarray.length; i++) {
				for (ColumnMetaData columnMetaData : metadataList) {
					if (i != columnMetaData.ordinal - 1)
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
							if (logger.isDebugEnabled())
								logger.debug("[getFrame] short type mismatched: {} on {} position",
										Arrays.toString(sarray), i);
						}
						row.add(s);
						break;
					case Types.INTEGER:
						Integer n = null;
						try {
							n = Integer.valueOf(sarray[i]);
						} catch (final NumberFormatException nfe) {
							if (logger.isDebugEnabled())
								logger.debug("[getFrame] int type mismatched: {} on {} position",
										Arrays.toString(sarray), i);
						}
						row.add(n);
						break;
					case Types.BIGINT:
						Long l = null;
						try {
							l = Long.valueOf(sarray[i]);
						} catch (final NumberFormatException nfe) {
							if (logger.isDebugEnabled())
								logger.debug("[getFrame] long type mismatched: {} on {} position",
										Arrays.toString(sarray), i);
						}
						row.add(l);
						break;
					case Types.FLOAT:
						Float f = null;
						try {
							f = Float.valueOf(sarray[i]);
						} catch (final NumberFormatException nfe) {
							if (logger.isDebugEnabled())
								logger.debug("[getFrame] float type mismatched: {} on {} position",
										Arrays.toString(sarray), i);
						}
						row.add(f);
						break;
					case Types.DOUBLE:
						Double d = null;
						try {
							d = Double.valueOf(sarray[i]);
						} catch (final NumberFormatException nfe) {
							if (logger.isDebugEnabled())
								logger.debug("[getFrame] double type mismatched: {} on {} position",
										Arrays.toString(sarray), i);
						}
						row.add(d);
						break;
					case Types.DECIMAL:
						BigDecimal bd = null;
						try {
							bd = new BigDecimal(sarray[i]);
						} catch (final NumberFormatException nfe) {
							if (logger.isDebugEnabled())
								logger.debug("[getFrame] decimal type mismatched: {} on {} position",
										Arrays.toString(sarray), i);
						}
						row.add(bd);
						break;
					case Types.TIMESTAMP:
						Timestamp ts = null;
						try {
							Date dt = TIMESTAMP_FORMATTER.get().parse(sarray[i]);
							ts = new Timestamp(dt.getTime());
						} catch (final ParseException e) {
							if (logger.isDebugEnabled())
								logger.debug("[getFrame] " + e.getMessage());
							try {
								Date dt = TIMESTAMP_SHORT_FORMATTER.get().parse(sarray[i]);
								ts = new Timestamp(dt.getTime());
							} catch (ParseException e1) {
								if (logger.isDebugEnabled())
									logger.debug("[getFrame] " + e1.getMessage());
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
			rows.add(row);
		}
		return rows;
	}

	private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	private static final ThreadLocal<SimpleDateFormat> TIME_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("HH:mm:ss");
		}
	};

	public static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		}
	};

	public static final ThreadLocal<SimpleDateFormat> TIMESTAMP_SHORT_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
		if (logger.isDebugEnabled())
			logger.debug("[commit] " + ch.id + "->" + ch.toString());
	}

	@Override
	public void rollback(ConnectionHandle ch) {
		if (logger.isDebugEnabled())
			logger.debug("[rollback] " + ch.id + "->" + ch.toString());
	}

	private static final int TIMESTAMP_LENGTH = "2016-01-01T00:00:00.000".length();
	private final AtomicInteger idGenerator = new AtomicInteger(1);
	private final Map<Integer, ContentMetadata> metaCache = new ConcurrentHashMap<>();
	private final Map<Integer, IDataProvider> providerCache = new ConcurrentHashMap<>();
	private final Map<Integer, StatementContext> contextMap = new ConcurrentHashMap<>();
}
