package com.axibase.tsd.driver.jdbc.enums;

import org.apache.calcite.avatica.ColumnMetaData.Rep;

import java.sql.Types;

public enum AtsdType {
	DECIMAL_TYPE("decimal", "decimal", Types.DECIMAL, Rep.OBJECT),
	DOUBLE_DATA_TYPE("double", "double", Types.DOUBLE, Rep.DOUBLE),
	FLOAT_DATA_TYPE("float", "float", Types.FLOAT, Rep.FLOAT),
	INTEGER_DATA_TYPE("integer", "integer", Types.INTEGER, Rep.INTEGER),
	LONG_DATA_TYPE("long", "bigint", Types.BIGINT, Rep.LONG),
	SHORT_DATA_TYPE("short", "smallint", Types.SMALLINT, Rep.SHORT),
	STRING_DATA_TYPE("string", "varchar", Types.VARCHAR, Rep.STRING),
	TIMESTAMP_DATA_TYPE("xsd:dateTimeStamp", "timestamp", Types.TIMESTAMP, Rep.JAVA_SQL_TIMESTAMP);

	public final String originalType;
	public final String sqlType;
	public final int sqlTypeCode;
	public final Rep avaticaType;

	AtsdType(String atsdType, String sqlType, int sqlTypeCode, Rep avaticaType) {
		this.originalType = atsdType;
		this.sqlType = sqlType;
		this.sqlTypeCode = sqlTypeCode;
		this.avaticaType = avaticaType;
	}

}


