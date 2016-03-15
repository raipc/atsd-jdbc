package com.axibase.tsd.driver.jdbc;

public interface DriverConstants {
	static final String CATALOG_NAME = "ATSD";
	static final String CONNECT_URL_PREFIX = "jdbc:axibase:atsd:";
	static final String DATABASE_PRODUCT_NAME = "Axibase";
	static final String DATABASE_PRODUCT_VERSION = "Axibase Time Series Database";
	static final String JDBC_DRIVER_NAME = "ATSD JDBC driver";
	static final String PRODUCT_NAME_KEY = "product.name";
	static final String PRODUCT_VERSION_KEY = "product.version";
	static final String DRIVER_VERSION_KEY = "driver.version";
	static final String DRIVER_NAME_KEY = "driver.name";
	static final String DATABASE_VERSION_MAJOR_KEY = "database.version.major";
	static final String DATABASE_VERSION_MINOR_KEY = "database.version.minor";
	static final String DRIVER_VERSION_MAJOR_KEY = "driver.version.major";
	static final String DRIVER_VERSION_MINOR_KEY = "driver.version.minor";
	static final String DRIVER_PROPERTIES = "META-INF/axibase-atsd-jdbc.properties";
	static final String JDBC_COMPLIENT_KEY = "jdbc.compliant";
	static final String RETRIES_NUMBER = "2";
	static final String JDBC_DRIVER_VERSION_DEFAULT = "1.2.0";
	static final int    DRIVER_VERSION_MAJOR_DEFAULT = 1;
	static final int    DRIVER_VERSION_MINOR_DEFAULT = 2;

	static final String FORMAT_PARAM_NAME = "outputFormat";
	static final String FORMAT_PARAM_VALUE = "csv";
	static final String PARAM_SEPARATOR = ";";
	static final String PROTOCOL_SEPARATOR = "://";
	static final String Q_PARAM_NAME = "q";
	static final String QUERY_PARAM_NAME = "%s=%s&%s=%s";
	static final String REVISION_LINE = "Revision";
	static final String STRATEGY_PARAM_NAME = "strategy=";
	static final String TRUST_PARAM_TRUE = "trustServerCertificate=true";
	static final String TRUST_PARAM_FALSE = "trustServerCertificate=false";
	static final String VERSION_ENDPOINT = "/version";
	
	static final String COLUMNS_SCHEME = "columns";
	static final String DOUBLE_DATA_TYPE = "double";
	static final String FLOAT_DATA_TYPE = "float";
	static final String DATATYPE_PROPERTY = "datatype";
	static final String DEFAULT_CATALOG_NAME = "axiCatalog";
	static final String INDEX_PROPERTY = "columnIndex";
	static final String INTEGER_DATA_TYPE = "integer";
	static final String LONG_DATA_TYPE = "long";
	static final String NAME_PROPERTY = "name";
	static final String PUBLISHER_SECTION = "dc:publisher";
	static final String SCHEMA_NAME_PROPERTY = "schema:name";
	static final String SHORT_DATA_TYPE = "short";
	static final String STRING_DATA_TYPE = "string";
	static final String TABLE_PROPERTY = "table";
	static final String TABLE_SCHEMA_SECTION = "tableSchema";
	static final String TITLE_PROPERTY = "titles";
	static final String TIME_STAMP_DATA_TYPE = "xsd:dateTimeStamp";

}