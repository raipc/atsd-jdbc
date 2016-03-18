[![Build Status](https://secure.travis-ci.org/axibase/atsd-jdbc.png?branch=master)](https://travis-ci.org/axibase/atsd-jdbc) 
[![Codacy Badge](https://api.codacy.com/project/badge/grade/ee9af82a9b734fe595f35811c632868e)](https://www.codacy.com/app/alexey-reztsov/atsd-jdbc) 
[![Coverage Status](https://coveralls.io/repos/github/axibase/atsd-jdbc/badge.svg?branch=master)](https://coveralls.io/github/axibase/atsd-jdbc?branch=master) 
[![Dependency Status](https://www.versioneye.com/user/projects/56e93b274e714c003625c322/badge.svg)](https://www.versioneye.com/user/projects) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.axibase/atsd-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.axibase/atsd-jdbc)

# JDBC driver

The driver is designed to provide a convenient way to access ATSD instance via SQL API. The internal communication happens by means of transferring CSV data via HTTP or HTTPS protocols. See the [SQL API Documentation](http://axibase.com/atsd/api/#sql) to find a description of the query format, a list of supported SQL functions, and other useful information.

## Supported Data Types

| TYPE NAME | CASE SENSITIVE | DATA TYPE | PRECISION  |
|:---------:|---------------:|----------:|-----------:|
| DECIMAL | false | 3 | -1 |
| DOUBLE | false | 8 | 52 |
| FLOAT | false | 6 | 23 |
| INTEGER | false | 4 | 10 |
| LONG | false | -5 | 19 |
| SHORT | false | 5 | 5 |
| STRING | true  | 12 | 2147483647 |
| TIMESTAMP | false | 93 | 23 |

## JDBC Connection Properties Supported by Driver

Property Name | Valid Values | Default
--- | --- | ---
trustServerCertificate | true, false | `false`
strategy | file, stream | `stream`

## Apache Maven

You can find the project in the central repository.

```xml
<dependency>
    <groupId>com.axibase</groupId>
    <artifactId>atsd-jdbc</artifactId>
    <version>1.2.1</version>
</dependency>
```

Alternatively, you can build a project yourself.

```bash
$ mvn -DskipTests=true clean install
```

## Classpath

If you do not use any build managers such as Gradle or Maven, you can get a JAR library from Maven Central: [Direct URL](http://search.maven.org/remotecontent?filepath=com/axibase/atsd-jdbc/1.2.1/atsd-jdbc-1.2.1.jar) and add it to the classpath of your application.

```
* Unix: java -cp "atsd-jdbc-1.2.1.jar:lib/*" your.package.MainClass
* Windows java -cp "atsd-jdbc-1.2.1.jar;lib/*" your.package.MainClass
```

## Database Tools

You can also use a universal database manager, for example [DbVisualizer](https://www.dbvis.com). Follow instructions in their user guide to create a custom driver based on JAR file from the link above.

## JDBC URL

A prefix of the JDBC driver is "jdbc:axibase:atsd:". Next you should specify a URL where your ATSD instance is installed. And, if necessary, specify JDBC Connection properties listed above. By combining all three segments together you can get a full JDBC URL and use it to set a connection.

```
Examples:

jdbc:axibase:atsd:http://host.example.com/api/sql
jdbc:axibase:atsd:http://host.example.com|:4567/api/sql;strategy=stream
jdbc:axibase:atsd:https://host.example.com/api/sql;trustServerCertificate=true;strategy=file
```

## License

The project is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Requirements

* Java 1.7 and later

## Tests

To run tests, you have to choose (or create) your own ATSD metrics. A test phase requires a set of test properties listed below. The first three parameters are mandatory. You can use the rest of the parameters to get more accurate test results.

```
* -Daxibase.tsd.driver.jdbc.url=<ATSD_URL [http, https]>
* -Daxibase.tsd.driver.jdbc.username=<ATSD_LOGIN> 
* -Daxibase.tsd.driver.jdbc.password=<ATSD_PASSWORD> 
* -Daxibase.tsd.driver.jdbc.metric.tiny=<METRIC_NAME> 
* -Daxibase.tsd.driver.jdbc.metric.small=<METRIC_NAME>
* -Daxibase.tsd.driver.jdbc.metric.medium=<METRIC_NAME>
* -Daxibase.tsd.driver.jdbc.metric.large=<METRIC_NAME>
* -Daxibase.tsd.driver.jdbc.metric.huge=<METRIC_NAME>
* -Daxibase.tsd.driver.jdbc.metric.jumbo=<METRIC_NAME>
* -Daxibase.tsd.driver.jdbc.metric.wrong=<METRIC_NAME_THROWING_SQL_EXCEPTION>
* -Daxibase.tsd.driver.jdbc.metric.concurrent=<SEVERAL_COMMA_SEPARATED_METRIC_NAMES>
* -Daxibase.tsd.driver.jdbc.trust=<IGNORE_CERTIFICATES> 
* -Daxibase.tsd.driver.jdbc.strategy=<STORE_STRATEGY [file,stream]>
```


## Usage

First, make sure your ATSD instance is started and you have valid credentials to it. In general to create SQL statement you can use the usual Java approach:

```java
Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:" + <ATDS_URL>, <ATSD_LOGIN>, <ATSD_PASSWORD>);
Statement statement = connection.createStatement();
ResultSet resultSet = statement.executeQuery(<SQL_QUERY>);
```

You can the same approach to create a prepared statement:

```java
Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:" + <ATDS_URL>, <ATSD_LOGIN>, <ATSD_PASSWORD>);
PreparedStatement prepareStatement = connection.prepareStatement(<SQL_QUERY>);
ResultSet resultSet = prepareStatement.executeQuery();
	    ...
}
```

Please note that the current version of the driver has limitations. Users do not have permissions to change the data source. It is possible to iterate over records one by one. No positioning is supported yet but this option may be added later. To check how the driver works, run the following example:

```java
	Class.forName("com.axibase.tsd.driver.jdbc.AtsdDriver");
	try (Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:<PROTOCOL>://<HOST>.<DOMAIN>:<PORT>/api/sql", <ATSD_LOGIN>, <ATSD_PASSWORD>); 
			Statement statement = connection.createStatement();) {
		final DatabaseMetaData metaData = connection.getMetaData();
		final String databaseProductName = metaData.getDatabaseProductName();
		final String databaseProductVersion = metaData.getDatabaseProductVersion();
		final String driverName = metaData.getDriverName();
		final String driverVersion = metaData.getDriverVersion();
		System.out.println("Product Name:   \t" + databaseProductName);
		System.out.println("Product Version:\t" + databaseProductVersion);
		System.out.println("Driver Name:    \t" + driverName);
		System.out.println("Driver Version: \t" + driverVersion);
		System.out.println("\nTableTypes:")
		final ResultSet rs = metaData.getCatalogs();
		while (rs.next()) {
			final String catalog = rs.getString(1);
			System.out.println("\nCatalog: \t" + catalog);
			final ResultSet rs1 = metaData.getSchemas(catalog, null);
			while (rs1.next()) {
				final String schema = rs1.getString(1);
				System.out.println("Schema: \t" + schema);
			}
		};
		final ResultSet rs2 = metaData.getTableTypes();
		while (rs2.next()) {
			final String type = rs2.getString(1);
			System.out.println('\t' + type);
		}
		try (ResultSet resultSet = statement.executeQuery("SELECT * from <METRIC_NAME> LIMIT 100");) {
			final ResultSetMetaData rsmd = resultSet.getMetaData();
			System.out.println("\nColumns:");
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String name = rsmd.getColumnName(i);
				String typeName = rsmd.getColumnTypeName(i);
				System.out.println(String.format("\t%s\t%s", typeName, name));
			}
			System.out.println("\nData:");
			int count = 1;
			final StringBuilder sb = new StringBuilder();
			while (resultSet.next()) {
				sb.append(count++).append(" ");
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					int type = rsmd.getColumnType(i);
					switch (type) {
					case Types.VARCHAR:
						sb.append(" getString: " + resultSet.getString(i));
						break;
					case Types.INTEGER:
						sb.append(" getInt: " + resultSet.getInt(i));
						break;
					case Types.BIGINT:
						sb.append(" getLong: " + resultSet.getLong(i));
						break;
					case Types.SMALLINT:
						sb.append(" getShort: " + resultSet.getShort(i));
						break;
					case Types.FLOAT:
						sb.append(" getFloat: " + resultSet.getFloat(i));
						break;
					case Types.DOUBLE:
						sb.append(" getDouble: " + resultSet.getDouble(i));
						break;
					case Types.DECIMAL:
						sb.append("getDecimal: " + resultSet.getBigDecimal(i));
						break;							
					case Types.TIMESTAMP:
						sb.append(" getTimestamp: " + resultSet.getTimestamp(i).toString());
						break;
					default:
						throw new UnsupportedOperationException();
					}
				}
				sb.append('\n');
			}
			System.out.println(sb.toString());
			final SQLWarning warnings = resultSet.getWarnings();
			if (warnings != null)
				warnings.printStackTrace();
		}
	}
```

Results:

```
Product Name:   	Axibase
Product Version:	Axibase Time Series Database, <ATSD_EDITION>, Revision: <ATSD_REVISION_NUMBER>
Driver Name:    	ATSD JDBC driver
Driver Version: 	<DRIVER_VERSION>
	
TableTypes:
	TABLE
	VIEW
	SYSTEM
	
Catalog: 	ATSD
Schema: 	Axibase

Columns:
	...
	
Data:
	...

```
