## 1.2.17
* Updated dependencies
* Improved performance

## 1.2.16
* Added support for ATSD `java_object` type.
* Improved performance.
* Driver correctly recognizes NULL and empty values as of ATSD version 14540.

## 1.2.15
* Fixed generation of database metadata result sets. Better support of DbVis 9.2.
* The `Connection#getCatalog` method is implemented.
* Fixed NPE in `AtsdResultSet#close` for constant result sets.
* Implemented the `Statement#cancel` method. As of ATSD revision 14451, `Statement#cancel` will force ATSD to cancel the executed query. Older ATSD revisions release drivers break the connection. Manual query cancellation may be needed.

## 1.2.14
* Better support for GUI tools working with custom databases.

## 1.2.13
* `ResultSet#getRow` now counts rows starting with 1 instead of 0.

## 1.2.12
* Implemented the `stream` fetching strategy. The old `stream` strategy is renamed to `memory`.
* Better support for screened values.
* `ResultSet.getObject()` returns `null` for absent tags.
* `SQLFeatureNotSupportedException` is thrown while trying to create not forward-only result set.

## 1.2.11
* Older ATSD versions support (13919 and less).

## 1.2.10
* Fixed the `date_format` function result representation.
* Fixed the `Exception` throw when the value in first column starts with a quote.
* ATSD v.14126 support.

## 1.2.9-RELEASE
* Fixed the `PreparedStatement.setMaxRows` behavior.
* `SQLException` is thrown on a `ResultSet` object creation, not invocating `ResultSet.next()`.
* Fixed database metadata representation.

## 1.2.8
* Time expression support implemented. Use the `AtsdPreparedStatement.setTimeExpression(String expression)` method to validate the string as a time expression.
* Better errors description.
* Fixed `ClassCastException` on `ResultSet.getFloat()`.
* Changed `bigint` and `smallint` datatypes to be treated as strings.
* Performance improvements of `PreparedStatement`.
* Support for long column schema.
* ATSD v.14049 support.
* `connectTimeout` is 5 seconds by default.
* Default `displaySize` for varchar type is 10 kB.

## 1.2.7
* Standard SQL types are displayed in driver instead of original types from ATSD.
* `Statement.setQueryTimeout()` implemented.
* `Statement.setMaxRows()` implemented.
* Added `connectTimeout` and `readTimeout` options in the connection string.
* Error messages from `/sql/api` are handled.
* No retry after erroneous query.
