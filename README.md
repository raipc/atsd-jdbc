[![Build Status](https://secure.travis-ci.org/axibase/atsd-jdbc.png?branch=master)](https://travis-ci.org/axibase/atsd-jdbc) 
[![Codacy Badge](https://api.codacy.com/project/badge/grade/ee9af82a9b734fe595f35811c632868e)](https://www.codacy.com/app/alexey-reztsov/atsd-jdbc) 
[![Dependency Status](https://www.versioneye.com/user/projects/56e93b274e714c003625c322/badge.svg)](https://www.versioneye.com/user/projects) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.axibase/atsd-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.axibase/atsd-jdbc)

# JDBC driver

The driver is designed to provide a convenient way to access ATSD instance via SQL API. The internal communication happens by means of transferring CSV data via HTTP or HTTPS protocols. See the [SQL API Documentation](http://axibase.com/atsd/api/#sql) to find a description of the query format, a list of supported SQL functions, and other useful information.

## Compatibility

Product / Date | 2016-03-15 | TBA |
--- | --- | ---
| JDBC Driver  | 1.2.1 | 1.2.2 |
| ATSD Version | 12400 | 12500 |


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
$ mvn clean install -DskipTests=true
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
jdbc:axibase:atsd:http://host.example.com:4567/api/sql;strategy=stream
jdbc:axibase:atsd:https://host.example.com/api/sql;trustServerCertificate=true;strategy=file
```

## License

The project is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).

## Requirements

* Java 1.7 and later

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

## Database Capabilities

| Feature | Value |
|:--------|:------|
| All Procedures Are Callable |  false  |
| All Tables Are Selectable |  false  |
| Auto Commit Failure Closes All Result Sets |  false  |
| Catalog Separator |  .  |
| Catalog Term |  catalog  |
| Database Major Version |  12500  |
| Database Minor Version |  0  |
| Data Definition Causes Transaction Commit |  false  |
| Data Definition Ignored In Transactions |  false  |
| Default Transaction Isolation |  0  |
| Does Max Row Size Include Blobs |  false  |
| Driver Major Version |  1  |
| Driver Minor Version |  2  |
| Extra Name Characters |    |
| Generated Key Always Returned |  false  |
| Identifier Quote String |  "  |
| Is Catalog At Start |  false  |
| Is Read Only |  true  |
| JDBCMajor Version |  4  |
| JDBCMinor Version |  1  |
| Locators Update Copy |  false  |
| Max Binary Literal Length |  0  |
| Max Catalog Name Length |  0  |
| Max Char Literal Length |  0  |
| Max Column Name Length |  0  |
| Max Columns In Group By |  0  |
| Max Columns In Index |  0  |
| Max Columns In Order By |  0  |
| Max Columns In Select |  0  |
| Max Columns In Table |  0  |
| Max Connections |  0  |
| Max Cursor Name Length |  0  |
| Max Index Length |  0  |
| Max Procedure Name Length |  0  |
| Max Row Size |  0  |
| Max Schema Name Length |  0  |
| Max Statement Length |  0  |
| Max Statements |  0  |
| Max Table Name Length |  0  |
| Max Tables In Select |  0  |
| Max User Name Length |  0  |
| Null Plus Non Null Is Null |  true  |
| Nulls Are Sorted At End |  true  |
| Nulls Are Sorted At Start |  false  |
| Nulls Are Sorted High |  false  |
| Nulls Are Sorted Low |  false  |
| Procedure Term |  procedure  |
| Result Set Holdability |  1  |
| Schema Term |  schema  |
| Search String Escape |  \  |
| SQL State Type |  2  |
| Stores Lower Case Identifiers |  true  |
| Stores Lower Case Quoted Identifiers |  true  |
| Stores Mixed Case Identifiers |  false  |
| Stores Mixed Case Quoted Identifiers |  false  |
| Stores Upper Case Identifiers |  false  |
| Stores Upper Case Quoted Identifiers |  false  |
| Supports Alter Table With Add Column |  false  |
| Supports Alter Table With Drop Column |  false  |
| Supports ANSI92 Entry Level SQL |  false  |
| Supports ANSI92 Full SQL |  false  |
| Supports ANSI92 Intermediate SQL |  false  |
| Supports Batch Updates |  false  |
| Supports Catalogs In Data Manipulation |  false  |
| Supports Catalogs In Index Definitions |  false  |
| Supports Catalogs In Privilege Definitions |  false  |
| Supports Catalogs In Procedure Calls |  false  |
| Supports Catalogs In Table Definitions |  false  |
| Supports Column Aliasing |  true  |
| Supports Convert |  false  |
| Supports Core SQLGrammar |  false  |
| Supports Correlated Subqueries |  false  |
| Supports Data Definition And Data Manipulation Transactions |  false  |
| Supports Data Manipulation Transactions Only |  true  |
| Supports Different Table Correlation Names |  false  |
| Supports Expressions In Order By |  true  |
| Supports Extended SQLGrammar |  false  |
| Supports Full Outer Joins |  true  |
| Supports Get Generated Keys |  false  |
| Supports Group By |  true  |
| Supports Group By Beyond Select |  true  |
| Supports Group By Unrelated |  true  |
| Supports Integrity Enhancement Facility |  false
| Supports Like Escape Clause |  true  |
| Supports Limited Outer Joins |  true  |
| Supports Minimum SQLGrammar |  false  |
| Supports Mixed Case Identifiers |  true  |
| Supports Mixed Case Quoted Identifiers |  true
| Supports Multiple Open Results |  false  |
| Supports Multiple Result Sets |  false  |
| Supports Multiple Transactions |  false  |
| Supports Named Parameters |  false  |
| Supports Non Nullable Columns |  true  |
| Supports Open Cursors Across Commit |  false  |
| Supports Open Cursors Across Rollback |  false  |
| Supports Open Statements Across Commit |  false  |
| Supports Open Statements Across Rollback |  false  |
| Supports Order By Unrelated |  true  |
| Supports Outer Joins |  true  |
| Supports Positioned Delete |  false  |
| Supports Positioned Update |  false  |
| Supports Savepoints |  false  |
| Supports Schemas In Data Manipulation |  false  |
| Supports Schemas In Index Definitions |  false  |
| Supports Schemas In Privilege Definitions |  false  |
| Supports Schemas In Procedure Calls |  false  |
| Supports Schemas In Table Definitions |  false  |
| Supports Select For Update |  false  |
| Supports Statement Pooling |  false  |
| Supports Stored Functions Using Call Syntax |  false  |
| Supports Stored Procedures |  false  |
| Supports Subqueries In Comparisons |  false  |
| Supports Subqueries In Exists |  false  |
| Supports Subqueries In Ins |  false  |
| Supports Subqueries In Quantifieds |  false  |
| Supports Table Correlation Names |  false  |
| Supports Transactions |  false  |
| Supports Union |  false  |
| Supports Union All |  false  |
| Uses Local File Per Table |  false  |
| Uses Local Files |  false  |

## Usage

First, make sure your ATSD instance is started and you have valid credentials to it. In general to create SQL statement you can use the usual Java approach:

```java

	Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:" + 
		<ATDS_URL>, <ATSD_LOGIN>, <ATSD_PASSWORD>);
	Statement statement = connection.createStatement();
	ResultSet resultSet = statement.executeQuery(<SQL_QUERY>);

```

You can the same approach to create a prepared statement:

```java

	Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:" + 
		<ATDS_URL>, <ATSD_LOGIN>, <ATSD_PASSWORD>);
	PreparedStatement prepareStatement = connection.prepareStatement(<SQL_QUERY>);
	ResultSet resultSet = prepareStatement.executeQuery();

}
```

Please note that the current version of the driver has limitations. Users do not have permissions to change the data source. It is possible to iterate over records one by one. No positioning is supported yet but this option may be added later. To check how the driver works, run the following example:

```java

	Class.forName("com.axibase.tsd.driver.jdbc.AtsdDriver");
	
	String url = "jdbc:axibase:atsd:https://10.102.0.6:8443/api/sql";
	String query = "SELECT entity, datetime, value, tags.mount_point, tags.file_system "
		+ "FROM df.disk_used_percent WHERE entity = 'NURSWGHBS001' AND datetime > now - 1 * HOUR LIMIT 10";
		
	try (Connection connection = DriverManager.getConnection(url, "axibase", "axibase");
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(query);) {
		
			int rowNumber = 1;
			while (resultSet.next()) {
				System.out.print(rowNumber++);
				System.out.print("\tentity = " + resultSet.getString("entity"));
				System.out.print("\tdatetime = " + resultSet.getTimestamp("datetime").toString());
				System.out.print("\tvalue = " + resultSet.getString("value"));
				System.out.print("\ttags.mount_point = " + resultSet.getString("tags.mount_point"));
				System.out.println("\ttags.file_system = " + resultSet.getString("tags.file_system"));
			}
			
			final SQLWarning warnings = resultSet.getWarnings();
			if (warnings != null)
				warnings.printStackTrace();
	}
	
```

Results:

```

 1 entity = nurswghbs001 datetime = 2016-03-22 12:52:03.0 value = 28.8116 tags.mount_point = / tags.file_system = /dev/md2
 2 entity = nurswghbs001 datetime = 2016-03-22 12:52:04.0 value = 28.8116 tags.mount_point = / tags.file_system = /dev/md2
 3 entity = nurswghbs001 datetime = 2016-03-22 12:52:18.0 value = 28.8116 tags.mount_point = / tags.file_system = /dev/md2
 4 entity = nurswghbs001 datetime = 2016-03-22 12:52:19.0 value = 28.8116 tags.mount_point = / tags.file_system = /dev/md2
 5 entity = nurswghbs001 datetime = 2016-03-22 12:52:33.0 value = 28.8117 tags.mount_point = / tags.file_system = /dev/md2
 6 entity = nurswghbs001 datetime = 2016-03-22 12:52:34.0 value = 28.8117 tags.mount_point = / tags.file_system = /dev/md2
 7 entity = nurswghbs001 datetime = 2016-03-22 12:52:48.0 value = 28.8117 tags.mount_point = / tags.file_system = /dev/md2
 8 entity = nurswghbs001 datetime = 2016-03-22 12:52:49.0 value = 28.8117 tags.mount_point = / tags.file_system = /dev/md2
 9 entity = nurswghbs001 datetime = 2016-03-22 12:53:03.0 value = 28.8117 tags.mount_point = / tags.file_system = /dev/md2
10 entity = nurswghbs001 datetime = 2016-03-22 12:53:04.0 value = 28.8117 tags.mount_point = / tags.file_system = /dev/md2

```

The following example shows how to extract metadata from a database:

```java

	Class.forName("com.axibase.tsd.driver.jdbc.AtsdDriver");
	
	String url = "jdbc:axibase:atsd:https://10.102.0.6:8443/api/sql";
	
	try (Connection connection = DriverManager.getConnection(url, "axibase", "axibase");
		Statement statement = connection.createStatement();) {
		
		DatabaseMetaData metaData = connection.getMetaData();
		String databaseProductName = metaData.getDatabaseProductName();
		String databaseProductVersion = metaData.getDatabaseProductVersion();
		String driverName = metaData.getDriverName();
		String driverVersion = metaData.getDriverVersion();
		System.out.println("Product Name:   \t" + databaseProductName);
		System.out.println("Product Version:\t" + databaseProductVersion);
		System.out.println("Driver Name:    \t" + driverName);
		System.out.println("Driver Version: \t" + driverVersion);
		System.out.println("\nTypeInfo:");
		
		ResultSet rs = metaData.getTypeInfo();
		while (rs.next()) {
			String name = rs.getString("TYPE_NAME");
			int type = rs.getInt("DATA_TYPE");
			int precision = rs.getInt("PRECISION");
			boolean isCS = rs.getBoolean("CASE_SENSITIVE");
			System.out.println(String.format(
				"\tName:%s \tCS: %s \tType: %s \tPrecision: %s", name, isCS, type, precision));
		}
		System.out.println("\nTableTypes:");
		
		rs = metaData.getTableTypes();
		while (rs.next()) {
			String type = rs.getString(1);
			System.out.println('\t' + type);
		}
		rs = metaData.getCatalogs();
		
		while (rs.next()) {
			String catalog = rs.getString(1);
			System.out.println("\nCatalog: \t" + catalog);
			ResultSet rs1 = metaData.getSchemas(catalog, null);
			while (rs1.next()) {
				String schema = rs1.getString(1);
				System.out.println("Schema: \t" + schema);
			}
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
