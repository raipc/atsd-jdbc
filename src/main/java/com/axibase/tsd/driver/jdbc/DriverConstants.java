package com.axibase.tsd.driver.jdbc;

public final class DriverConstants {
	public static final String DATABASE_PRODUCT_NAME = "Axibase";
	public static final String DATABASE_PRODUCT_VERSION = "Axibase Time Series Database";
	public static final String JDBC_DRIVER_NAME = "ATSD JDBC driver";
	public static final String JDBC_DRIVER_VERSION_DEFAULT = "1.2.15";
	public static final int    DRIVER_VERSION_MAJOR_DEFAULT = 1;
	public static final int    DRIVER_VERSION_MINOR_DEFAULT = 2;

	public static final String PRODUCT_NAME_KEY = "product.name";
	public static final String PRODUCT_VERSION_KEY = "product.version";
	public static final String DRIVER_VERSION_KEY = "driver.version";
	public static final String DRIVER_NAME_KEY = "driver.name";
	public static final String DATABASE_VERSION_MAJOR_KEY = "database.version.major";
	public static final String DATABASE_VERSION_MINOR_KEY = "database.version.minor";
	public static final String DRIVER_VERSION_MAJOR_KEY = "driver.version.major";
	public static final String DRIVER_VERSION_MINOR_KEY = "driver.version.minor";
	public static final String DRIVER_PROPERTIES = "META-INF/axibase-atsd-jdbc.properties";
	public static final String RETRIES_NUMBER = "1";

	public static final String CONNECT_URL_PREFIX = "jdbc:axibase:atsd:";
	public static final String PARAM_SEPARATOR = ";";
	public static final String PROTOCOL_SEPARATOR = "://";
	public static final String CONNECT_TIMEOUT_PARAM = "connectTimeout";
	public static final int    DEFAULT_CONNECT_TIMEOUT_VALUE = 5;
	public static final String READ_TIMEOUT_PARAM = "readTimeout";
	public static final int    DEFAULT_READ_TIMEOUT_VALUE = 0;
	public static final String TRUST_PARAM_NAME = "trustServerCertificate";
	public static final boolean DEFAULT_TRUST_SERVER_CERTIFICATE = false;

	public static final String QUERY_ID_PARAM_NAME = "queryId";
	public static final String Q_PARAM_NAME = "q";
	public static final String STRATEGY_PARAM_NAME = "strategy";
	public static final String FORMAT_PARAM_NAME = "outputFormat";
	public static final String FORMAT_PARAM_VALUE = "csv";
	public static final String METADATA_FORMAT_PARAM_NAME = "metadataFormat";
	public static final String LIMIT_PARAM_NAME = "limit";

	public static final int    ATSD_VERSION_SUPPORTING_BODY_METADATA = 13919;
	public static final int    ATSD_VERSION_COMPRESSED_ERRORS = 14185;
	public static final String REVISION_LINE = "Revision";

	public static final String VERSION_ENDPOINT = "/api/v1/version";
	public static final String CANCEL_METHOD = "/cancel";
	public static final String CONN_KEEP_ALIVE = "Keep-Alive";
	public static final String AUTHORIZATION_TYPE = "Basic ";
	public static final String CSV_MIME_TYPE = "text/csv";
	public static final String COMPRESSION_ENCODING = "gzip";
	public static final String DEFAULT_ENCODING = "identity";
	public static final String FORM_URLENCODED_TYPE = "application/x-www-form-urlencoded";
	public static final String USER_AGENT = "ATSD Client/1.0 axibase.com";

	public static final String DATATYPE_PROPERTY = "datatype";
	public static final String DEFAULT_CATALOG_NAME = "atsd";
	public static final String DEFAULT_TABLE_NAME = "atsd_series";

	public static final String COLUMNS_SCHEME = "columns";
	public static final String INDEX_PROPERTY = "columnIndex";
	public static final String NAME_PROPERTY = "name";
	public static final String PUBLISHER_SECTION = "dc:publisher";
	public static final String SCHEMA_NAME_PROPERTY = "schema:name";
	public static final String TABLE_PROPERTY = "table";
	public static final String TABLE_SCHEMA_SECTION = "tableSchema";
	public static final String TITLE_PROPERTY = "titles";

	private DriverConstants(){}
}
