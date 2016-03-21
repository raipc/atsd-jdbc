[![Build Status](https://secure.travis-ci.org/axibase/atsd-jdbc.png?branch=master)](https://travis-ci.org/axibase/atsd-jdbc) 
[![Codacy Badge](https://api.codacy.com/project/badge/grade/ee9af82a9b734fe595f35811c632868e)](https://www.codacy.com/app/alexey-reztsov/atsd-jdbc) 
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
			try (ResultSet resultSet = statement.executeQuery(
			  "SELECT entity, datetime, value, tags.mount_point, tags.file_system FROM df.disk_used_percent WHERE entity = 'NURSWGHBS001' AND datetime > now - 1 * HOUR LIMIT 10");) {
				final ResultSetMetaData rsmd = resultSet.getMetaData();
				System.out.println("\nColumns:");
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String name = rsmd.getColumnName(i);
					String typeName = rsmd.getColumnTypeName(i);
					System.out.println(String.format("\t%s\t%s", typeName, name));
				}
				System.out.println("\nData:");
				int count = 1;
				while (resultSet.next()) {
					System.out.print(count++);
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						int type = rsmd.getColumnType(i);
						switch (type) {
						case Types.VARCHAR:
							System.out.print(" getString: " + resultSet.getString(i));
							break;
						case Types.INTEGER:
							System.out.print(" getInt: " + resultSet.getInt(i));
							break;
						case Types.BIGINT:
							System.out.print(" getLong: " + resultSet.getLong(i));
							break;
						case Types.SMALLINT:
							System.out.print(" getShort: " + resultSet.getShort(i));
							break;
						case Types.FLOAT:
							System.out.print(" getFloat: " + resultSet.getFloat(i));
							break;
						case Types.DOUBLE:
							System.out.print(" getDouble: " + resultSet.getDouble(i));
							break;
						case Types.DECIMAL:
							System.out.print(" getDecimal: " + resultSet.getBigDecimal(i));
							break;
						case Types.TIMESTAMP:
							System.out.print(" getTimestamp: " + resultSet.getTimestamp(i).toString());
							break;
						default:
							throw new UnsupportedOperationException();
						}
					}
					System.out.println("");
				}
				final SQLWarning warnings = resultSet.getWarnings();
				if (warnings != null)
					warnings.printStackTrace();
			}
	}
```

Results:

```
Columns:
1	string				entity
2	xsd:dateTimeStamp	datetime
3	float				value
4	string				tags.mount_point
5	string				tags.file_system

Data:
1  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:35:39.0 	getFloat: 28.0181 	getString: / getString: /dev/md2
2  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:35:45.0 	getFloat: 28.0181 	getString: / getString: /dev/md2
3  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:35:54.0 	getFloat: 28.0181 	getString: / getString: /dev/md2
4  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:36:00.0 	getFloat: 28.0181 	getString: / getString: /dev/md2
5  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:36:09.0 	getFloat: 28.0182 	getString: / getString: /dev/md2
6  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:36:15.0 	getFloat: 28.0182 	getString: / getString: /dev/md2
7  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:36:24.0 	getFloat: 28.0182 	getString: / getString: /dev/md2
8  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:36:30.0 	getFloat: 28.0182 	getString: / getString: /dev/md2
9  getString: nurswghbs001 	getTimestamp: 2016-03-18 13:36:39.0 	getFloat: 28.0183 	getString: / getString: /dev/md2
10 getString: nurswghbs001 	getTimestamp: 2016-03-18 13:36:45.0 	getFloat: 28.0183 	getString: / getString: /dev/md2

```

The following example shows how to extract metadata from a database:

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
			System.out.println("\nTypeInfo:");
			ResultSet rs = metaData.getTypeInfo();
			while (rs.next()) {
				final String name = rs.getString("TYPE_NAME");
				final int type = rs.getInt("DATA_TYPE");
				final int precision = rs.getInt("PRECISION");
				final boolean isCS = rs.getBoolean("CASE_SENSITIVE");
				System.out.println(String.format("\tName: %s      \tCS: %s \tType: %s    \tPrecision: %s", name, isCS, type, precision));
			}
			System.out.println("\nTableTypes:");
			rs = metaData.getTableTypes();
			while (rs.next()) {
				final String type = rs.getString(1);
				System.out.println('\t' + type);
			}
			rs = metaData.getCatalogs();
			while (rs.next()) {
				final String catalog = rs.getString(1);
				System.out.println("\nCatalog: \t" + catalog);
				final ResultSet rs1 = metaData.getSchemas(catalog, null);
				while (rs1.next()) {
					final String schema = rs1.getString(1);
					System.out.println("Schema: \t" + schema);
				}
			}
```

Results:

```
Product Name:   	Axibase
Product Version:	Axibase Time Series Database, <ATSD_EDITION>, Revision: <ATSD_REVISION_NUMBER>
Driver Name:    	ATSD JDBC driver
Driver Version: 	<DRIVER_VERSION>

TypeInfo:
	Name: DECIMAL      	CS: false 	Type: 3    	Precision: -1
	Name: DOUBLE      	CS: false 	Type: 8    	Precision: 52
	Name: FLOAT      	CS: false 	Type: 6    	Precision: 23
	Name: INTEGER      	CS: false 	Type: 4    	Precision: 10
	Name: LONG      	CS: false 	Type: -5    Precision: 19
	Name: SHORT      	CS: false 	Type: 5    	Precision: 5
	Name: STRING      	CS: true 	Type: 12    Precision: 2147483647
	Name: TIMESTAMP     CS: false 	Type: 93    Precision: 23

TableTypes:
	TABLE
	VIEW
	SYSTEM
	
Catalog: 	ATSD
Schema: 	Axibase

```
