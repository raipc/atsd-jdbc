package com.axibase.tsd.driver.jdbc.enums;

public enum DefaultColumn {
	TIME("time", AtsdType.LONG_DATA_TYPE),
	DATETIME("datetime", AtsdType.TIMESTAMP_DATA_TYPE),
	VALUE("value", AtsdType.FLOAT_DATA_TYPE),
	METRIC("metric", AtsdType.STRING_DATA_TYPE),
	ENTITY("entity", AtsdType.STRING_DATA_TYPE),
	TAGS("tags", AtsdType.STRING_DATA_TYPE),
	ENTITY_TAGS("entity.tags", AtsdType.STRING_DATA_TYPE),
	METRIC_TAGS("metric.tags", AtsdType.STRING_DATA_TYPE),
	ENTITY_GROUPS("entity.groups", AtsdType.STRING_DATA_TYPE);

	private final String columnNamePrefix;
	private final AtsdType type;
	
	DefaultColumn(String prefix, AtsdType type) {
		this.columnNamePrefix = prefix;
		this.type = type;
	}

	public String getColumnNamePrefix() {
		return columnNamePrefix;
	}

	public AtsdType getType() {
		return type;
	}
}
