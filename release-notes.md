## Release Notes

### 1.0.0 

Initial release

### 1.0.1

* Added support for UUID as generated key for PostgreSQL 
* Added support for UUID for other supported DBs (mapping to String or byte array)
* Added support for sql.Time, LocalTime, LocalDate, LocalDateTime
* Added support for BigInteger
* Added support for MSSQL/JTDS money and smallmoney types (mapping to Float, Double or BigDecimal)
* Added support for ENUM type in the db - mapping to Java enum (PostgreSQL, MySQL, H2)  
* Added warning when using a primitive type mapped to a column with a default in the database
* Added AutoClosable implementation to Session  
* Updated test mssql jdbc driver to 8.4.1
* Updated test H2 jdbc driver to 1.4.200
* Removed null waring about sql type 1111 (Other) will just be considered Object
* Fixed java.lang.UnsupportedOperationException occurring if you have LocalDateTime and DATE (not time) type in the DB
* Fixed missing Derby keyword delimiters
* Fixed issue where objects using Persistable interface would have all columns updated in some cases






