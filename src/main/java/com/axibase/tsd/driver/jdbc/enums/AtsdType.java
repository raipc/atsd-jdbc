package com.axibase.tsd.driver.jdbc.enums;

import org.apache.calcite.avatica.ColumnMetaData.Rep;

import java.sql.Types;

public enum AtsdType {
	BIGINT_DATA_TYPE("bigint", "bigint", Types.BIGINT, Rep.LONG, 19),
	DECIMAL_TYPE("decimal", "decimal", Types.DECIMAL, Rep.OBJECT, -1),
	DOUBLE_DATA_TYPE("double", "double", Types.DOUBLE, Rep.DOUBLE, 52),
	FLOAT_DATA_TYPE("float", "float", Types.FLOAT, Rep.FLOAT, 23),
	INTEGER_DATA_TYPE("integer", "integer", Types.INTEGER, Rep.INTEGER, 10),
	LONG_DATA_TYPE("long", "bigint", Types.BIGINT, Rep.LONG, 19),
	SHORT_DATA_TYPE("short", "smallint", Types.SMALLINT, Rep.SHORT, 5),
	SMALLINT_DATA_TYPE("smallint", "smallint", Types.SMALLINT, Rep.SHORT, 5),
	STRING_DATA_TYPE("string", "varchar", Types.VARCHAR, Rep.STRING, 2147483647),
	TIMESTAMP_DATA_TYPE("xsd:dateTimeStamp", "timestamp", Types.TIMESTAMP, Rep.JAVA_SQL_TIMESTAMP,
			"2016-01-01T00:00:00.000".length());

	public final String originalType;
	public final String sqlType;
	public final int sqlTypeCode;
	public final Rep avaticaType;
	public final int maxPrecision;

	AtsdType(String atsdType, String sqlType, int sqlTypeCode, Rep avaticaType, int maxPrecision) {
		this.originalType = atsdType;
		this.sqlType = sqlType;
		this.sqlTypeCode = sqlTypeCode;
		this.avaticaType = avaticaType;
		this.maxPrecision = maxPrecision;
	}

}
