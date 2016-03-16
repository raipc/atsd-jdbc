[![Build Status](https://secure.travis-ci.org/axibase/atsd-jdbc.png?branch=master)](https://travis-ci.org/axibase/atsd-jdbc) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.axibase/atsd-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.axibase/atsd-jdbc) [![Dependency Status](https://www.versioneye.com/user/projects/56e93b274e714c003625c322/badge.svg)](https://www.versioneye.com/user/projects)

# JDBC driver

The driver is designed to provide more convenient working with ATSD via SQL API. The internal communication occurs by means of transferring CSV data via HTTP/HTTPS protocols according to [SQL API Documentation](http://axibase.com/atsd/api/#sql). You can find there as the description of the query format and the list of supported SQL functions.

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

## Building

```bash
$ mvn -DskipTests=true clean install
```

## Classpath

For using in Java projects without build managers like Gradle or Maven you can get a JAR library from Maven Central: [Direct URL](http://search.maven.org/remotecontent?filepath=com/axibase/atsd-jdbc/1.2.1/atsd-jdbc-1.2.1.jar) and add it to the classpath of your application.

```
* Unix: java -cp "atsd-jdbc-1.2.1.jar:lib/*" your.package.MainClass
* Windows java -cp "atsd-jdbc-1.2.1.jar;lib/*" your.package.MainClass
```

## Database Tools

On the other hand you can use an universal database manager like [DbVisualizer](https://www.dbvis.com) following appropriate user guide in order to create a custom driver based on JAR file from the link above.

## JDBC URL

A prefix of the JDBC driver is "jdbc:axibase:atsd:". Next, you should specify a URL where your ATSD instance is available. And if it is necessary you should specify some JDBC Connection Properties listed above. By combining all three segments together, you can get a full JDBC url and use it for getting connection.

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

In order to to run tests you will have to choose (or create) own ATSD metrics to deal with it. It is expected the next set of test properies:
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
The first three parameters are mandatory to have tests done and the others are optional for more accurate checking.

## Usage
To get started you should have ATSD instance started and valid credentials to deal with it. In general to create SQL statement you can use the usual java approach like:

```java
Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:" + <ATDS_URL>, <ATSD_LOGIN>, <ATSD_PASSWORD>);
Statement statement = connection.createStatement();
ResultSet resultSet = statement.executeQuery(<SQL_QUERY>);
```

or the same to create a prepared statement:

```java
Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:" + <ATDS_URL>, <ATSD_LOGIN>, <ATSD_PASSWORD>);
PreparedStatement prepareStatement = connection.prepareStatement(<SQL_QUERY>);
ResultSet resultSet = prepareStatement.executeQuery();
	    ...
}
```

Please note that the current version of the driver has some limitations. It is made read-only by not allowing users to change the data source. It is possible to iterate in one direction only one by one. No positioning is supported yet but would be added later.

In order to check a basic way of using driver you could run the next simple example:

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
