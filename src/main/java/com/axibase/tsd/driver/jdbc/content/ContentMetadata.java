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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;

import com.axibase.tsd.driver.jdbc.enums.AtsdType;
import org.apache.calcite.avatica.AvaticaParameter;
import org.apache.calcite.avatica.ColumnMetaData;
import org.apache.calcite.avatica.Meta.CursorFactory;
import org.apache.calcite.avatica.Meta.MetaResultSet;
import org.apache.calcite.avatica.Meta.Signature;
import org.apache.calcite.avatica.Meta.StatementType;
import org.apache.commons.lang3.StringUtils;

import static com.axibase.tsd.driver.jdbc.DriverConstants.*;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

@SuppressWarnings("unchecked")
public class ContentMetadata {
	private static final LoggingFacade logger = LoggingFacade.getLogger(ContentMetadata.class);

	private final Signature sign;
	private final List<MetaResultSet> list;
	private final List<ColumnMetaData> metadataList;

	public ContentMetadata(String scheme, String sql, String connectionId, int statementId)
			throws AtsdException, IOException {
		metadataList = StringUtils.isNoneEmpty(scheme) ? buildMetadataList(scheme)
				: Collections.<ColumnMetaData> emptyList();
		sign = new Signature(metadataList, sql, Collections.<AvaticaParameter> emptyList(), null, CursorFactory.LIST,
				StatementType.SELECT);
		list = Collections.unmodifiableList(
				Collections.singletonList(MetaResultSet.create(connectionId, statementId, false, sign, null)));
	}

	public Signature getSign() {
		return sign;
	}

	public List<MetaResultSet> getList() {
		return list;
	}

	public List<ColumnMetaData> getMetadataList() {
		return metadataList;
	}

	static List<ColumnMetaData> buildMetadataList(String json)
			throws JsonParseException, MalformedURLException, IOException, AtsdException {
		final Map<String, Object> jsonObject = getJsonScheme(json);
		if (jsonObject == null)
			throw new AtsdException("Wrong metadata content");
		final Map<String, Object> publisher = (Map<String, Object>) jsonObject.get(PUBLISHER_SECTION);
		if (publisher == null)
			throw new AtsdException("Wrong metadata publisher");
		final String schema = (String) publisher.get(SCHEMA_NAME_PROPERTY);
		if (schema == null)
			throw new AtsdException("Wrong metadata schema");
		final Map<String, Object> tableSchema = (Map<String, Object>) jsonObject.get(TABLE_SCHEMA_SECTION);
		if (tableSchema == null)
			throw new AtsdException("Wrong table schema");
		final List<Object> columns = (List<Object>) tableSchema.get(COLUMNS_SCHEME);
		if (columns == null)
			throw new AtsdException("Wrong columns schema");
		final List<ColumnMetaData> metadataList = new ArrayList<>();
		int ind = 0;
		for (final Object obj : columns) {
			final ColumnMetaData cmd = getColumnMetaData(schema, ind, obj);
			metadataList.add(cmd);
			ind++;
		}
		if (logger.isDebugEnabled())
			logger.debug(String.format("Schema is processed. %s headers are found.", metadataList.size()));
		return Collections.unmodifiableList(metadataList);
	}

	private static ColumnMetaData getColumnMetaData(String schema, int ind, final Object obj) {
		final Map<String, Object> property = (Map<String, Object>) obj;
		String name = (String) property.get(NAME_PROPERTY);
		String title = (String) property.get(TITLE_PROPERTY);
		String table = (String) property.get(TABLE_PROPERTY);
		String datatype = (String) property.get(DATATYPE_PROPERTY);
		Integer index = (Integer) property.get(INDEX_PROPERTY);
		final ColumnMetaData.AvaticaType atype = getAvaticaType(datatype);
		return new ColumnMetaData(index != null ? index - 1 : ind, false, false, false,
				false, 0, false, 10, name, title, schema, 1, 1, table, DEFAULT_CATALOG_NAME, atype, true, false,
				false, atype.rep.clazz.getCanonicalName());
	}

	private static Map<String, Object> getJsonScheme(String json) throws IOException {
		final MappingJsonFactory jsonFactory = new MappingJsonFactory();
		try (final InputStream is = new ByteArrayInputStream(json.getBytes(Charset.defaultCharset()));
				JsonParser parser = jsonFactory.createParser(is)) {
			final JsonToken token = parser.nextToken();
			Class<?> type;
			if (token == JsonToken.START_OBJECT) {
				type = Map.class;
			} else if (token == JsonToken.START_ARRAY) {
				type = List.class;
			} else {
				type = String.class;
			}
			return (Map<String, Object>) parser.readValueAs(type);
		}
	}

	private static ColumnMetaData.AvaticaType getAvaticaType(String datatype) {
		final AtsdType type = AtsdType.getAtsdTypeByOriginalName(datatype);
		return new ColumnMetaData.AvaticaType(type.sqlTypeCode, type.sqlType, type.avaticaType);
	}

}
