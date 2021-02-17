## Release Notes

### 1.0.0 

Initial release

### 1.0.1

* Removed null waring about sql type 1111 (Other) will just be considered Object
* Added UUID support as generated key for PostgreSQL 
* Added UUID support for other supported DBs (mapping to String or byte array)
* Added warning when using a primitive type mapped to a column with a default in the database
* Added support for sql.Time, LocalTime, LocalDate, LocalDateTime
* Added support for BigInteger
* Added support for MSSQL/JTDS money and smallmoney types (mapping to Float or Double)  
* Fixed java.lang.UnsupportedOperationException occurring if you have LocalDateTime and DATE (not time) type in the DB.
* Fixed missing Derby keyword delimiters
* Fixed issue where objects using Persistable interface would have all columns updated in some cases.
* Updated test mssql jdbc driver to 8.4.1






