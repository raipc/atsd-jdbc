package com.axibase.tsd.driver.jdbc.enums;

public enum DefaultColumn {
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

	private static final String[] NULLABLE_AS_STRING = {"NO", "YES", ""};

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
