package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Performs various read and write operations in the database.
 *
 * @author Dan Howard
 * @since 1/8/2021
 */
public final class Session {

    private static final Log log = Log.getLogger(Session.class);

    private Connection connection;

    private MetaData metaData;

    private static final List<String> warnings = new ArrayList<>(32);

    /**
     * @param connection db connection
     * @throws PersismException if something goes wrong
     */
    public Session(Connection connection) throws PersismException {
        this.connection = connection;
        init(connection);
    }

    private void init(Connection connection) {

        // place any DB specific properties here.
        try {
            metaData = MetaData.getInstance(connection);
        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    /**
     * Updates the data object in the database.
     *
     * @param object data object to update.
     * @return usually 1 to indicate rows changed via JDBC.
     * @throws PersismException Indicating the upcoming robot uprising.
     */
    public int update(Object object) throws PersismException {
        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException("Cannot perform update. " + metaData.getTableName(object.getClass()) + " has no primary keys.");
        }

        PreparedStatement st = null;
        try {

            String updateStatement = null;
            try {
                updateStatement = metaData.getUpdateStatement(object, connection);
            } catch (NoChangesDetectedForUpdateException e) {
                log.info("No properties changed. No update required for Object: " + object + " class: " + object.getClass().getName());
                return 0;
            }

            st = connection.prepareStatement(updateStatement);

            // These keys should always be in sorted order.
            Map<String, PropertyInfo> allProperties = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);
            Map<String, PropertyInfo> changedProperties;
            if (object instanceof Persistable) {
                changedProperties = metaData.getChangedProperties((Persistable) object, connection);
            } else {
                changedProperties = allProperties;
            }

            List<Object> params = new ArrayList<>(primaryKeys.size());
            for (String column : changedProperties.keySet()) {
                if (!primaryKeys.contains(column)) {
                    Object value = allProperties.get(column).getter.invoke(object);

                    if (value != null) {
                        if (value.getClass().isEnum()) {
                            value = "" + value; // convert enum to string.
                        }

                        if (value instanceof java.util.Date) {
                            java.util.Date dt = (java.util.Date) value;
                            value = new Timestamp(dt.getTime());
                        }

                        if (value instanceof String) {
                            // check width
                            ColumnInfo columnInfo = metaData.getColumns(object.getClass(), connection).get(column);
                            String str = (String) value;
                            if (str.length() > columnInfo.length) {
                                str = str.substring(0, columnInfo.length);
                                // todo Should Persism strict should throw this as an exception?
                                log.warn("TRUNCATION with Column: " + column + " for table: " + metaData.getTableName(object.getClass()) + ". Old value: \"" + value + "\" New value: \"" + str + "\"");
                                value = str;
                            }
                        }
                    }
                    params.add(value);
                }
            }

            for (String column : primaryKeys) {
                params.add(allProperties.get(column).getter.invoke(object));
            }
            Util.setParameters(st, params.toArray());
            int ret = st.executeUpdate();
            return ret;

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);

        } finally {
            Util.cleanup(st, null);
        }
    }

    /**
     * Inserts the data object in the database refreshing with autoinc and other defaults that may exist.
     *
     * @param object the data object to insert.
     * @return usually 1 to indicate rows changed via JDBC.
     * @throws PersismException When planet of the apes starts happening.
     */
    public int insert(Object object) throws PersismException {
        String insertStatement = metaData.getInsertStatement(object, connection);

        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            // These keys should always be in sorted order.
            Map<String, PropertyInfo> properties = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);
            Map<String, ColumnInfo> columns = metaData.getColumns(object.getClass(), connection);

            List<String> generatedKeys = new ArrayList<>(1);
            for (ColumnInfo column : columns.values()) {
                if (column.autoIncrement) {
                    generatedKeys.add(column.columnName);
                }
            }

            if (generatedKeys.size() > 0) {
                String[] keyArray = generatedKeys.toArray(new String[0]);
                st = connection.prepareStatement(insertStatement, keyArray);
            } else {
                st = connection.prepareStatement(insertStatement);
            }

            boolean tableHasDefaultColumnValues = false;

            List<Object> params = new ArrayList<>();
            for (ColumnInfo columnInfo : columns.values()) {

                PropertyInfo propertyInfo = properties.get(columnInfo.columnName);
                if (!columnInfo.autoIncrement) {

                    // See MetaData getInsertStatement - Maybe we should return a new Object type for InsertStatement
                    if (columnInfo.hasDefault) {
                        // Do not include if this column has a default and no value has been
                        // set on it's associated property.
                        if (properties.get(columnInfo.columnName).getter.invoke(object) == null) {

                            if (columnInfo.primary) {
                                throw new PersismException("Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.");
                            }

                            tableHasDefaultColumnValues = true;
                            continue;
                        }
                    }


                    Object value = propertyInfo.getter.invoke(object);

                    if (value != null) {

                        if (value.getClass().isEnum()) {
                            value = "" + value; // convert enum to string.
                        }

                        // sql.Date is a subclass so this would be true
                        if (value instanceof java.util.Date) {
                            java.util.Date dt = (java.util.Date) value;
                            value = new Timestamp(dt.getTime());
                        }


                        if (value instanceof String) {
                            // check width
                            String str = (String) value;
                            if (str.length() > columnInfo.length) {
                                // todo should Persism strict throw this as an exception?
                                str = str.substring(0, columnInfo.length);
                                log.warn("TRUNCATION with Column: " + columnInfo.columnName + " for table: " + metaData.getTableName(object.getClass()) + ". Old value: \"" + value + "\" New value: \"" + str + "\"");
                                value = str;
                            }
                        }
                    }
                    params.add(value);
                }
            }

            // https://forums.oracle.com/forums/thread.jspa?threadID=879222
            // http://download.oracle.com/javase/1.4.2/docs/guide/jdbc/getstart/statement.html
            //int ret = st.executeUpdate(insertStatement, Statement.RETURN_GENERATED_KEYS);
            Util.setParameters(st, params.toArray());
            int ret = st.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("insert ret: " + ret);
            }

            if (generatedKeys.size() > 0) {
                rs = st.getGeneratedKeys();
                for (String column : generatedKeys) {
                    if (rs.next()) {

                        Method setter = properties.get(column).setter;

                        if (setter != null) {
                            Object value = getTypedValueReturnedFromGeneratedKeys(setter.getParameterTypes()[0], rs);

                            if (log.isDebugEnabled()) {
                                log.debug(column + " generated " + value); // HERE!
                                log.debug(setter);
                            }
                            setter.invoke(object, value);

                        } else {
                            log.warn("no setter found for column " + column);
                        }
                    }
                }
            }

            if (tableHasDefaultColumnValues) {
                // Read the full object back to update any properties which had defaults
                fetch(object);
            }

            return ret;
        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(st, rs);
        }
    }


    /**
     * Deletes the data object object from the database.
     *
     * @param object data object to delete
     * @return usually 1 to indicate rows changed via JDBC.
     * @throws PersismException Perhaps when asteroid 1999 RQ36 hits us?
     */
    public int delete(Object object) throws PersismException {

        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException("Cannot perform delete. " + metaData.getTableName(object.getClass()) + " has no primary keys.");
        }

        PreparedStatement st = null;
        try {
            String deleteStatement = metaData.getDeleteStatement(object, connection);
            st = connection.prepareStatement(deleteStatement);

            // These keys should always be in sorted order.
            Map<String, PropertyInfo> columns = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);

            List<Object> params = new ArrayList<Object>(primaryKeys.size());
            for (String column : primaryKeys) {
                params.add(columns.get(column).getter.invoke(object));
            }
            Util.setParameters(st, params.toArray());
            int ret = st.executeUpdate();
            return ret;

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);

        } finally {
            Util.cleanup(st, null);
        }
    }

    // For unit tests only for now.
    void execute(String sql, Object... parameters) {

        if (log.isDebugEnabled()) {
            log.debug("execute: " + sql + " params: " + Arrays.asList(parameters));
        }
        Statement st = null;
        try {

            if (parameters.length == 0) {
                st = connection.createStatement();
                st.execute(sql);
            } else {
                st = connection.prepareStatement(sql);
                PreparedStatement pst = (PreparedStatement) st;
                Util.setParameters(pst, parameters);
                pst.execute();
            }

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(st, null);
        }
    }

    /**
     * Query for a list of objects of the specified class using the specified SQL query and parameters.
     * The type of the list can be Data Objects or native Java Objects or primitives.
     *
     * @param objectClass class of objects to return.
     * @param sql         query string to execute.
     * @param parameters  parameters to the query.
     * @param <T>         Return type
     * @return a list of objects of the specified class using the specified SQL query and parameters.
     * @throws PersismException If something goes wrong you get a big stack trace.
     */
    public <T> List<T> query(Class<T> objectClass, String sql, Object... parameters) throws PersismException {
        List<T> list = new ArrayList<T>(32);

        Result result = new Result();

        // If we know this type it means it's a primitive type. Not a DAO so we use a different rule to read those
        boolean readPrimitive = Types.getType(objectClass) != null;

        if (!readPrimitive && objectClass.getAnnotation(NotTable.class) == null) {
            // Make sure columns are initialized if this is a table.
            metaData.getTableColumnsPropertyInfo(objectClass, connection);
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

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(result.st, result.rs);
        }

        return list;

    }

    /**
     * Fetch an object from the database by it's primary key(s).
     * You should instantiate the object and set the primary key properties before calling this method.
     *
     * @param object Data object to read from the database.
     * @return true if the object was found by the primary key.
     * @throws PersismException if something goes wrong.
     */
    public boolean fetch(Object object) throws PersismException {
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

        Map<String, PropertyInfo> properties = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);
        List<Object> params = new ArrayList<>(primaryKeys.size());

        Result result = new Result();
        try {
            for (String column : primaryKeys) {
                PropertyInfo propertyInfo = properties.get(column);
                params.add(propertyInfo.getter.invoke(object));
            }
            String sql = metaData.getSelectStatement(object, connection);
            if (log.isDebugEnabled()) {
                log.debug("FETCH " + sql + " PARAMS: " + params);
            }
            exec(result, sql, params.toArray());

            if (result.rs.next()) {
                readObject(object, result.rs);
                return true;
            }
            return false;

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(result.st, result.rs);
        }
    }


// Method collides with other fetch methods - ambiguous.
//    /**
//     * Fetch an object from the database by primary key(s).
//     *
//     * @param objectClass class of objects to return
//     * @param primaryKey  primary key value parameters
//     * @param <T>         Return type
//     * @return new instance of T or null if not found
//     * @throws PersismException if something goes wrong
//     */
//    public <T> T fetch(Class<T> objectClass, Object... primaryKey) throws PersismException {
//
//        String select = metaData.getSelectStatement(objectClass, connection);
//
//        if (log.isDebugEnabled()) {
//            log.debug("fetch Class<T> objectClass, Object... primaryKey: SQL " + select);
//        }
//        return fetch(objectClass, select, primaryKey);
//    }

    /**
     * Fetch an object of the specified type from the database. The type can be a Data Object or a native Java Object or primitive.
     *
     * @param objectClass Type of returned value
     * @param sql         query - this would usually be a select OR a select of a single column if the type is a primitive.
     *                    If this is a primitive type then this method will only look at the 1st column in the result.
     * @param parameters  parameters to the query.
     * @param <T>         Return type
     * @return value read from the database of type T or null if not found
     * @throws PersismException Well, this is a runtime exception so actually it could be anything really.
     */
    public <T> T fetch(Class<T> objectClass, String sql, Object... parameters) throws PersismException {
        // If we know this type it means it's a primitive type. Not a DAO so we use a different rule to read those
        boolean readPrimitive = Types.getType(objectClass) != null;

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

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(result.st, result.rs);
        }
    }

    MetaData getMetaData() {
        return metaData;
    }

    /*
    Private methods
     */

    private Result exec(Result result, String sql, Object... parameters) throws SQLException {
        if (sql.toLowerCase().startsWith("select ")) {
            result.st = connection.prepareStatement(sql);

            PreparedStatement pst = (PreparedStatement) result.st;
            Util.setParameters(pst, parameters);
            result.rs = pst.executeQuery();
        } else {
            if (!sql.toLowerCase().startsWith("{call")) {
                sql = "{call " + sql + "} ";
            }
            result.st = connection.prepareCall(sql);

            CallableStatement cst = (CallableStatement) result.st;
            Util.setParameters(cst, parameters);
            result.rs = cst.executeQuery();
        }
        return result;
    }

    private <T> T readObject(Object object, ResultSet rs) throws IllegalAccessException, SQLException, InvocationTargetException, IOException {

        Class objectClass = object.getClass();
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

            throw new PersismException("Object " + objectClass + " was not properly initialized. Some properties not initialized in the queried columns (" + sb + ").");
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

        // This is doing a similar check to above but on the ResultSet itself.
        // This tests for when a user writes their own SQL and forgets a column.
        if (foundColumns.size() < properties.keySet().size()) {

            Set<String> missing = new HashSet<String>(columnCount);
            missing.addAll(properties.keySet());
            missing.removeAll(foundColumns);

            // todo maybe strict mode off logs warn? Should we do this if this is Query vs Table?
            throw new PersismException("Object " + objectClass + " was not properly initialized. Some properties not initialized by the queried columns: " + foundColumns + " Missing:" + missing);
        }

        if (object instanceof Persistable) {
            // Save this object's initial state to later detect changed properties
            ((Persistable) object).saveReadState();
        }

        return (T) object;

    }

    private Object readPrimitive(ResultSet rs, int column, Class returnType) throws SQLException, IOException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int sqlColumnType = rsmd.getColumnType(column);

        Types columnType = Types.convert(sqlColumnType); // note this could be null if we can't match a type
        // Since there is no specific SQL column type for UUID we will use the return type to detect it.
        if (returnType.equals(UUID.class)) {
            // Check the return type for UUID since resultSetMetaData.getColumnType(column) has no UUID type
            // it always returns a char or nvarchar so we'll just test and set it here. FFS.
            columnType = Types.UUIDType;
        }
        String columnName = rsmd.getColumnLabel(column);

        Object value = read(rs, column, sqlColumnType);

        // If value is null or column type is unknown - no need to try to convert anything.
        if (value != null && columnType != null) {
            value = convert(value, returnType, columnName);
        }

        return value;
    }

    // Make an educated guess read an object from the ResultSet to get the best Class type of the object
    private Object read(ResultSet rs, int column, int sqlColumnType) throws SQLException, IOException {
        Object value;
        Types columnType = Types.convert(sqlColumnType); // note this could be null if we can't match a type

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
//                case BlobType:
//                    // todo BlobType
//                case InputStreamType:
//                    // todo InputStreamType
//                case ReaderType:
//                    // todo ReaderType
//                case EnumType:
//                    // todo EnumType - no SQL type for that
                case IntegerType:
                    value = rs.getObject(column) == null ? null : rs.getInt(column);
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
                case DecimalType:
                    value = rs.getBigDecimal(column);
                    break;
                case TimeType:
                    value = rs.getTime(column);
                    break;
// We can't assume rs.getDate will work. SQLITE actually has a long value in here.
// We can live with rs.getObject and the convert method will handle it.
//                case SQLDateType:
//                case UtilDateType:DateType:
//                    value = rs.getDate(column);
//                    break;
                default:
                    value = rs.getObject(column);
            }

        } else {
            log.warn("Column type not known for SQL type " + sqlColumnType, new Throwable());
            value = rs.getObject(column);
        }

        return value;
    }

    // Make a sensible conversion of the Value read from the DB and the property type defined on the Data class.
    private Object convert(Object dbValue, Class propertyType, String columnName) {
        Types valueType = Types.getType(dbValue.getClass());

        // try to convert or cast the value to the proper type.
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
                if (propertyType == Boolean.class || propertyType == boolean.class) {
                    dbValue = (Integer.valueOf("" + dbValue) == 0) ? false : true;
                }
                break;

            case longType:
            case LongType:
                // long to date
                if (propertyType.equals(java.util.Date.class) || propertyType.equals(java.sql.Date.class)) {
                    long lval = Long.valueOf("" + dbValue);

                    if (propertyType.equals(java.sql.Date.class)) {
                        dbValue = new java.sql.Date(lval);
                    } else {
                        dbValue = new java.util.Date(lval);
                    }
                } else if (propertyType == Integer.class || propertyType == int.class) {
                    warnOverflow("Possible overflow column " + columnName + " - Property is INT and column value is LONG");
                    dbValue = Integer.parseInt("" + dbValue);
                }

                break;

            case floatType:
            case FloatType:
                break;

            case doubleType:
            case DoubleType:
                // float or doubles to BigDecimal
                if (propertyType == BigDecimal.class) {
                    dbValue = new BigDecimal("" + dbValue);
                } else if (propertyType == Float.class || propertyType == float.class) {
                    warnOverflow("Possible overflow column " + columnName + " - Property is FLOAT and column value is DOUBLE");
                    dbValue = Float.parseFloat("" + dbValue);
                } else if (propertyType == Integer.class || propertyType == int.class) {
                    warnOverflow("Possible overflow column " + columnName + " - Property is INT and column value is DOUBLE");
                    String val = "" + dbValue;
                    if (val.contains(".")) {
                        val = val.substring(0, val.indexOf("."));
                    }
                    dbValue = Integer.parseInt(val);
                }
                break;

            case DecimalType:
                if (propertyType == Float.class || propertyType == float.class) {
                    dbValue = ((BigDecimal) dbValue).floatValue();
                    warnOverflow("Possible overflow column " + columnName + " - Property is Float and column value is BigDecimal");
                } else if (propertyType == Double.class || propertyType == double.class) {
                    dbValue = ((BigDecimal) dbValue).doubleValue();
                    warnOverflow("Possible overflow column " + columnName + " - Property is Double and column value is BigDecimal");
                } else if (propertyType == Long.class || propertyType == long.class) {
                    dbValue = ((BigDecimal) dbValue).longValue();
                    warnOverflow("Possible overflow column " + columnName + " - Property is Long and column value is BigDecimal");
                } else if (propertyType == Integer.class || propertyType == int.class) {
                    dbValue = ((BigDecimal) dbValue).intValue();
                    warnOverflow("Possible overflow column " + columnName + " - Property is Integer and column value is BigDecimal");
                } else if (propertyType == Boolean.class || propertyType == boolean.class) {
                    // BigDecimal to Boolean. Oracle (sigh) - Additional for a Char to Boolean as then (see TestOracle for links)
                    dbValue = ((BigDecimal) dbValue).intValue() == 1;
                    warnOverflow("Possible overflow column " + columnName + " - Property is Boolean and column value is BigDecimal - seems a bit overkill?");
                } else if (propertyType == String.class) {
                    dbValue = (dbValue).toString();
                }
                break;

            case StringType:

                // Read a string but we want a date
                if (propertyType.equals(java.util.Date.class) || propertyType.equals(java.sql.Date.class)) {
                    // This condition occurs in SQLite when you have a datetime with default annotated
                    // the format returned is 2012-06-02 19:59:49
                    java.util.Date dval = null;
                    try {
                        // Used for SQLite returning dates as Strings under some conditions
                        // SQL or others may return STRING yyyy-MM-dd for older legacy 'date' type.
                        // https://docs.microsoft.com/en-us/sql/t-sql/data-types/date-transact-sql?view=sql-server-ver15
                        String format;
                        if ((""+dbValue).length() > "yyyy-MM-dd".length()) {
                            format = "yyyy-MM-dd hh:mm:ss";
                        } else {
                            format = "yyyy-MM-dd";
                        }
                        DateFormat df = new SimpleDateFormat(format);
                        dval = df.parse("" + dbValue);
                    } catch (ParseException e) {
                        String msg = e.getMessage() + ". Column: " + columnName + " Type of property: " + propertyType + " - Type read: " + dbValue.getClass() + " VALUE: " + dbValue;
                        throw new PersismException(msg, e);
                    }

                    if (propertyType.equals(java.sql.Date.class)) {
                        dbValue = new java.sql.Date(dval.getTime());
                    } else {
                        dbValue = dval;
                    }

                } else if (propertyType.equals(java.sql.Timestamp.class)) {
                    // String to timestamp?
                    // value = new Timestamp()
                    java.util.Date dval = null;
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    try {
                        dval = df.parse("" + dbValue);
                        dbValue = new Timestamp(dval.getTime());
                    } catch (ParseException e) {
                        String msg = e.getMessage() + ". Column: " + columnName + " Type of property: " + propertyType + " - Type read: " + dbValue.getClass() + " VALUE: " + dbValue;
                        throw new PersismException(msg, e);
                    }

                } else if (propertyType.isEnum()) {
                    // If this is an enum do a case insensitive comparison
                    Object[] enumConstants = propertyType.getEnumConstants();
                    for (Object element : enumConstants) {
                        if (("" + dbValue).equalsIgnoreCase(element.toString())) {
                            dbValue = element;
                            break;
                        }
                    }
                } else if (propertyType.equals(UUID.class)) {
                    dbValue = UUID.fromString("" + dbValue);

                } else if (propertyType.equals(Boolean.class) || propertyType.equals(boolean.class)) {
                    // String to Boolean - true or 1 - otherwise false (or null)
                    if (dbValue != null) {
                        String bval = "" + dbValue;
                        dbValue = (bval.equalsIgnoreCase("true") || bval.equals("1"));
                    }

                } else if (propertyType.equals(Time.class)) {
                    // MSSQL works, JTDS returns Varchar in format below
                    DateFormat df = new SimpleDateFormat("hh:mm:ss.SSSSS");
                    try {
                        Date d = df.parse(dbValue + "");
                        dbValue = new Time(d.getTime());
                    } catch (ParseException e) {
                        String msg = e.getMessage() + ". Column: " + columnName + " Type of property: " + propertyType + " - Type read: " + dbValue.getClass() + " VALUE: " + dbValue;
                        throw new PersismException(msg, e);
                    }
                } else if (propertyType.equals(Character.class) || propertyType.equals(char.class)) {
                    String s = ""+dbValue;
                    if (s.length() > 0) {
                        dbValue = s.charAt(0);
                    }
                } else if (propertyType.equals(BigDecimal.class)) {
                    dbValue = new BigDecimal(""+dbValue);
                }
                break;

            case characterType:
            case CharacterType:
                break;

            case UtilDateType:
            case SQLDateType:
            case TimestampType:
                // todo maybe add check for property type = long. Same with TimestampType
                if (propertyType.equals(java.util.Date.class) || propertyType.equals(java.sql.Date.class)) {
                    if (propertyType.equals(java.sql.Date.class)) {
                        dbValue = new java.sql.Date(((Date) dbValue).getTime());
                    } else {
                        dbValue = new java.util.Date(((Date) dbValue).getTime());
                    }
                }
                break;

            case TimeType:
                break;

            case byteArrayType:
            case ByteArrayType:
                break;

            case charArrayType:
            case CharArrayType:
                break;

            case ClobType:
                // Convert to string
                if (dbValue != null) {
                    dbValue = "" + dbValue;
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
            case UUIDType:
                break;
        }
        return dbValue;
    }

    private <T> T getTypedValueReturnedFromGeneratedKeys(Class<T> type, ResultSet rs) throws SQLException {

        Object value = null;
        Types types = Types.getType(type);

        if (types == null) {
            log.warn("Unhandled type " + type);
            return (T) rs.getObject(1);
        }

        switch (types) {

            case integerType:
            case IntegerType:
                value = rs.getInt(1);
                break;

            case longType:
            case LongType:
                value = rs.getLong(1);
                break;
        }
        return (T) value;
    }

    // Prevent duplicate "Possible overflow column" messages
    private static void warnOverflow(String message) {
        if (!warnings.contains(message)) {
            warnings.add(message);
            log.warn(message);
        }
    }

}
