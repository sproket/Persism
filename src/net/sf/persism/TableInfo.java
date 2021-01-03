package net.sf.persism;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to hold results of connection.getDatabaseMetaData() result sets as maps
 * so we don't need to repeatedly call it.
 *
 * @author Dan Howard
 * @since 5/5/12 5:26 AM
 */
final class TableInfo {

    /*
     TABLE_CAT String => table catalog (may be null)
     TABLE_SCHEM String => table schema (may be null)
     TABLE_NAME String => table name
     COLUMN_NAME String => column name
     KEY_SEQ short => sequence number within primary key( a value of 1 represents the first column of the primary key, a value of 2 would represent the second column within the primary key).
     PK_NAME String => primary key name (may be null)
     */
    List<Map<String, Object>> primaryKeys = new ArrayList<Map<String, Object>>(4);


    /*
    TABLE_CAT String => table catalog (may be null)
    TABLE_SCHEM String => table schema (may be null)
    TABLE_NAME String => table name
    COLUMN_NAME String => column name
    DATA_TYPE int => SQL type from java.sql.Types
    TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified
    COLUMN_SIZE int => column size.
    BUFFER_LENGTH is not used.
    DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable.
    NUM_PREC_RADIX int => Radix (typically either 10 or 2)
    NULLABLE int => is NULL allowed.
        columnNoNulls - might not allow NULL values
        columnNullable - definitely allows NULL values
        columnNullableUnknown - nullability unknown
    REMARKS String => comment describing column (may be null)
    COLUMN_DEF String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be null)
    SQL_DATA_TYPE int => unused
    SQL_DATETIME_SUB int => unused
    CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column
    ORDINAL_POSITION int => index of column in table (starting at 1)
    IS_NULLABLE String => ISO rules are used to determine the nullability for a column.
        YES --- if the parameter can include NULLs
        NO --- if the parameter cannot include NULLs
        empty string --- if the nullability for the parameter is unknown
    SCOPE_CATLOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF)
    SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF)
    SCOPE_TABLE String => table name that this the scope of a reference attribure (null if the DATA_TYPE isn't REF)
    SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)
    * IS_AUTOINCREMENT String => Indicates whether this column is auto incremented MAY NOT EXIST - DEPENDS ON JDBC VERSION
        YES --- if the column is auto incremented
        NO --- if the column is not auto incremented
        empty string --- if it cannot be determined whether the column is auto incremented parameter is unknown
     */
    List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>(4);

}
