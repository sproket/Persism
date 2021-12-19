package net.sf.persism;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Information about columns used for insert, update and delete.
 * Queries do not use this object.
 *
 * @author Dan Howard
 * @since 5/4/12 6:22 AM
 */
final class ColumnInfo implements Convertable {

    String columnName;

    // SQLite - Date - comes back as StringType
    // H2 - BIT - comes back NULL
    Types columnType;

    // kept for possible future use
    int sqlColumnType;
    String sqlColumnTypeName;

    // indicates this column is generated. Only for Auto-Inc for now
    boolean autoIncrement;

    // Indicates this is primary key column
    boolean primary;

    boolean hasDefault;

    int length; // for string to varchar length checking

    Function converter;
    String converterName;

    @Override
    public void setConverter(Function<?, ?> func, String name) {
        // new Throwable().printStackTrace();
        converter = func;
        converterName = name;
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "columnName='" + columnName + '\'' +
                ", columnType=" + columnType +
                ", sqlColumnType=" + sqlColumnType +
                ", sqlColumnTypeName=" + sqlColumnTypeName +
                ", autoIncrement=" + autoIncrement +
                ", primary=" + primary +
                ", hasDefault=" + hasDefault +
                ", length=" + length +
                ", converterName=" + converterName +
                '}';
    }
}
