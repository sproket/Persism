package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.Date;

/**
 * Performs various read and write operations in the database.
 *
 * @author Dan Howard
 * @since 1/8/2021
 */
public final class Session implements AutoCloseable {

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

    /**
     * Close the connection
     */
    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.warn(e.getMessage(), e);
            }
        }
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
     * Function block of database operations to group together in one transaction.
     * This method will set autocommit to false then execute the function, commit and set autocommit back to true.
     * @throws PersismException in case of SQLException where the transaction is rolled back.
     * to make public in 1.1.0
     */
    void withTransaction(Runnable function) {
        try {
            connection.setAutoCommit(false);
            function.run();
            connection.commit();
        } catch (SQLException e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                log.warn(e.getMessage());
            }
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
            List<ColumnInfo> columnInfos = new ArrayList<>(primaryKeys.size());

            Map<String, ColumnInfo> columns = metaData.getColumns(object.getClass(), connection);

            for (String column : changedProperties.keySet()) {
                ColumnInfo columnInfo = columns.get(column);

                if (!primaryKeys.contains(column)) {
                    Object value = allProperties.get(column).getter.invoke(object);

                    params.add(value);
                    columnInfos.add(columnInfo);
                }
            }

            for (String column : primaryKeys) {
                params.add(allProperties.get(column).getter.invoke(object));
                columnInfos.add(metaData.getColumns(object.getClass(), connection).get(column));
            }
            assert params.size() == columnInfos.size();
            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }
            setParameters(st, params.toArray());
            int ret = st.executeUpdate();

            if (object instanceof Persistable) {
                // Save this object state to later detect changed properties
                ((Persistable) object).saveReadState();
            }

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
                } else if (metaData.getConnectionType() == ConnectionTypes.PostgreSQL && column.primary && column.hasDefault) {
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

            List<Object> params = new ArrayList<>(columns.size());
            List<ColumnInfo> columnInfos = new ArrayList<>(columns.size());

            for (ColumnInfo columnInfo : columns.values()) {

                PropertyInfo propertyInfo = properties.get(columnInfo.columnName);
                if (!columnInfo.autoIncrement) {

                    if (columnInfo.hasDefault) {
                        // Do not include if this column has a default and no value has been
                        // set on it's associated property.

                        if (propertyInfo.getter.getReturnType().isPrimitive()) {
                            warnNoDuplicates("Property " + propertyInfo.propertyName + " for column " + columnInfo.columnName + " for class " + object.getClass() +
                                    " should be an Object type to properly detect NULL for defaults (change it from the primitive type to its Boxed version).");
                        }

                        if (propertyInfo.getter.invoke(object) == null) {

                            if (columnInfo.primary) {
                                // This is supported with PostgreSQL but otherwise throw this an exception
                                if (!(metaData.getConnectionType() == ConnectionTypes.PostgreSQL)) {
                                    throw new PersismException("Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.");
                                }
                            }

                            tableHasDefaultColumnValues = true;
                            continue;
                        }
                    }

                    Object value = propertyInfo.getter.invoke(object);

                    params.add(value);
                    columnInfos.add(columnInfo);
                }
            }

            // https://forums.oracle.com/forums/thread.jspa?threadID=879222
            // http://download.oracle.com/javase/1.4.2/docs/guide/jdbc/getstart/statement.html
            //int ret = st.executeUpdate(insertStatement, Statement.RETURN_GENERATED_KEYS);
            assert params.size() == columnInfos.size();
            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }

            setParameters(st, params.toArray());
            st.execute();
            int ret = st.getUpdateCount();

            if (log.isDebugEnabled()) {
                log.debug("insert ret: " + ret);
            }

            if (generatedKeys.size() > 0) {
                rs = st.getGeneratedKeys();
                for (String column : generatedKeys) {
                    if (rs.next()) {

                        Method setter = properties.get(column).setter;
                        Object value = getTypedValueReturnedFromGeneratedKeys(setter.getParameterTypes()[0], rs);

                        if (log.isDebugEnabled()) {
                            log.debug(column + " generated " + value);
                        }
                        setter.invoke(object, value);
                    }
                }
            }

            if (tableHasDefaultColumnValues) {
                // Read the full object back to update any properties which had defaults
                fetch(object);
            } else {
                if (object instanceof Persistable) {
                    // Save this object new state to later detect changed properties
                    ((Persistable) object).saveReadState();
                }
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

            List<Object> params = new ArrayList<>(primaryKeys.size());
            List<ColumnInfo> columnInfos = new ArrayList<>(primaryKeys.size());
            for (String column : primaryKeys) {
                params.add(columns.get(column).getter.invoke(object));
                columnInfos.add(metaData.getColumns(object.getClass(), connection).get(column));
            }

            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
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

    // For unit tests only for now.
    boolean execute(String sql, Object... parameters) {

        if (log.isDebugEnabled()) {
            log.debug("execute: " + sql + " params: " + Arrays.asList(parameters));
        }
        Statement st = null;
        try {

            if (parameters.length == 0) {
                st = connection.createStatement();
                return st.execute(sql);
            } else {
                st = connection.prepareStatement(sql);
                PreparedStatement pst = (PreparedStatement) st;
                setParameters(pst, parameters);
                return pst.execute();
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
        boolean isPOJO = Types.getType(objectClass) == null;

        if (isPOJO && objectClass.getAnnotation(NotTable.class) == null) {
            // Make sure columns are initialized if this is a table.
            metaData.getTableColumnsPropertyInfo(objectClass, connection);
        }

        try {

            exec(result, sql, parameters);

            while (result.rs.next()) {
                if (isPOJO) {
                    // should be getDeclaredConstructor().newInstance() now.
                    T t = objectClass.newInstance();
                    list.add(readObject(t, result.rs));
                } else {
                    list.add((T) readColumn(result.rs, 1, objectClass));
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
        Class<?> objectClass = object.getClass();

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
        List<ColumnInfo> columnInfos = new ArrayList<>(primaryKeys.size());
        Map<String, ColumnInfo> cols = metaData.getColumns(objectClass, connection);
        Result result = new Result();
        try {
            for (String column : primaryKeys) {
                PropertyInfo propertyInfo = properties.get(column);
                params.add(propertyInfo.getter.invoke(object));
                columnInfos.add(cols.get(column));
            }
            assert params.size() == columnInfos.size();

            String sql = metaData.getSelectStatement(object, connection);
            if (log.isDebugEnabled()) {
                log.debug("FETCH " + sql + " PARAMS: " + params);
            }
            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
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
        boolean isPOJO = Types.getType(objectClass) == null;

        Result result = new Result();
        try {

            exec(result, sql, parameters);

            if (result.rs.next()) {

                if (isPOJO) {
                    T t = objectClass.newInstance();
                    readObject(t, result.rs);
                    return t;
                } else {
                    return (T) readColumn(result.rs, 1, objectClass);
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
        // Any properties not marked by NotMapped should have been set (or if they have a getter only)
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
                        columnProperty.setter.invoke(object, value);
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

    private Object readColumn(ResultSet rs, int column, Class<?> returnType) throws SQLException, IOException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int sqlColumnType = rsmd.getColumnType(column);
        String columnName = rsmd.getColumnLabel(column);

        if (returnType.isEnum()) {
            // Some DBs may read an enum type as other 1111 - we can tell it here to read it as a string.
            sqlColumnType = java.sql.Types.CHAR;
        }

        Object value;

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
                    if(clob == null){
                        value = null; //jtds has this problem
                    }else {
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
                    try (InputStream in = blob.getBinaryStream()) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) blob.length());
                        for (int len; (len = in.read(buffer)) != -1; ) {
                            bos.write(buffer, 0, len);
                        }
                        value = bos.toByteArray();
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
            value = convert(value, returnType, columnName);
        }

        return value;
    }

    // Make a sensible conversion of the value type from the DB and the property type defined
    // on the Data class - or the value type from the property to the statement parameter.
    private Object convert(Object value, Class<?> targetType, String columnName) {
        assert value != null;

        Types valueType = Types.getType(value.getClass());

        if (valueType == null) {
            log.warn("Conversion: Unknown Persism type " + value.getClass() + " - no conversion performed.");
            return value;
        }

        Object returnValue = value;

        // try to convert or cast the value to the proper type.
        switch (valueType) {

            case booleanType:
            case BooleanType:
                log.debug("BooleanType");
                break;

            case byteType:
            case ByteType:
                log.warn("MSSQL Sees tinyint as 0-254 - H2 -127-+127 - no conversion here - recommend changing it to SMALLINT/Short.");
                break;

            case shortType:
            case ShortType:
                log.debug(valueType);
                break;

            case integerType:
            case IntegerType:
                // int to bool
                if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                    returnValue = Integer.valueOf("" + value) == 0 ? false : true;

                } else if (targetType.equals(Time.class)) {
                    // SQLite when a Time is defined VIA a convert from LocalTime via Time.valueOf (see getContactForTest)
                    returnValue = new Time(((Integer) value).longValue());

                } else if (targetType.equals(LocalTime.class)) {
                    // SQLite for Time SQLite sees Long, for LocalTime it sees Integer
                    returnValue = new Time((Integer) value).toLocalTime();

                } else if (targetType.equals(Short.class) || targetType.equals(short.class)) {
                    returnValue = Short.parseShort("" + value);

                } else if (targetType.equals(Byte.class) || targetType.equals(byte.class)) {
                    returnValue = Byte.parseByte(""+value);
                }
                break;

            case longType:
            case LongType:
                long lval = Long.valueOf("" + value);
                if (targetType.equals(java.sql.Date.class)) {
                    returnValue = new java.sql.Date(lval);

                } else if (targetType.equals(java.util.Date.class)) {
                    returnValue = new java.util.Date(lval);

                } else if (targetType.equals(Timestamp.class)) {
                    returnValue = new Timestamp(lval);

                } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is INT and Value type is LONG");
                    returnValue = Integer.parseInt("" + lval);

                } else if (targetType.equals(LocalDate.class)) {
                    // SQLite reads long as date.....
                    returnValue = new Timestamp(lval).toLocalDateTime().toLocalDate();

                } else if (targetType.equals(LocalDateTime.class)) {
                    returnValue = new Timestamp(lval).toLocalDateTime();

                } else if (targetType.equals(Time.class)) {
                    // SQLite.... Again.....
                    returnValue = new Time((Long) value);
                }
                break;

            case floatType:
            case FloatType:
                log.debug("FloatType");
                break;

            case doubleType:
            case DoubleType:
                Double dbl = (Double) value;
                // float or doubles to BigDecimal
                if (targetType.equals(BigDecimal.class)) {
                    returnValue = new BigDecimal("" + value);

                } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is FLOAT and Value type is DOUBLE");
                    returnValue = dbl.floatValue();

                } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is INT and Value type is DOUBLE");
                    returnValue = dbl.intValue();

                } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is Long and Value type is DOUBLE");
                    returnValue = dbl.longValue();
                }
                break;

            case BigDecimalType:
                if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                    returnValue = ((Number) value).floatValue();
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is Float and Value type is BigDecimal");

                } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                    returnValue = ((Number) value).doubleValue();
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is Double and Value type is BigDecimal");

                } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                    returnValue = ((Number) value).longValue();
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is Long and Value type is BigDecimal");

                } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                    returnValue = ((Number) value).intValue();
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is Integer and Value type is BigDecimal");

                } else if (targetType.equals(Short.class) || targetType.equals(short.class)) {
                    returnValue = ((Number) value).shortValue();
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is Short and Value type is BigDecimal");

                } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                    // BigDecimal to Boolean. Oracle (sigh) - Additional for a Char to Boolean as then (see TestOracle for links)
                    returnValue = ((Number) value).intValue() == 1;
                    warnNoDuplicates("Possible overflow column " + columnName + " - Target type is Boolean and Value type is BigDecimal");

                } else if (targetType.equals(String.class)) {
                    returnValue = (value).toString();
                }
                break;

            case StringType:
                java.util.Date dval;
                String format;
                if (("" + value).length() > "yyyy-MM-dd".length()) {
                    format = "yyyy-MM-dd hh:mm:ss";
                } else {
                    format = "yyyy-MM-dd";
                }
                DateFormat df = new SimpleDateFormat(format);

                // Read a string but we want a date
                if (targetType.equals(java.util.Date.class) || targetType.equals(java.sql.Date.class)) {
                    // This condition occurs in SQLite when you have a datetime with default annotated
                    // the format returned is 2012-06-02 19:59:49
                    // Used for SQLite returning dates as Strings under some conditions
                    // SQL or others may return STRING yyyy-MM-dd for older legacy 'date' type.
                    // https://docs.microsoft.com/en-us/sql/t-sql/data-types/date-transact-sql?view=sql-server-ver15
                    dval = tryParseDate(value, targetType, columnName, df);

                    if (targetType.equals(java.sql.Date.class)) {
                        returnValue = new java.sql.Date(dval.getTime());
                    } else {
                        returnValue = dval;
                    }

                } else if (targetType.equals(Timestamp.class)) {
                    returnValue = tryParseTimestamp(value, targetType, columnName);

                } else if (targetType.equals(LocalDate.class)) {
                    // JTDS
                    dval = tryParseDate(value, targetType, columnName, df);
                    returnValue = dval.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                } else if (targetType.equals(LocalDateTime.class)) {
                    // JTDS
                    dval = tryParseDate(value, targetType, columnName, df);
                    returnValue = dval.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                } else if (targetType.isEnum()) {
                    // If this is an enum do a case insensitive comparison
                    Object[] enumConstants = targetType.getEnumConstants();
                    for (Object element : enumConstants) {
                        if (("" + value).equalsIgnoreCase(element.toString())) {
                            returnValue = element;
                            break;
                        }
                    }

                } else if (targetType.equals(UUID.class)) {
                    returnValue = UUID.fromString("" + value);

                } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                    // String to Boolean - true or 1 - otherwise false (or null)
                    String bval = "" + value;
                    returnValue = bval.equalsIgnoreCase("true") || bval.equals("1");

                } else if (targetType.equals(Time.class)) {
                    // MSSQL works, JTDS returns Varchar in format below with varying decimal numbers
                    // which won't format unless I use Exact so I chop of the milliseconds.
                    DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
                    String sval = "" + value;
                    if (sval.indexOf('.') > -1) {
                        sval = sval.substring(0, sval.indexOf('.'));
                    }
                    dval = tryParseDate(sval, targetType, columnName, timeFormat);
                    returnValue = new Time(dval.getTime());

                } else if (targetType.equals(LocalTime.class)) {
                    // JTDS Fails again...
                    returnValue = LocalTime.parse("" + value);

                } else if (targetType.equals(Character.class) || targetType.equals(char.class)) {
                    String s = "" + value;
                    if (s.length() > 0) {
                        returnValue = s.charAt(0);
                    }

                } else if (targetType.equals(BigDecimal.class)) {
                    try {
                        returnValue = new BigDecimal("" + value);
                    } catch (NumberFormatException e) {
                        String msg = "NumberFormatException: Column: " + columnName + " Type of property: " + targetType + " - Type read: " + value.getClass() + " VALUE: " + value;
                        throw new PersismException(msg, e);
                    }
                }
                break;

            case characterType:
            case CharacterType:
                log.debug("CharacterType");
                break;

            case LocalDateType:
                log.debug("LocalDateType");
                returnValue = java.sql.Date.valueOf((LocalDate) value);
                break;

            case LocalDateTimeType:
                log.debug("LocalDateTimeType");
                returnValue = Timestamp.valueOf((LocalDateTime) value);
                break;

            case LocalTimeType:
                log.debug("LocalTimeType");
                returnValue = Time.valueOf((LocalTime) value);
                break;

            case UtilDateType:
            case SQLDateType:
            case TimestampType:
                if (targetType.equals(java.util.Date.class)) {
                    returnValue = new java.util.Date(((Date) value).getTime());

                } else if (targetType.equals(java.sql.Date.class)) {
                    returnValue = new java.sql.Date(((Date) value).getTime());

                } else if (targetType.equals(Timestamp.class)) {
                    returnValue = new Timestamp(((Date) value).getTime());

                } else if (targetType.equals(LocalDate.class)) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                } else if (targetType.equals(LocalDateTime.class)) {
                    Date dt = new Date(((Date) value).getTime());
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                } else if (targetType.equals(Time.class)) {
                    // Oracle doesn't seem to have Time so we use Timestamp
                    returnValue = new Time(((Date) value).getTime());

                } else if (targetType.equals(LocalTime.class)) {
                    // Oracle.... Sigh
                    Date dt = (Date) value;
                    returnValue = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime();
                }
                break;

            case TimeType:
                log.debug("TimeType");
                if (targetType.equals(LocalTime.class)) {
                    returnValue = LocalTime.parse("" + value);
                }
                break;

            case InstantType:
            case OffsetDateTimeType:
            case ZonedDateTimeType:
                log.warn(valueType + " not yet supported", new Throwable());
                break;

            case byteArrayType:
            case ByteArrayType:
                log.debug("ByteArrayType");
                if (targetType.equals(UUID.class)) {
                    returnValue = Util.asUuid((byte[]) value);
                }
                break;

            case ClobType:
            case BlobType:
                log.warn("why? Clob is read as String, Blob as byte array - see readColumn method", new Throwable());
                break;

            case EnumType:
                // No need to convert it here.
                // If it's being used for the property setter then it's OK
                // If it's being used by setParameters it's converted to String
                // The String case above converts from the String to the Enum
                log.debug("EnumType");
                break;

            case UUIDType:
                log.debug("UUIDType");
                if (targetType.equals(Blob.class) || targetType.equals(byte[].class) || targetType.equals(Byte[].class)) {
                    returnValue = Util.asBytes((UUID) value);
                }
                break;

            case ObjectType:
                log.debug("ObjectType");
                break;
        }
        return returnValue;
    }

    /*
     * Used by convert for convenience - common possible parsing
     */
    private Date tryParseDate(Object value, Class targetType, String columnName, DateFormat df) throws PersismException {
        try {
            return df.parse("" + value);
        } catch (ParseException e) {
            String msg = e.getMessage() + ". Column: " + columnName + " Target Conversion: " + targetType + " - Type read: " + value.getClass() + " VALUE: " + value;
            throw new PersismException(msg, e);
        }
    }

    private Timestamp tryParseTimestamp(Object value, Class targetType, String columnName) throws PersismException {
        try {
            return Timestamp.valueOf("" + value);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() + ". Column: " + columnName + " Target Conversion: " + targetType + " - Type read: " + value.getClass() + " VALUE: " + value;
            throw new PersismException(msg, e);
        }
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

    void setParameters(PreparedStatement st, Object[] parameters) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("PARAMS: " + Arrays.asList(parameters));
        }

        int n = 1;
        for (Object param : parameters) {

            if (param != null) {

                Types type = Types.getType(param.getClass());
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

                    case BigDecimalType:
                        st.setBigDecimal(n, (BigDecimal) param);
                        break;

                    case BigIntegerType:
                        st.setString(n, "" + param);
                        break;

                    case StringType:
                        st.setString(n, (String) param);
                        break;

                    case characterType:
                    case CharacterType:
                        st.setObject(n, "" + param);
                        break;

                    case SQLDateType:
                        st.setDate(n, (java.sql.Date) param);
                        break;

                    case TimeType:
                        st.setTime(n, (Time) param);
                        break;

                    case TimestampType:
                        st.setTimestamp(n, (Timestamp) param);
                        break;

                    // THESE are converted to Timestamp (or Time) by convert method.
                    case LocalTimeType:
                    case LocalDateType:
                    case LocalDateTimeType:
                        log.warn(type + " why would this occur in setParameters?", new Throwable());
                        break;

                    case OffsetDateTimeType:
                    case ZonedDateTimeType:
                    case InstantType:
                        // todo ZonedDateTime, OffsetDateTimeType and MAYBE Instant
                        break;

                    case byteArrayType:
                    case ByteArrayType:
                        // Blob maps to byte array
                        st.setBytes(n, (byte[]) param);
                        break;

                    case ClobType:
                    case BlobType:
                        // Clob is converted to String Blob is converted to byte array
                        // So this should not occur.
                        log.warn(type + " why would this occur in setParameters?", new Throwable());
                        break;

                    case EnumType:
                        if (metaData.getConnectionType() == ConnectionTypes.PostgreSQL) {
                            st.setObject(n, param.toString(), java.sql.Types.OTHER);
                        } else {
                            st.setString(n, param.toString());
                        }
                        break;

                    case UUIDType:
                        if (metaData.getConnectionType() == ConnectionTypes.PostgreSQL) {
                            // PostgreSQL does work with setObject but not setString unless you set the connection property stringtype=unspecified
                            st.setObject(n, param);
                        } else {
                            st.setString(n, param.toString());
                        }
                        break;

                    default:
                        // Usually SQLite with util.date - setObject works
                        log.info("setParameters default: " + n + " " + param.getClass());
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
        //noinspection OptionalGetWithoutIsPresent
        String additional = Arrays.stream(new Throwable().getStackTrace()).skip(2).findFirst().get().toString().trim();
        String msg = message + " " + additional;
        if (!warnings.contains(msg)) {
            warnings.add(msg);
            log.warn(msg);
        }
    }

}
