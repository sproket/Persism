package net.sf.persism;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class Reader {

    private static final Log log = Log.getLogger(Reader.class);
    private static final Log blog = Log.getLogger("net.sf.persism.Benchmarks");

    private final MetaData metaData;
    private final Converter converter;

    Reader(Session session) {
        this.metaData = session.getMetaData();
        this.converter = session.getConverter();
    }

    <T> T readObject(T object, Map<String, PropertyInfo> properties, ResultSet rs) throws SQLException, IOException {
        Class<?> objectClass = object.getClass();

        // We should never call this method with a primitive type.
        assert JavaType.getType(objectClass) == null;

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int j = 1; j <= columnCount; j++) {

            String columnName = rsmd.getColumnLabel(j);

            PropertyInfo columnProperty = getPropertyInfo(columnName, properties);

            if (columnProperty != null) {

                if (columnProperty.getter == null) {
                    throw new PersismException(Message.ClassHasNoGetterForProperty.message(object.getClass(), columnProperty.propertyName));
                }

                Class<?> returnType = columnProperty.getter.getReturnType();

                Object value = readColumn(rs, j, rsmd.getColumnType(j), columnName, returnType);

                if (value != null) {
                    try {
                        columnProperty.setValue(object, value);
                    } catch (IllegalArgumentException e) {
                        // A IllegalArgumentException (which is a RuntimeException) occurs if we're setting an unmatched ENUM
                        throw new PersismException(Message.IllegalArgumentReadingColumn.message(columnProperty.propertyName, objectClass, columnName, returnType, value.getClass(), value), e);
                    }
                }
            }
        }

        if (object instanceof Persistable<?> pojo) {
            // Save this object initial state to later detect changed properties
            pojo.saveReadState();
        }
        return (T) object;
    }

    <T> T readRecord(RecordInfo<T> recordInfo, ResultSet rs) throws SQLException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {

        long now;
        now = System.nanoTime();

        ResultSetMetaData rsmd = rs.getMetaData();
        List<Object> constructorParams = new ArrayList<>(recordInfo.propertyInfoByConstructorOrder().keySet().size());

        for (String col : recordInfo.propertyInfoByConstructorOrder().keySet()) {
            Class<?> returnType = recordInfo.propertyInfoByConstructorOrder().get(col).field.getType();

            int ncol = recordInfo.ordinals().get(col);
            Object value = readColumn(rs, ncol, rsmd.getColumnType(ncol), rsmd.getColumnLabel(ncol), returnType);
            if (value == null && returnType.isPrimitive()) {
                // Set null primitives to their default, otherwise the constructor will not be found
                value = JavaType.getDefaultValue(returnType);
            }

            constructorParams.add(value);
        }


        try {
            //noinspection
            return recordInfo.constructor().newInstance(constructorParams.toArray());
        } finally {
            blog.debug("time to get readRecord: %s", (System.nanoTime() - now));
        }
    }


    Object readColumn(ResultSet rs, int column, int sqlColumnType, String columnName, Class<?> returnType) throws SQLException, IOException {
        long now = System.nanoTime();

        if (returnType.isEnum()) {
            // Some DBs may read an enum type as other 1111 - we can tell it here to read it as a string.
            sqlColumnType = java.sql.Types.CHAR;
        }

        JavaType columnType = JavaType.convert(sqlColumnType); // note this could be null if we can't match a type

        Object value = null;

        if (columnType != null) {

            switch (columnType) {

                case BooleanType:
                case booleanType:
                    if (returnType == Boolean.class || returnType == boolean.class) {
                        value = rs.getBoolean(column);
                    } else {
                        value = rs.getByte(column);
                    }
                    break;

                case TimestampType:
                    if (returnType == String.class) { // JTDS
                        value = rs.getString(column);
                    } else {
                        // work around to Oracle reading a oracle.sql.TIMESTAMP class with getObject
                        value = rs.getTimestamp(column);
                    }
                    break;

                case ByteArrayType:
                case byteArrayType:
                    value = rs.getBytes(column);
                    break;

                case ClobType:
                    if (metaData.getConnectionType().supportsReadingFromClobType()) {
                        Clob clob = rs.getClob(column);
                        if (clob != null) {
                            try (InputStream in = clob.getAsciiStream()) {
                                StringWriter writer = new StringWriter();

                                int c = -1;
                                while ((c = in.read()) != -1) {
                                    writer.write(c);
                                }
                                writer.flush();
                                value = writer.toString();
                            }
                        }
                    } else {
                        try (InputStream in = rs.getAsciiStream(column)) {
                            StringWriter writer = new StringWriter();

                            int c = -1;
                            while ((c = in.read()) != -1) {
                                writer.write(c);
                            }
                            writer.flush();
                            value = writer.toString();
                        }
                    }
                    break;

                case BlobType:
                    byte[] buffer = new byte[1024];
                    if (metaData.getConnectionType().supportsReadingFromBlobType()) {
                        Blob blob = rs.getBlob(column);
                        if (blob != null) {
                            try (InputStream in = blob.getBinaryStream()) {
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                for (int len; (len = in.read(buffer)) != -1; ) {
                                    bos.write(buffer, 0, len);
                                }
                                value = bos.toByteArray();
                            }
                        }
                    } else {
                        try (InputStream in = rs.getBinaryStream(column)) {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            for (int len; (len = in.read(buffer)) != -1; ) {
                                bos.write(buffer, 0, len);
                            }
                            value = bos.toByteArray();
                        }
                    }
                    break;

                case IntegerType:
                    // https://github.com/xerial/sqlite-jdbc/issues/604
                    // SQLite jdbc reports INT but the value is LONG for date types which can WRAP past Integer.MAX - Fixed in 3.39.3.0! Thanks!
                    if (metaData.getConnectionType() == ConnectionType.SQLite) {
                        value = rs.getObject(column);
                        if (value != null) {
                            if (value instanceof Long) {
                                value = rs.getLong(column);
                            } else {
                                value = rs.getInt(column);
                            }
                        }
                    } else {
                        value = rs.getObject(column) == null ? null : rs.getInt(column);
                    }
                    break;

                case LongType:
                    value = rs.getObject(column) == null ? null : rs.getLong(column);
                    break;

                case FloatType:
                    value = rs.getObject(column) == null ? null : rs.getFloat(column);
                    break;

                case DoubleType:
                    value = rs.getObject(column) == null ? null : rs.getDouble(column);
                    break;

                case BigIntegerType:
                case BigDecimalType:
                    if (returnType == BigInteger.class) {
                        BigDecimal bd = rs.getBigDecimal(column);
                        if (bd != null) {
                            value = bd.toBigInteger();
                        }
                    } else {
                        value = rs.getBigDecimal(column);
                    }
                    break;

                case TimeType:
                    value = rs.getTime(column);
                    break;

// We can't assume rs.getDate will work. SQLITE actually has a long value in here.
// We can live with rs.getObject and the convert method will handle it.
//                case SQLDateType:
//                case UtilDateType:
//                    value = rs.getDate(column);
//                    break;

                case StringType:
                    if (returnType == Character.class || returnType == char.class) {
                        String s = rs.getString(column);
                        if (s != null && s.length() > 0) {
                            value = s.charAt(0);
                        }
                        break;
                    }

                    value = rs.getString(column);
                    break;

                default:
                    value = rs.getObject(column);
            }

        } else {
            value = rs.getObject(column);
            String objType = "Unknown";
            if (value != null) {
                objType = value.getClass().getName();
            }
            log.warnNoDuplicates(Message.ColumnTypeNotKnownForSQLType.message(sqlColumnType, columnName, objType));
        }

        // If value is null or column type is unknown - no need to try to convert anything.
        if (value != null && columnType != null) {
            value = converter.convert(value, returnType, columnName);
        }

        blog.debug("time to readColumn: %s", (System.nanoTime() - now));
        return value;
    }


    // Poor man's case-insensitive linked hash map ;)
    PropertyInfo getPropertyInfo(String col, Map<String, PropertyInfo> properties) {
        for (String key : properties.keySet()) {
            if (key.equalsIgnoreCase(col)) {
                return properties.get(key);
            }
        }
        return null;
    }

}
