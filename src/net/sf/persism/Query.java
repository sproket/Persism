package net.sf.persism;

import net.sf.persism.annotations.QueryResult;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Performs read operations from the database.
 * @deprecated see Session
 * @author Dan Howard
 * @since 9/8/11 6:07 AM
 */
final class Query {

    private static final Log log = Log.getLogger(Query.class);

    private Connection connection;

    // JUNIT
    MetaData metaData;

    /**
     * @param connection
     * @throws PersismException
     */
    public Query(Connection connection) throws PersismException {
        this.connection = connection;
        init(connection);
    }

    private void init(Connection connection) {

        // place any DB specific properties here.
        try {
            metaData = MetaData.getInstance(connection);
        } catch (SQLException e) {
            throw new PersismException(e);
        }
    }

    /**
     * Read a list of objects of the specified class using the specified SQL query and parameters.
     * The type of the list can be Data Objects or native Java Objects.
     *
     * @param objectClass class of objects to return.
     * @param sql         query string to execute.
     * @param parameters  parameters to the query.
     * @param <T>
     * @return a list of objects of the specified class using the specified SQL query and parameters.
     * @throws PersismException
     */
    public <T> List<T> readList(Class<T> objectClass, String sql, Object... parameters) throws PersismException {
        List<T> list = new ArrayList<T>(32);

        Result result = new Result();

        // If we know this type it means it's a primitive type. Not a DAO so we use a different rule to read those
        boolean readPrimitive = Types.getType(objectClass) != null;

        if (!readPrimitive && objectClass.getAnnotation(QueryResult.class) == null) {
            metaData.getTableColumns(objectClass, connection); // TODO Make sure columns are initialized properly if this is a table WHY?
        }

        try {

            exec(result, sql, parameters);

            while (result.rs.next()) {

                if (readPrimitive) {
                    list.add((T) readPrimitive(result.rs, 1, objectClass));
                } else {
                    // should be getDeclaredConstructor().newInstance() now.
                    T t = objectClass.newInstance();
                    t = (T) readObject(t, result.rs);
                    list.add(t);
                }
            }

        } catch (IllegalAccessException e) {
            throw new PersismException(e);
        } catch (InstantiationException e) {
            throw new PersismException(e);
        } catch (InvocationTargetException e) {
            throw new PersismException(e);
        } catch (SQLException e) {
            // todo check transaction and rollback. Even though it's a query a transaction may exist. SEE OTHER PLACES WHERE THIS COULD OCCUR
            throw new PersismException(e);
        } catch (IOException e) {
            throw new PersismException(e);
        } finally {
            Util.cleanup(result.st, result.rs);
        }

        return list;
    }

//    public <T> List<T> readList(Class<T> objectClass) throws PersismException {
//        return readList(objectClass, metaData.getSelectStatement(objectClass, connection));
//    }

    /**
     * Read an object from the database by it's primary key.
     * You should instantiate the object and set the primary key properties before calling this method.
     *
     * @param object Data object to read from the database.
     * @return true if the object was found by the primary key.
     * @throws PersismException if something goes wrong.
     */
    public boolean read(Object object) throws PersismException {

        Class objectClass = object.getClass();

        // If we know this type it means it's a primitive type. This method cannot be used for primitives
        boolean readPrimitive = Types.getType(objectClass) != null;
        if (readPrimitive) {
            throw new PersismException("Cannot read a primitive type object with this method.");
        }

        List<String> primaryKeys = metaData.getPrimaryKeys(objectClass, connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException("Cannot perform readObjectByPrimary. " + metaData.getTableName(objectClass) + " has no primary keys.");
        }

        Map<String, PropertyInfo> properties = metaData.getTableColumns(object.getClass(), connection);
        List<Object> params = new ArrayList<Object>(primaryKeys.size());

        Result result = new Result();
        try {
            for (String column : primaryKeys) {
                PropertyInfo propertyInfo = properties.get(column);
                params.add(propertyInfo.getter.invoke(object));
            }

            exec(result, metaData.getSelectStatement(object, connection), params.toArray());

            if (result.rs.next()) {
                readObject(object, result.rs);
                return true;
            }
            return false;

        } catch (IllegalAccessException e) {
            throw new PersismException(e);
        } catch (InvocationTargetException e) {
            throw new PersismException(e);
        } catch (SQLException e) {
            throw new PersismException(e);
        } catch (IOException e) {
            throw new PersismException(e);
        } finally {
            Util.cleanup(result.st, result.rs);
        }
    }

    /**
     * Reads an object of the specified type from the database. The type can a be Data Object or a native Java Object.
     *
     * @param objectClass Type of returned value
     * @param sql         query - this would usually be a select OR a select of a single column if the type is a primitive.
     *                    If this is a primitive type then this method will only look at the 1st column in the result.
     * @param parameters  parameters to the query.
     * @param <T>
     * @return value read from the database
     * @throws PersismException Well, this is a runtime exception so actually it could be anything really.
     */
    public <T> T read(Class<T> objectClass, String sql, Object... parameters) throws PersismException {

        // If we know this type it means it's a primitive type. Not a DAO so we use a different rule to read those
        boolean readPrimitive = Types.getType(objectClass) != null;

        if (!readPrimitive && objectClass.getAnnotation(QueryResult.class) == null) {
            // Make sure columns are initialized properly if this is a table  todo  why?
            metaData.getTableColumns(objectClass, connection);
        }

        Result result = new Result();
        try {

            exec(result, sql, parameters);

            if (result.rs.next()) {

                if (readPrimitive) {
                    return (T) readPrimitive(result.rs, 1, objectClass);

                } else {
                    T t = objectClass.newInstance();
                    readObject(t, result.rs);
                    return t;
                }
            }

            return null;

        } catch (IllegalAccessException e) {
            throw new PersismException(e);

        } catch (InvocationTargetException e) {
            throw new PersismException(e);

        } catch (SQLException e) {
            throw new PersismException(e);

        } catch (InstantiationException e) {
            throw new PersismException(e);

        } catch (IOException e) {
            throw new PersismException(e);

        } finally {
            Util.cleanup(result.st, result.rs);
        }
    }

    private <T> T readObject(Object object, ResultSet rs) throws IllegalAccessException, SQLException, InvocationTargetException, IOException {

        Class objectClass = object.getClass();
        // We should never call this method with a primitive type.
        assert Types.getType(objectClass) == null;

        Map<String, PropertyInfo> properties;
        if (objectClass.getAnnotation(QueryResult.class) == null) {
            properties = metaData.getTableColumns(objectClass, connection);
        } else {
            properties = metaData.getQueryColumns(objectClass, rs);
        }

        // Test if all properties have column mapping and throw PersismException if not
        // This block verifies that the object is fully initialized.
        // Any properties not marked by NotMapped should have been set (or if they have a getter only)
        // If not throw a PersismException
        Collection<PropertyInfo> allProperties = MetaData.getPropertyInfo(objectClass);
        if (properties.values().size() < allProperties.size()) {
            Set<PropertyInfo> missing = new HashSet<PropertyInfo>(allProperties.size());
            missing.addAll(allProperties);
            missing.removeAll(properties.values());

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (PropertyInfo prop : missing) {
                sb.append(sep).append(prop.propertyName);
                sep = ",";
            }

            throw new PersismException("Object " + objectClass + " was not properly initialized. Some properties not found in the queried columns (" + sb + ").");
        }


        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<String> foundColumns = new ArrayList<String>(columnCount);

        for (int j = 1; j <= columnCount; j++) {

            String columnName = rsmd.getColumnLabel(j);
            PropertyInfo columnProperty = properties.get(columnName);

            if (columnProperty != null) {
                Class getterType = columnProperty.getter.getReturnType();

                Object value = readPrimitive(rs, j, getterType);

                foundColumns.add(columnName);

                if (value != null) {
                    try {
                        columnProperty.setter.invoke(object, value);
                    } catch (IllegalArgumentException e) {
                        String msg = e.getMessage() + " Object " + objectClass + ". Column: " + columnName + " Type of property: " + getterType + " - Type read: " + value.getClass() + " VALUE: " + value;
                        throw new PersismException(msg, e);
                    }

                }
            }
        }

        if (foundColumns.size() < properties.keySet().size()) {

            Set<String> missing = new HashSet<String>(columnCount);
            missing.addAll(properties.keySet());
            missing.removeAll(foundColumns);

            throw new PersismException("Object " + objectClass + " was not properly initialized. Some properties not found in the queried columns. : " + missing);
        }

        if (object instanceof Persistable) {
            // Save this object's initial state to later detect changed properties
            ((Persistable) object).saveReadState();
        }

        return (T) object;

    }

    private Object readPrimitive(ResultSet rs, int column, Class returnType) throws SQLException, IOException {
        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        int sqlColumnType = resultSetMetaData.getColumnType(column);

        Types columnType = Types.convert(sqlColumnType); // note this could be null if we can't match a type
        // Since there is no specific SQL column type for UUID we will use the return type to detect it.
        if (returnType.equals(UUID.class)) {
            // Check the return type for UUID since resultSetMetaData.getColumnType(column) has no UUID type
            // it always returns a char or nvarchar so we'll just test and set it here. FFS.
            columnType = Types.UUIDType;
        }
        String columnName = resultSetMetaData.getColumnLabel(column);

        Object value;
        if (columnType != null) {
            switch (columnType) {

                case TimestampType:
                    // work around to Oracle reading a oracle.sql.TIMESTAMP class with getObject
                    value = rs.getTimestamp(column);
                    break;
                case ClobType:
                    value = rs.getClob(column);
                    InputStream in = ((Clob) value).getAsciiStream();
                    StringWriter write = new StringWriter();

                    int c = -1;
                    while ((c = in.read()) != -1) {
                        write.write(c);
                    }
                    write.flush();
                    value = write.toString();
                    break;
                case BlobType:
                    // todo BlobType
                case InputStreamType:
                    // todo InputStreamType
                case ReaderType:
                    // todo ReaderType
                case EnumType:
                    // todo EnumType?
                case UUIDType:
                    value = rs.getObject(column);
                    if (value != null) {
                        value = UUID.fromString("" + value);
                    }
                    break;
                case floatType:
                case FloatType:
                case doubleType:
                case DoubleType:
                    if (returnType.equals(java.math.BigDecimal.class)) {
                        value = rs.getBigDecimal(column);
                    } else if (returnType.equals(java.lang.Double.class)) {
                        value = rs.getDouble(column);
                    } else {
                        value = rs.getFloat(column);
                    }
                    break;

                default:
                    value = rs.getObject(column);
            }

        } else {
            log.warn("Column type not known for SQL type " + sqlColumnType);
            value = rs.getObject(column);
        }

        // If value is null or column type is unknown - no need to try to convert anything.
        if (value != null && columnType != null) {

            Types valueType = Types.getType(value.getClass());

            // try to convert or cast the value to the proper type.
            // todo do code coverage for each specific type
            switch (valueType) {

                case booleanType:
                case BooleanType:
                    break;

                case byteType:
                case ByteType:
                case shortType:
                case ShortType:
                case integerType:
                case IntegerType:
                    // int to bool
                    if (returnType == Boolean.class || returnType == boolean.class) {
                        value = (Integer.valueOf("" + value) == 0) ? false : true;
                    }
                    break;

                case longType:
                case LongType:
                    // long to date
                    if (returnType.isAssignableFrom(java.util.Date.class) || returnType.isAssignableFrom(java.sql.Date.class)) {
                        long lval = Long.valueOf("" + value);

                        if (returnType.equals(java.sql.Date.class)) {
                            value = new java.sql.Date(lval);
                        } else {
                            value = new java.util.Date(lval);
                        }
                    } else if (returnType == Integer.class || returnType == int.class) {
                        log.warn("Possible overflow column " + columnName + " - Property is INT and column value is LONG");
                        value = Integer.parseInt("" + value);
                    }

                    break;

                case floatType:
                case FloatType:
                    break;

                case doubleType:
                case DoubleType:
                    // float or doubles to BigDecimal
                    if (returnType == BigDecimal.class) {
                        value = new BigDecimal("" + value);
                    } else if (returnType == Float.class || returnType == float.class) {
                        // todo add tests for this
                        log.warn("Possible overflow column " + columnName + " - Property is FLOAT and column value is DOUBLE");
                        value = Float.parseFloat("" + value);
                    } else if (returnType == Integer.class || returnType == int.class) {
                        log.warn("Possible overflow column " + columnName + " - Property is INT and column value is DOUBLE");
                        String val = "" + value;
                        if (val.contains(".")) {
                            val = val.substring(0, val.indexOf("."));
                        }
                        value = Integer.parseInt(val);
                    }
                    break;

                case BigDecimalType:
                    // mostly oracle
                    if (returnType == Float.class || returnType == float.class) {
                        value = ((BigDecimal) value).floatValue();
                    } else if (returnType == Double.class || returnType == double.class) {
                        value = ((BigDecimal) value).doubleValue();
                    } else if (returnType == Long.class || returnType == long.class) {
                        value = ((BigDecimal) value).longValue();
                    } else if (returnType == Integer.class || returnType == int.class) {
                        value = ((BigDecimal) value).intValue();
                    } else if (returnType == Boolean.class || returnType == boolean.class) {
                        value = ((BigDecimal) value).intValue() == 1;
                    }
                    break;

                case StringType:

                    // Read a string but we want a date
                    if (returnType.isAssignableFrom(java.util.Date.class) || returnType.isAssignableFrom(java.sql.Date.class)) {
                        // This condition occurs in SQLite when you have a datetime with default annotated
                        // the format returned is 2012-06-02 19:59:49
                        Date dval = null;
                        try {
                            // Used for SQLite returning dates as Strings under some conditions
                            DateFormat df = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");
                            dval = df.parse("" + value);
                        } catch (ParseException e) {
                            String msg = e.getMessage() + ". Column: " + columnName + " Type of property: " + returnType + " - Type read: " + value.getClass() + " VALUE: " + value;
                            throw new PersismException(msg, e);
                        }

                        if (returnType.equals(java.sql.Date.class)) {
                            value = new java.sql.Date(dval.getTime());
                        } else {
                            value = dval;
                        }

                    } else if (returnType.isEnum()) {
                        // If this is an enum do a case insensitive comparison
                        Object[] enumConstants = returnType.getEnumConstants();
                        for (Object element : enumConstants) {
                            if (("" + value).equalsIgnoreCase(element.toString())) {
                                value = element;
                                break;
                            }
                        }
                    }

                    break;

                case characterType:
                case CharacterType:
                    break;

                case UtilDateType:
                    break;
                case SQLDateType:
                    break;
                case TimeType:
                    break;

                case TimestampType:
                    if (returnType.isAssignableFrom(Date.class) || returnType.isAssignableFrom(java.sql.Date.class)) {
                        if (returnType.equals(java.sql.Date.class)) {
                            value = new java.sql.Date(((Timestamp) value).getTime());
                        } else {
                            value = new Date(((Timestamp) value).getTime());
                        }
                    } else {
                        value = ((Timestamp) value).getTime();
                    }

                    break;

                case byteArrayType:
                    break;
                case ByteArrayType:
                    break;
                case charArrayType:
                    break;
                case CharArrayType:
                    break;
                case ClobType:
                    // Convert to string
                    if (value != null) {
                        value = "" + value;
                    }
                    break;
                case BlobType:
                    break;
                case InputStreamType:
                    break;
                case ReaderType:
                    break;
                case EnumType:
                    break;
            }
        }

        return value;

/*
            } else if (type == java.io.InputStream.class) {
                value = resultSet.getBinaryStream(column);
            } else if (type == java.io.Reader.class) {
                value = resultSet.getCharacterStream(column);
            } else if (type == java.sql.Clob.class) {
                value = resultSet.getClob(column);
            } else if (type == java.sql.Blob.class) {
                value = resultSet.getBlob(column);

         */
    }

    private Object XreadPrimitive(ResultSet rs, int column, Class returnType) throws SQLException {
        // todo in H2
        // CREATE TABLE X  ID INT IDENTITY PRIMARY KEY -- creates INT
        // CREATE TABLE X  ID IDENTITY PRIMARY KEY -- creates LONG - can cause problems if data object uses int

        ResultSetMetaData resultSetMetaData = rs.getMetaData();
        Types columnType = Types.convert(resultSetMetaData.getColumnType(column)); // note this could be null if we can't match a type
        String columnName = resultSetMetaData.getColumnLabel(column);

        Object value;
        if (Types.TimestampType == columnType) {
            // work around to Oracle reading a oracle.sql.TIMESTAMP class with getObject
            value = rs.getTimestamp(column);
        } else {
            value = rs.getObject(column);
        }

        if (value != null) {

            Types valueType = Types.getType(value.getClass());

            //if (log.isDebugEnabled()) {
            log.debug("COLUMN: " + columnName + " value " + value + " type: " + valueType + " COLUMN TYPE: " + columnType + " return type: " + returnType);
//            }

            // Convert value to native java type
            if (value instanceof Timestamp) {
// done
                if (!returnType.isAssignableFrom(java.util.Date.class) && !returnType.isAssignableFrom(java.sql.Date.class)) {
                    value = ((Timestamp) value).getTime();
                } else {
                    if (returnType.equals(java.sql.Date.class)) {
                        value = new java.sql.Date(((Timestamp) value).getTime());
                    } else {
                        value = new java.util.Date(((Timestamp) value).getTime());
                    }
                }

            } else if (value instanceof Long &&
                    (returnType.isAssignableFrom(java.util.Date.class) || returnType.isAssignableFrom(java.sql.Date.class))) {
// done

                long lval = Long.valueOf("" + value);

                if (returnType.equals(java.sql.Date.class)) {
                    value = new java.sql.Date(lval);
                } else {
                    value = new java.util.Date(lval);
                }
            } else if (value instanceof String &&
                    (returnType.isAssignableFrom(java.util.Date.class) || returnType.isAssignableFrom(java.sql.Date.class))) {
// done
                // This condition occurs in SQLite when you have a datetime with default annotated
                // the format returned is 2012-06-02 19:59:49
                Date dval = null;
                try {
                    // Used for SQLite returning dates as Strings under some conditions
                    DateFormat df = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");
                    dval = df.parse("" + value);
                } catch (ParseException e) {
                    String msg = e.getMessage() + ". Column: " + columnName + " Type of property: " + returnType + " - Type read: " + value.getClass() + " VALUE: " + value;
                    throw new PersismException(msg, e);
                }

                if (returnType.equals(java.sql.Date.class)) {
                    value = new java.sql.Date(dval.getTime());
                } else {
                    value = dval;
                }

            } else if (value instanceof String && returnType.isEnum()) {
// done
                // If this is an enum do a case insensitive comparison
                Object[] enumConstants = returnType.getEnumConstants();
                for (Object element : enumConstants) {
                    if (("" + value).equalsIgnoreCase(element.toString())) {
                        value = element;
                        break;
                    }
                }
            } else if (value instanceof BigDecimal) { //  mostly oracle.
// done
                if (returnType == Float.class || returnType == float.class) {
                    value = ((BigDecimal) value).floatValue();
                } else if (returnType == Double.class || returnType == double.class) {
                    value = ((BigDecimal) value).doubleValue();
                } else if (returnType == Long.class || returnType == long.class) {
                    value = ((BigDecimal) value).longValue();
                } else if (returnType == Integer.class || returnType == int.class) {
                    value = ((BigDecimal) value).intValue();
                } else if (returnType == Boolean.class || returnType == boolean.class) {
                    value = ((BigDecimal) value).intValue() == 1;
                }

            } else if (value instanceof Integer && (returnType == Boolean.class || returnType == boolean.class)) {
// done
                value = (Integer.valueOf("" + value) == 0) ? false : true;

            } else if (value instanceof Float && returnType == BigDecimal.class) {
// done
                value = new BigDecimal("" + value);
            }
        }

        return value;
    }


    private Result exec(Result result, String sql, Object... parameters) throws SQLException {
        if (sql.toLowerCase().contains("select ")) {
            result.st = connection.prepareStatement(sql);

            PreparedStatement pst = (PreparedStatement) result.st;
            Util.setParameters(pst, parameters);
            result.rs = pst.executeQuery();
        } else {
            // todo unit tests need to cover this.
            result.st = connection.prepareCall("{call " + sql + "}");

            CallableStatement cst = (CallableStatement) result.st;
            Util.setParameters(cst, parameters);
            result.rs = cst.executeQuery();
        }
        return result;
    }

}
