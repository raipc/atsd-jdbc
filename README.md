# JDBC driver

## Usage

```java
try (Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:" + <ATDS_URL>, <ATSD_LOGIN>, <ATSD_PASSWORD>);
	Statement statement = connection.createStatement();) {
    try (ResultSet resultSet = statement.executeQuery(sql);) {
	...
    }
}
```

```java
try (Connection connection = DriverManager.getConnection("jdbc:axibase:atsd:" + <ATDS_URL>, <ATSD_LOGIN>, <ATSD_PASSWORD>);
	PreparedStatement prepareStatement = connection.prepareStatement(sql);
	ResultSet resultSet = prepareStatement.executeQuery();) {
	    ...
}
```

## Building

```bash
$ mvn clean install 
```

## Requirements
Java 1.7 and later
Maven 3


## License
The project is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).