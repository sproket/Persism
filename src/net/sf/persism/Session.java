package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            throw new PersismException("Cannot perform UPDATE - " + metaData.getTableName(object.getClass()) + " has no primary keys.");
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
            setParameters(st, params.toArray());
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
                if (column.autoIncrement || (column.primary && column.hasDefault)) {
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

                    if (columnInfo.hasDefault) {
                        // Do not include if this column has a default and no value has been
                        // set on it's associated property.
                        if (propertyInfo.getter.invoke(object) == null) {

                            if (columnInfo.primary) {
                                // This is supported with PostgreSQL but otherwise throw this an exception
                                if ( !(metaData.connectionType == ConnectionTypes.PostgreSQL)) {
                                    throw new PersismException("Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.");
                                }
                            }

                            tableHasDefaultColumnValues = true;
                            continue;
                        }
                    }

                    Object value = propertyInfo.getter.invoke(object);

                    if (value != null) {
                        // todo these tests should be moved to the setParameters method - duplicated in update method
                        // sql.Date is a subclass so this would be true
                        // same with sql.Time
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
            setParameters(st, params.toArray());
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
                                log.debug(column + " generated " + value);
                            }
                            setter.invoke(object, value);

                        } else {
                            // Should never occur
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
            throw new PersismException("Cannot perform DELETE - " + metaData.getTableName(object.getClass()) + " has no primary keys.");
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
            setParameters(st, params.toArray());
            int ret = st.executeUpdate();
            return ret;

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);

        } finally {
            Util.cleanup(st, null);
        }
    }

    // For unit tests only for now. todo If we ever use this need to add tests for String (length) and Timestamp like we do in insert and update
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
                setParameters(pst, parameters);
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
            // For unit tests
            throw new PersismException("Cannot read a primitive type object with this method.");
        }

        List<String> primaryKeys = metaData.getPrimaryKeys(objectClass, connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException("Cannot perform FETCH - " + metaData.getTableName(objectClass) + " has no primary keys.");
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
            setParameters(pst, parameters);
            result.rs = pst.executeQuery();
        } else {
            if (!sql.toLowerCase().startsWith("{call")) {
                sql = "{call " + sql + "} ";
            }
            result.st = connection.prepareCall(sql);

            CallableStatement cst = (CallableStatement) result.st;
            setParameters(cst, parameters);
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
                        String msg = "Object " + objectClass + ". Column: " + columnName + " Type of property: " + getterType + " - Type read: " + value.getClass() + " VALUE: " + value;
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
        String columnName = rsmd.getColumnLabel(column);
        Object value = read(rs, column, columnName, sqlColumnType, returnType);
        return value;
    }

    // Make an educated guess read an object from the ResultSet to get the best Class type of the object
    private Object read(ResultSet rs, int column, String columnName, int sqlColumnType, Class returnType) throws SQLException, IOException {
        Object value;
        Types columnType = Types.convert(sqlColumnType); // note this could be null if we can't match a type
        if (columnType != null) {

            switch (columnType) {

                case TimestampType:
                    // work around to Oracle reading a oracle.sql.TIMESTAMP class with getObject
                    value = rs.getTimestamp(column);
                    break;
                case ClobType:
                    Clob clob = rs.getClob(column);
                    try (InputStream in = clob.getAsciiStream()) {
                        StringWriter writer = new StringWriter();

                        int c = -1;
                        while ((c = in.read()) != -1) {
                            writer.write(c);
                        }
                        writer.flush();
                        value = writer.toString();
                    }
                    break;
                case BlobType:
                    byte[] buffer = new byte[1024];
                    Blob blob = rs.getBlob(column);
                    try (InputStream in = blob.getBinaryStream()) {
                        // if it goes past int length the oh well...
                        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) blob.length());
                        for (int len; (len = in.read(buffer)) != -1; ) {
                            bos.write(buffer, 0, len);
                        }
                        value = bos.toByteArray();
                    }
                    break;

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

        // If value is null or column type is unknown - no need to try to convert anything.
        if (value != null && columnType != null) {
            value = convert(value, returnType, columnName);
        }

        return value;
    }

    // Make a sensible conversion of the Value read from the DB and the property type defined on the Data class.
    private Object convert(Object dbValue, Class propertyType, String columnName) {
        assert dbValue != null;

        Types valueType = Types.getType(dbValue.getClass());

        if (valueType == null) {
            log.warn("Conversion: Unknown Persism type " + dbValue.getClass() + " - no conversion performed.");
            return dbValue;
        }

        // try to convert or cast the value to the proper type.
        switch (valueType) {

            case booleanType:
            case BooleanType:
                log.debug("BooleanType");
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

                } else if (propertyType.equals(Integer.class) || propertyType.equals(int.class)) {
                    warnNoDuplicates("Possible overflow column " + columnName + " - Property is INT and column value is LONG");
                    dbValue = Integer.parseInt("" + dbValue);

                } else if (propertyType.equals(LocalDate.class)) {
                    // SQLite reads long as date.....
                    dbValue = new Timestamp((long) dbValue).toLocalDateTime().toLocalDate();

                } else if (propertyType.equals(LocalDateTime.class)) {
                    dbValue = new Timestamp((long) dbValue).toLocalDateTime();
                }

                break;

            case floatType:
            case FloatType:
                log.debug("FloatType");
                break;

            case doubleType:
            case DoubleType:
                // float or doubles to BigDecimal
                if (propertyType == BigDecimal.class) {
                    dbValue = new BigDecimal("" + dbValue);
                } else if (propertyType == Float.class || propertyType == float.class) {
                    warnNoDuplicates("Possible overflow column " + columnName + " - Property is FLOAT and column value is DOUBLE");
                    dbValue = Float.parseFloat("" + dbValue);
                } else if (propertyType == Integer.class || propertyType == int.class) {
                    // todo Long?
                    warnNoDuplicates("Possible overflow column " + columnName + " - Property is INT and column value is DOUBLE");
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
                    warnNoDuplicates("Possible overflow column " + columnName + " - Property is Float and column value is BigDecimal");
                } else if (propertyType == Double.class || propertyType == double.class) {
                    dbValue = ((BigDecimal) dbValue).doubleValue();
                    warnNoDuplicates("Possible overflow column " + columnName + " - Property is Double and column value is BigDecimal");
                } else if (propertyType == Long.class || propertyType == long.class) {
                    dbValue = ((BigDecimal) dbValue).longValue();
                    warnNoDuplicates("Possible overflow column " + columnName + " - Property is Long and column value is BigDecimal");
                } else if (propertyType == Integer.class || propertyType == int.class) {
                    dbValue = ((BigDecimal) dbValue).intValue();
                    warnNoDuplicates("Possible overflow column " + columnName + " - Property is Integer and column value is BigDecimal");
                } else if (propertyType == Boolean.class || propertyType == boolean.class) {
                    // BigDecimal to Boolean. Oracle (sigh) - Additional for a Char to Boolean as then (see TestOracle for links)
                    dbValue = ((BigDecimal) dbValue).intValue() == 1;
                    warnNoDuplicates("Possible overflow column " + columnName + " - Property is Boolean and column value is BigDecimal - seems a bit overkill?");
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
                        if (("" + dbValue).length() > "yyyy-MM-dd".length()) {
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
                } else if (propertyType.equals(LocalDate.class)) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date dval = null;
                    try {
                        dval = df.parse("" + dbValue);
                        dbValue = Instant.ofEpochMilli(dval.getTime())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    } catch (ParseException e) {
                        String msg = e.getMessage() + ". Column: " + columnName + " Type of property: " + propertyType + " - Type read: " + dbValue.getClass() + " VALUE: " + dbValue;
                        throw new PersismException(msg, e);
                    }
// This block should maybe have occurred because SQLite usually reads dates as strings
// but for whatever reason getObject call in the read() method returns LONG so we
// never get here. The convert to LocalDateTime is made under the Long case
//                } else if (propertyType.equals(LocalDateTime.class)) {
//                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//                    java.util.Date dval = null;
//                    try {
//                        dval = df.parse("" + dbValue);
//                        dbValue = Instant.ofEpochMilli(dval.getTime())
//                                .atZone(ZoneId.systemDefault())
//                                .toLocalDate();
//                    } catch (ParseException e) {
//                        String msg = e.getMessage() + ". Column: " + columnName + " Type of property: " + propertyType + " - Type read: " + dbValue.getClass() + " VALUE: " + dbValue;
//                        throw new PersismException(msg, e);
//                    }

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
                    String s = "" + dbValue;
                    if (s.length() > 0) {
                        dbValue = s.charAt(0);
                    }
                } else if (propertyType.equals(BigDecimal.class)) {
                    dbValue = new BigDecimal("" + dbValue);
                }
                break;

            case characterType:
            case CharacterType:
                // Does not occur because there's no direct single CHAR type.
                // We get a string and handle it in the String case above.
                log.debug("CharacterType");
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

                if (propertyType.equals(LocalDate.class) || propertyType.equals(LocalDateTime.class)) {
                    Date dt = (Date) dbValue;
                    dbValue = Instant.ofEpochMilli(dt.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }
                break;

            case TimeType:
                log.debug("TimeType");
                break;

            case byteArrayType:
            case ByteArrayType:
                log.debug("ByteArrayType");
                break;
        }
        return dbValue;
    }

    private <T> T getTypedValueReturnedFromGeneratedKeys(Class<T> objectClass, ResultSet rs) throws SQLException {

        Object value = null;
        Types type = Types.getType(objectClass);

        if (type == null) {
            log.warn("Unhandled type " + objectClass);
            return (T) rs.getObject(1);
        }

        switch (type) {

            case integerType:
            case IntegerType:
                value = rs.getInt(1);
                break;

            case longType:
            case LongType:
                value = rs.getLong(1);
                break;

            default:
                value = rs.getObject(1);
        }
        return (T) value;
    }

    // Place code conversions here to prevent type exceptions on setObject
    private void setParameters(PreparedStatement st, Object[] parameters) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("PARAMS: " + Arrays.asList(parameters));
        }

        int n = 1;
        for (Object param : parameters) {

            if (param != null) {

                Types type;
                if (param.getClass().isEnum()) {
                    type = Types.EnumType;
                } else {
                    type = Types.getType(param.getClass());
                }

                assert type != null;

                switch (type) {

                    case booleanType:
                    case BooleanType:
                        st.setBoolean(n, (Boolean) param);
                        break;

                    case byteType:
                    case ByteType:
                        st.setByte(n, (Byte) param);
                        break;

                    case shortType:
                    case ShortType:
                        st.setShort(n, (Short) param);
                        break;

                    case integerType:
                    case IntegerType:
                        st.setInt(n, (Integer) param);
                        break;

                    case longType:
                    case LongType:
                        st.setLong(n, (Long) param);
                        break;

                    case floatType:
                    case FloatType:
                        st.setFloat(n, (Float) param);
                        break;

                    case doubleType:
                    case DoubleType:
                        st.setDouble(n, (Double) param);
                        break;

                    case DecimalType:
                        st.setBigDecimal(n, (BigDecimal) param);
                        break;

                    case StringType:
                        st.setString(n, (String) param);
                        break;

                    case characterType:
                    case CharacterType:
                        st.setObject(n, "" + param);
                        break;

                    case UtilDateType:
                    case SQLDateType:
                        java.util.Date date = (Date) param;
                        st.setDate(n, new java.sql.Date(date.getTime()));
                        break;

                    case TimeType:
                    case TimestampType:
                        // For Time we convert to Timestamp anyway
                        st.setTimestamp(n, (Timestamp) param);
                        break;

                    case LocalDate:
                        LocalDate localDate = (LocalDate) param;
                        st.setTimestamp(n, Timestamp.valueOf(localDate.atStartOfDay()));
                        break;

                    case LocalDateTime:
                        LocalDateTime ldt = (LocalDateTime) param;
                        st.setTimestamp(n, Timestamp.valueOf(ldt));
                        break;

                    case Instant:
                        // todo Instant
                        break;
                    case OffsetDateTime:
                        // todo OffsetDateTime
                        break;
                    case ZonedDateTime:
                        // todo ZonedDateTime
                        break;

                    case byteArrayType:
                    case ByteArrayType:
                        // Blob maps to byte array
                        st.setObject(n, param);
                        break;

                    case ClobType:
                        log.debug("ClobType"); // doesn't occur since Clobs are mapped into Strings
                        st.setClob(n, (Clob) param);
                        break;

                    case BlobType:
                        log.debug("BlobType"); // doesn't occur since Blobs are mapped into byte arrays
                        st.setBlob(n, (Blob) param);
                        break;

                    case EnumType:
                        st.setString(n, param.toString());
                        break;

                    case UUIDType:
                        if (metaData.connectionType == ConnectionTypes.PostgreSQL) {
                            // postgress does work with setObject but not setString unless you set the connection property stringtype=unspecified
                            st.setObject(n, param);
                        } else {
                            st.setString(n, param.toString());
                        }
                        break;

                    default:
                        st.setObject(n, param);
                }

            } else {
                // param is null
                st.setObject(n, param);
            }

            n++;
        }
    }


    // Prevent duplicate "Possible overflow column" and other possibly repeating messages
    private static void warnNoDuplicates(String message) {
        if (!warnings.contains(message)) {
            warnings.add(message);
            log.warn(message);
        }
    }

}
