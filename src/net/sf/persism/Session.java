package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import static net.sf.persism.Util.isRecord;

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

    private Reader reader;
    private Convertor convertor;

    /**
     * Default constructor for a Session object
     * @param connection db connection
     * @throws PersismException if something goes wrong
     */
    public Session(Connection connection) throws PersismException {
        this.connection = connection;
        init(connection, null);
    }

    /**
     * Constructor for Session where you want to specify the Session Key.
     * @param connection db connection
     * @param sessionKey Unique string to represent the connection URL if it is not available on the Connection metadata.
     *                   This string should start with the jdbc url string to indicate the connection type.
     *<pre>
     *                  jdbc:h2 = h2
     *                  jdbc:sqlserver = MS SQL
     *                  jdbc:oracle = Oracle
     *                  jdbc:sqlite = SQLite
     *                  jdbc:derby = Derby
     *                  jdbc:mysql = MySQL/MariaDB
     *                  jdbc:postgresql = PostgreSQL
     *                  jdbc:firebirdsql = Firebird (Jaybird)
     *                  jdbc:hsqldb = HSQLDB
     *                  jdbc:ucanaccess = MS Access
     *                  jdbc:informix = Informix
     *
     </pre>
     * @throws PersismException if something goes wrong
     */
    public Session(Connection connection, String sessionKey) throws PersismException {
        this.connection = connection;
        init(connection, sessionKey);
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

    private void init(Connection connection, String sessionKey) {
        // place any DB specific properties here.
        try {
            metaData = MetaData.getInstance(connection, sessionKey);
        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        }

        convertor = new Convertor();
        reader = new Reader(this);
    }

    /**
     * Function block of database operations to group together in one transaction.
     * This method will set autocommit to false then execute the function, commit and set autocommit back to true.
     * <pre>{@code
     * session.withTransaction(() -> {
     *     Contact contact = getContactFromSomewhere();
     *
     *     contact.setIdentity(randomUUID);
     *     session.insert(contact);
     *
     *     contact.setContactName("Wilma Flintstone");
     *
     *     session.update(contact);
     *     session.fetch(contact);
     * });
     * }</pre>
     *
     * @param transactionBlock Block of operations expected to run as a single transaction.
     * @throws PersismException in case of SQLException where the transaction is rolled back.
     */
    public void withTransaction(Runnable transactionBlock) {
        try {
            connection.setAutoCommit(false);
            transactionBlock.run();
            connection.commit();
        } catch (Exception e) {
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
            List<ColumnInfo> columnInfos = new ArrayList<>(changedProperties.size());

            Map<String, ColumnInfo> columns = metaData.getColumns(object.getClass(), connection);

            for (String column : changedProperties.keySet()) {
                ColumnInfo columnInfo = columns.get(column);

                if (primaryKeys.contains(column)) {
                    log.info("Session update: skipping column " + column);
                } else {
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
                    params.set(j, convertor.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
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
     * @param <T> Type of the inserted object
     * @param object the data object to insert.
     * @return usually 1 to indicate rows changed via JDBC.
     * @throws PersismException When planet of the apes starts happening.
     */
    public <T> Result<T> insert(Object object) throws PersismException {
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

            boolean refreshAfterInsert = false;

            List<Object> params = new ArrayList<>(columns.size());
            List<ColumnInfo> columnInfos = new ArrayList<>(columns.size());

            for (ColumnInfo columnInfo : columns.values()) {

                PropertyInfo propertyInfo = properties.get(columnInfo.columnName);
                if (propertyInfo.getter == null) {
                    throw new PersismException(String.format("Class %s has no getter for property %s", object.getClass(), propertyInfo.propertyName));
                }
                if (!columnInfo.autoIncrement) {

                    if (columnInfo.hasDefault) {
                        // Do not include if this column has a default and no value has been
                        // set on it's associated property.
                        if (propertyInfo.getter.getReturnType().isPrimitive()) {
                            log.warnNoDuplicates("Property " + propertyInfo.propertyName + " for column " + columnInfo.columnName + " for class " + object.getClass() +
                                    " should be an Object type to properly detect NULL for defaults (change it from the primitive type to its Boxed version).");
                        }

                        if (propertyInfo.getter.invoke(object) == null) {

                            if (columnInfo.primary) {
                                // This is supported with PostgreSQL but otherwise throw this an exception
                                if (!(metaData.getConnectionType() == ConnectionTypes.PostgreSQL)) {
                                    throw new PersismException("Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.");
                                }
                            }

                            refreshAfterInsert = true;
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
                    params.set(j, convertor.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }

            setParameters(st, params.toArray());
            st.execute();
            int ret = st.getUpdateCount();

            log.debug("insert return count after insert: %s", ret);

            List<Object> primaryKeyValues = new ArrayList<>();
            if (generatedKeys.size() > 0) {
                rs = st.getGeneratedKeys();
                PropertyInfo propertyInfo;
                for (String column : generatedKeys) {
                    if (rs.next()) {

                        propertyInfo = properties.get(column);

                        Method setter = propertyInfo.setter;
                        Object value;
                        if (setter != null) {
                            value = getTypedValueReturnedFromGeneratedKeys(setter.getParameterTypes()[0], rs);
                            setter.invoke(object, value);
                        } else {
                            // Set read-only property by field ONLY FOR NON-RECORDS.
                            value = getTypedValueReturnedFromGeneratedKeys(propertyInfo.field.getType(), rs);
                            if (!isRecord(object.getClass())) {
                                propertyInfo.field.setAccessible(true);
                                propertyInfo.field.set(object, value);
                                propertyInfo.field.setAccessible(false);
                                log.debug("insert %s generated %s", column, value);
                            }
                        }

                        primaryKeyValues.add(value);
                    }
                }
            }

            // If it's a record we can't assign the autoinc so we need a refresh
            if (generatedKeys.size() > 0 && isRecord(object.getClass())) {
                refreshAfterInsert = true;
            }

            if (refreshAfterInsert) {
                // Read the full object back to update any properties which had defaults
                if (isRecord(object.getClass())) {
                    //SQL sql = new SQL(metaData.getDefaultSelectStatement(object.getClass(), connection));
                    //object = fetch(object.getClass(), sql, params(primaryKeyValues.toArray()));
                    object = fetch(object.getClass(), metaData.getSelectStatement(object, connection), primaryKeyValues.toArray());
                } else {
                    fetch(object);
                }
            }

            if (object instanceof Persistable) {
                // Save this object new state to later detect changed properties
                ((Persistable<?>) object).saveReadState();
            }

            //noinspection unchecked
            return new Result<>(ret, (T) object);
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
            List<ColumnInfo> columnInfos = new ArrayList<>(columns.size());
            for (String column : primaryKeys) {
                params.add(columns.get(column).getter.invoke(object));
                columnInfos.add(metaData.getColumns(object.getClass(), connection).get(column));
            }

            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, convertor.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
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

        log.debug("execute: %s params: %s", sql, Arrays.asList(parameters));

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

        JDBCResult result = new JDBCResult();

        // If we know this type it means it's a primitive type. Not a DAO so we use a different rule to read those
        boolean isPOJO = Types.getType(objectClass) == null;
        boolean isRecord = isPOJO && isRecord(objectClass);

        if (isPOJO && objectClass.getAnnotation(NotTable.class) == null) {
            // Make sure columns are initialized if this is a table.
            metaData.getTableColumnsPropertyInfo(objectClass, connection);
        }

        try {
            // we don't check parameter types here? Nope - we don't know anything at this point.
            exec(result, sql, parameters);

            while (result.rs.next()) {
                if (isRecord) {
                    list.add(reader.readRecord(objectClass, result.rs));
                } else if (isPOJO) {
                    T t = objectClass.getDeclaredConstructor().newInstance();
                    list.add(reader.readObject(t, result.rs));
                } else {
                    list.add(reader.readColumn(result.rs, 1, objectClass));
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

        if (isRecord(objectClass)) {
            throw new PersismException("Cannot read a Record type object with this method.");
        }

        List<String> primaryKeys = metaData.getPrimaryKeys(objectClass, connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException("Cannot perform FETCH - " + metaData.getTableName(objectClass) + " has no primary keys.");
        }

        Map<String, PropertyInfo> properties = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);
        List<Object> params = new ArrayList<>(primaryKeys.size());
        List<ColumnInfo> columnInfos = new ArrayList<>(properties.size());
        Map<String, ColumnInfo> cols = metaData.getColumns(objectClass, connection);
        JDBCResult result = new JDBCResult();
        try {
            for (String column : primaryKeys) {
                PropertyInfo propertyInfo = properties.get(column);
                params.add(propertyInfo.getter.invoke(object));
                columnInfos.add(cols.get(column));
            }
            assert params.size() == columnInfos.size();

            String sql = metaData.getSelectStatement(object, connection);
            log.debug("FETCH %s PARAMS: %s", sql, params);
            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, convertor.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }
            exec(result, sql, params.toArray());

            if (result.rs.next()) {
                reader.readObject(object, result.rs);
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
        boolean isRecord = isPOJO && isRecord(objectClass);

        JDBCResult result = new JDBCResult();
        try {

            exec(result, sql, parameters);

            if (result.rs.next()) {
                if (isRecord) {
                    return reader.readRecord(objectClass, result.rs);
                } else if (isPOJO) {
                    T t = objectClass.getDeclaredConstructor().newInstance();
                    return reader.readObject(t, result.rs);
                } else {
                    return reader.readColumn(result.rs, 1, objectClass);
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

    Convertor getConvertor() {
        return convertor;
    }

    Connection getConnection() {
        return connection;
    }
    /*
    Private methods
     */

    private JDBCResult exec(JDBCResult result, String sql, Object... parameters) throws SQLException {
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
            log.debug("PARAMS: %s", Arrays.asList(parameters));
        }

        int n = 1;
        for (Object param : parameters) {

            if (param != null) {

                Types type = Types.getType(param.getClass());
                if (type == null) {
                    log.warn("setParameters: Unknown type: " + param.getClass());
                    type = Types.ObjectType;
                }

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
                        // Also if it's a custom non-standard type.
                        log.info("setParameters using setObject on parameter: " + n + " for " + param.getClass());
                        st.setObject(n, param);
                }

            } else {
                // param is null
                st.setObject(n, param);
            }

            n++;
        }
    }

}
