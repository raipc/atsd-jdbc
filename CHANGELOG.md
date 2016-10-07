## 1.2.13
* `ResultSet#getRow` now counts rows starting with 1 instead of 0.

## 1.2.12
* Implemented `stream` fetching strategy. Old `stream` strategy renamed to `memory`.
* Better support for screened values.
* ResultSet.getObject() returns `null` for absent tags.
* `SQLFeatureNotSupportedException` is thrown while trying to create not forward-only result set.

## 1.2.11
* Older ATSD versions support (13919 and less)

## 1.2.10
* Fixed date_format function result representation.
* Fixed Exception throw when value in first column starts with a quote.
* ATSD v.14126 support.

## 1.2.9-RELEASE
* Fixed PreparedStatement.setMaxRows behaviour.
* SQLException is thrown on ResultSet object creation, not invocating ResultSet.next().
* Fixed database metadata representation.

## 1.2.8
* Time expression support implemented. Use AtsdPreparedStatement.setTimeExpression(String expression) method to validate string as time expression.
* Better errors description.
* Fixed ClassCastException on ResultSet.getFloat().
* Fixed bigint and smallint datatypes treating as strings.
* Performance improvements of PreparedStatement.
* Support for long column schema.
* ATSD v.14049 support.
* connectTimeout is 5 seconds by default.
* Default displaySize for varchar type is 10 kB.

## 1.2.7
* Standard SQL types are displayed in driver instead of original types from ATSD.
* Statement.setQueryTimeout() implemented.
* Statement.setMaxRows() implemented.
* Added connectTimeout and readTimeout options in connection string
* Error messages from /sql/api are handled.
* No retry after erroneous query.
