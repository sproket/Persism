package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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

        verifyColumnMappings(objectClass, properties);

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

        // This is doing a similar check to above verifyColumnMappings but on the ResultSet itself.
        // This tests for when a user writes their own SQL and forgets a column.
        if (foundColumns.size() < properties.keySet().size()) {
            Set<String> missing = new HashSet<>(columnCount);
            missing.addAll(properties.keySet());
            foundColumns.forEach(missing::remove);

            throw new PersismException("Object " + objectClass + " was not properly initialized. Some properties not initialized by the queried columns: " + foundColumns + " Missing:" + missing);
        }

        if (object instanceof Persistable) {
            // Save this object initial state to later detect changed properties
            ((Persistable<?>) object).saveReadState();
        }

        return (T) object;
    }

    <T> T readRecord(Class<?> objectClass, ResultSet rs) throws SQLException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // Note: We can't use this method to read Objects without default constructors using the Record styled conventions
        //       since Java 8 does not have the constructor parameter names. (we could if -parameters is used)

        Map<String, PropertyInfo> propertiesByColumn;
        if (objectClass.getAnnotation(NotTable.class) == null) {
            propertiesByColumn = metaData.getTableColumnsPropertyInfo(objectClass, connection);
        } else {
            propertiesByColumn = metaData.getQueryColumnsPropertyInfo(objectClass, rs);
        }
        verifyColumnMappings(objectClass, propertiesByColumn);

        ResultSetMetaData rsmd = rs.getMetaData();

        List<String> missing = new ArrayList<>();
        // http://stackoverflow.com/questions/2026104/hashmap-keyset-foreach-and-remove
        // Remove any properties WHERE COLUMN NOT found
        // REASON: There could be a Record Constructor with fewer parameters than it's main constructor
        // So we use the specific list to find a specific constructor.
        // If we don't find a constructor then there's some mismatch.
        var it = propertiesByColumn.keySet().iterator();
        while (it.hasNext()) {
            var key = it.next();
            boolean found = false;
            for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                if (rsmd.getColumnLabel(j).equalsIgnoreCase(key)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.warn("readRecord: Column " + key + " not found in Columns.");
                missing.add(key + " = " + propertiesByColumn.get(key).propertyName);
                it.remove();
            }
        }

        // TODO ALL THIS IS CALLED once PER ROW. Can we optimize? Clearly....

        List<String> propertyNames = propertiesByColumn.values().stream().
                map(PropertyInfo::propertyName).
                collect(Collectors.toList());

        Constructor<?> selectedConstructor = findConstructor(objectClass, propertyNames);
        if (selectedConstructor == null) {
            List<String> allProperties = MetaData.getPropertyInfo(objectClass).stream().
                    map(PropertyInfo::propertyName).
                    sorted().
                    collect(Collectors.toList());


            StringBuilder sb = new StringBuilder();

            sb.append("readRecord: Could not find an appropriate Record constructor for class: ").append(objectClass);
            if (missing.size() > 0) {
                sb.append(" Missing: ").append(missing);
            }
            sb.append(" Properties: ").append(allProperties);

            List<String> columns = new ArrayList<>();
            for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                columns.add(rsmd.getColumnLabel(j));
            }
            sb.append(" COLUMNS: ").append(columns);

            throw new PersismException(sb.toString());
        }

        // now read resultset by property order
        Map<String, PropertyInfo> propertyInfoByConstructorOrder = new LinkedHashMap<>(selectedConstructor.getParameterCount());
        for (Parameter param : selectedConstructor.getParameters()) {
            for (String col : propertiesByColumn.keySet()) {
                if (param.getName().equals(propertiesByColumn.get(col).field.getName())) {
                    propertyInfoByConstructorOrder.put(col, propertiesByColumn.get(col));
                }
            }
        }

        // now read by this order
        List<Object> constructorParams = new ArrayList<>(12);

        Map<String, Integer> ordinals = new HashMap<>(rsmd.getColumnCount());
        for (int j = 1; j <= rsmd.getColumnCount(); j++) {
            ordinals.put(rsmd.getColumnLabel(j), j);
        }
        for (String col : propertyInfoByConstructorOrder.keySet()) {

            Class<?> fieldType = propertyInfoByConstructorOrder.get(col).field.getType();
            if (ordinals.containsKey(col)) {
                Object value = readColumn(rs, ordinals.get(col), fieldType);
                if (value == null && fieldType.isPrimitive()) {
                    // Set null primitives to their default, otherwise the constructor will not be found
                    value = Types.getDefaultValue(fieldType);
                }
                constructorParams.add(value);
            } else {
                log.warn("COL? " + col);
            }
            // or add constructorParams.add(value); NULL?

        }
        return (T) selectedConstructor.newInstance(constructorParams.toArray());
    }

    private Constructor<?> findConstructor(Class<?> objectClass, List<String> propertyNames) {

        // todo we only want the primary canonical constructor for a record but it is possible to make constructors with less params which we can ignore BUT ALSO ONES WITH EXTRA UNUSED PARAMS! See CustomerOrderGarbage
        // SO get constructors OR DECLARED CONSTRUCTORS?  By Param count order desc
        // loop down and make sure each param has a field.
        // AND THAT FIELD IS NO STATIC SINCE THAT FUCKING WOULD BE ALLOWED!
        // objectClass.getRecordComponents() ?
        // NO FORGET IT. Go with the canonical constructor only
        Constructor<?>[] constructors = objectClass.getConstructors(); // just public ones.
        // Constructor<?>[] Xconstructors = objectClass.getDeclaredConstructors(); // we don't need any private or other constructors
        Constructor<?> selectedConstructor = null;

        for (Constructor<?> con : constructors) {
            //con.
            log.debug("constructor: %s", con + " " + Arrays.asList(con.getParameters()) );
            // Maps into a list if parameter names and uses listEqualsIgnoreOrder to compare
            List<String> parameterNames = Arrays.stream(con.getParameters()).
                    map(Parameter::getName).collect(Collectors.toList());

            if (listEqualsIgnoreOrder(propertyNames, parameterNames)) {
                selectedConstructor = con;
                break;
            }
        }
        log.debug("findConstructor: %s", selectedConstructor);
        return selectedConstructor;
    }

    private void verifyColumnMappings(Class<?> objectClass, Map<String, PropertyInfo> properties) {
        // Test if all properties have column mapping and throw PersismException if not
        // This block verifies that the object is fully initialized.
        // Any properties not marked by NotColumn should have been set (or if they have a getter only?????) ????
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

    // https://stackoverflow.com/questions/1075656/simple-way-to-find-if-two-different-lists-contain-exactly-the-same-elements
    private static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }
}
