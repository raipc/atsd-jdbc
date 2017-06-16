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
package com.axibase.tsd.driver.jdbc.enums;

import com.axibase.tsd.driver.jdbc.intf.MetadataColumnDefinition;

public enum DefaultColumn implements MetadataColumnDefinition {
	TIME("time", AtsdType.LONG_DATA_TYPE, 0),
	DATETIME("datetime", AtsdType.TIMESTAMP_DATA_TYPE, 0),
	VALUE("value", AtsdType.FLOAT_DATA_TYPE, 0),
	TEXT("text", AtsdType.STRING_DATA_TYPE, 1),
	METRIC("metric", AtsdType.STRING_DATA_TYPE, 0),
	ENTITY("entity", AtsdType.STRING_DATA_TYPE, 0),
	TAGS("tags", AtsdType.STRING_DATA_TYPE, 1),
	ENTITY_TAGS("entity.tags", AtsdType.STRING_DATA_TYPE, 1),
	METRIC_TAGS("metric.tags", AtsdType.STRING_DATA_TYPE, 1),
	ENTITY_GROUPS("entity.groups", AtsdType.STRING_DATA_TYPE, 1);

	private final String columnNamePrefix;
	private final AtsdType type;
	private final int nullable;
	
	DefaultColumn(String prefix, AtsdType type, int nullable) {
		this.columnNamePrefix = prefix;
		this.type = type;
		this.nullable = nullable;
	}

	public String getColumnNamePrefix() {
		return columnNamePrefix;
	}

	public AtsdType getType() {
		return type;
	}

	public int getNullable() {
		return nullable;
	}

	public String getNullableAsString() {
		return NULLABLE_AS_STRING[nullable];
	}
}
