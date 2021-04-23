package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

final class Reader {

    private static final Log log = Log.getLogger(Reader.class);

    private Connection connection;
    private MetaData metaData;
    private Convertor convertor;

    Reader(Session session) {
        this.connection = session.getConnection();
        this.metaData = session.getMetaData();
        this.convertor = session.getConvertor();
    }

    <T> T readObject(Object object, ResultSet rs) throws IllegalAccessException, SQLException, InvocationTargetException, IOException {

        Class<?> objectClass = object.getClass();
        // We should never call this method with a primitive type.
        assert Types.getType(objectClass) == null;

        Map<String, PropertyInfo> properties;
        if (objectClass.getAnnotation(NotTable.class) == null) {
            properties = metaData.getTableColumnsPropertyInfo(objectClass, connection);
        } else {
            properties = metaData.getQueryColumnsPropertyInfo(objectClass, rs);
        }

        // Test if all properties have column mapping and throw PersismException if not
        // This block verifies that the object is fully initialized.
        // Any properties not marked by NotColumn should have been set (or if they have a getter only)
        // If not throw a PersismException
        Collection<PropertyInfo> allProperties = MetaData.getPropertyInfo(objectClass);
        if (properties.values().size() < allProperties.size()) {
            Set<PropertyInfo> missing = new HashSet<>(allProperties.size());
            missing.addAll(allProperties);
            missing.removeAll(properties.values());

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (PropertyInfo prop : missing) {
                sb.append(sep).append(prop.propertyName);
                sep = ",";
            }

            throw new PersismException("Object " + objectClass + " was not properly initialized. Some properties not initialized in the queried columns (" + sb + ").");
        }

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<String> foundColumns = new ArrayList<>(columnCount);

        for (int j = 1; j <= columnCount; j++) {

            String columnName = rsmd.getColumnLabel(j);
            PropertyInfo columnProperty = properties.get(columnName);

            if (columnProperty != null) {
                Class<?> returnType = columnProperty.getter.getReturnType();

                Object value = readColumn(rs, j, returnType);

                foundColumns.add(columnName);

                if (value != null) {
                    try {
                        if (columnProperty.readOnly) {
                            // set the value on the field directly
                            columnProperty.field.setAccessible(true);
                            columnProperty.field.set(object, value);
                            columnProperty.field.setAccessible(false);
                        } else {
                            columnProperty.setter.invoke(object, value);
                        }
                    } catch (IllegalArgumentException e) {
                        String msg = "Object " + objectClass + ". Column: " + columnName + " Type of property: " + returnType + " - Type read: " + value.getClass() + " VALUE: " + value;
                        throw new PersismException(msg, e);
                    }
                }
            }
        }

        // This is doing a similar check to above but on the ResultSet itself.
        // This tests for when a user writes their own SQL and forgets a column.
        if (foundColumns.size() < properties.keySet().size()) {

            Set<String> missing = new HashSet<>(columnCount);
            missing.addAll(properties.keySet());
            missing.removeAll(foundColumns);

            // todo maybe strict mode off logs warn? Should we do this if this is Query vs Table?
            throw new PersismException("Object " + objectClass + " was not properly initialized. Some properties not initialized by the queried columns: " + foundColumns + " Missing:" + missing);
        }

        if (object instanceof Persistable) {
            // Save this object initial state to later detect changed properties
            ((Persistable) object).saveReadState();
        }

        return (T) object;
    }

    <T> T readRecord(Class<?> objectClass, ResultSet rs) throws SQLException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // resultset may not have columns in the proper order
        // resultset may not have all columns
        // step 1 - get column order based on which properties are found
        // read
        // Note: We can't use this method to read Objects without default constructors using the Record styled conventions
        //       since Java 8 does not have the constructor parameter names.

        Map<String, PropertyInfo> propertiesByColumn;
        if (objectClass.getAnnotation(NotTable.class) == null) {
            propertiesByColumn = metaData.getTableColumnsPropertyInfo(objectClass, connection);
        } else {
            propertiesByColumn = metaData.getQueryColumnsPropertyInfo(objectClass, rs);
        }

        Constructor<?>[] constructors = objectClass.getConstructors();
        Constructor<?> selectedConstructor = null;

        // Find constructor with the most params
        int paramCount = 0;
        for (Constructor con : constructors) {
            if (con.getParameterCount() > paramCount) {
                selectedConstructor = con;
                paramCount = con.getParameterCount();
            }
        }
        assert selectedConstructor != null;

        if (selectedConstructor.getParameterCount() != propertiesByColumn.keySet().size()) {
            throw new PersismException("TEMP: constructor mismatch to columns ....");
        }

        // now read resultset by property order
        Map<String, PropertyInfo> propertyInfoByConstructorOrder = new LinkedHashMap<>(paramCount);
        for (Parameter param : selectedConstructor.getParameters()) {
            for (String col : propertiesByColumn.keySet()) {
                if (param.getName().equals(propertiesByColumn.get(col).field.getName())) {
                    propertyInfoByConstructorOrder.put(col, propertiesByColumn.get(col));
                }
            }
        }

        // now read by this order
        List<Object> constructorParams = new ArrayList<>(12);
        List<Class<?>> constructorTypes = new ArrayList<>(12);

        ResultSetMetaData rsmd = rs.getMetaData();
        Map<String, Integer> ordinals = new HashMap<>(rsmd.getColumnCount());
        for (int j = 1; j <= rsmd.getColumnCount(); j++) {
            ordinals.put(rsmd.getColumnLabel(j), j);
        }
        for (String col : propertyInfoByConstructorOrder.keySet()) {

//                constructorParams.add(rs.getObject(col)); // todo rs.getObject could be a problem really we want to call readColum which reads and converts

            // THROWS Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Map.get(Object)" is null
            // IF you DIDN'T INCLUDE THE COLUMN
            Object value = readColumn(rs, ordinals.get(col), propertyInfoByConstructorOrder.get(col).field.getType());
            constructorParams.add(value); // todo rs.getObject could be a problem really we want to call readColum which reads and converts

            constructorTypes.add(propertyInfoByConstructorOrder.get(col).field.getType());
        }

        // TODO why do I need to get the constructor? Didn't I select a constructor above?
        Constructor<?> constructor = objectClass.getConstructor(constructorTypes.toArray(new Class<?>[0]));
        Parameter[] parameters = constructor.getParameters();
        for (Parameter parameter : parameters) {
            log.warn("param: " + parameter.getName());
        }
        return (T) constructor.newInstance(constructorParams.toArray());

    }


    <T> T readColumn(ResultSet rs, int column, Class<?> returnType) throws SQLException, IOException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int sqlColumnType = rsmd.getColumnType(column);
        String columnName = rsmd.getColumnLabel(column);

        if (returnType.isEnum()) {
            // Some DBs may read an enum type as other 1111 - we can tell it here to read it as a string.
            sqlColumnType = java.sql.Types.CHAR;
        }

        Object value = null;

        Types columnType = Types.convert(sqlColumnType); // note this could be null if we can't match a type
        if (columnType != null) {

            switch (columnType) {

                case BooleanType:
                case booleanType:
                    if (returnType.equals(Boolean.class) || returnType.equals(boolean.class)) {
                        value = rs.getBoolean(column);
                    } else {
                        value = rs.getByte(column);
                    }
                    break;

                case TimestampType:
                    if (returnType.equals(String.class)) { // JTDS
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
                    break;

                case BlobType:
                    byte[] buffer = new byte[1024];
                    Blob blob = rs.getBlob(column);
                    if (blob != null) {
                        try (InputStream in = blob.getBinaryStream()) {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) blob.length());
                            for (int len; (len = in.read(buffer)) != -1; ) {
                                bos.write(buffer, 0, len);
                            }
                            value = bos.toByteArray();
                        }
                    }
                    break;

                case IntegerType:
                    // stupid SQLite reports LONGS as Integers for date types which WRAPS past Integer.MAX - Clowns.
                    if (metaData.getConnectionType() == ConnectionTypes.SQLite) {
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
                    value = null;
                    if (returnType.equals(BigInteger.class)) {
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
                    if (returnType.equals(Character.class) || returnType.equals(char.class)) {
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
            log.warn("Column type not known for SQL type " + sqlColumnType, new Throwable());
            value = rs.getObject(column);
        }

        // If value is null or column type is unknown - no need to try to convert anything.
        if (value != null && columnType != null) {
            value = convertor.convert(value, returnType, columnName);
        }

        return (T) value;
    }
}
