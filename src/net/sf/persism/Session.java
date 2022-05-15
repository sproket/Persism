package net.sf.persism;

import net.sf.persism.annotations.NotTable;
import net.sf.persism.annotations.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import static net.sf.persism.Parameters.none;
import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;
import static net.sf.persism.Util.isRecord;

/**
 * Performs various read and write operations in the database.
 *
 * @author Dan Howard
 * @since 1/8/2021
 */
public final class Session implements AutoCloseable {

    private static final Log log = Log.getLogger(Session.class);
    private static final Log blog = Log.getLogger("net.sf.persism.Benchmarks");

    final SessionHelper helper;

    Connection connection;
    MetaData metaData;
    Reader reader;
    Converter converter;

    /**
     * @param connection db connection
     * @throws PersismException if something goes wrong
     */
    public Session(Connection connection) throws PersismException {
        this.connection = connection;
        helper = new SessionHelper(this);
        init(connection, null);
    }

    /**
     * Constructor for Session where you want to specify the Session Key.
     *
     * @param connection db connection
     * @param sessionKey Unique string to represent the connection URL if it is not available on the Connection metadata.
     *                   This string should start with the jdbc url string to indicate the connection type.
     *                   <code>
     *                   <br>
     *                   <br>   jdbc:h2 = h2
     *                   <br>   jdbc:sqlserver = MS SQL
     *                   <br>   jdbc:oracle = Oracle
     *                   <br>   jdbc:sqlite = SQLite
     *                   <br>   jdbc:derby = Derby
     *                   <br>   jdbc:mysql = MySQL/MariaDB
     *                   <br>   jdbc:postgresql = PostgreSQL
     *                   <br>   jdbc:firebirdsql = Firebird (Jaybird)
     *                   <br>   jdbc:hsqldb = HSQLDB
     *                   <br>   jdbc:ucanaccess = MS Access
     *                   <br>   jdbc:informix = Informix
     *                   </code>
     * @throws PersismException if something goes wrong
     */
    public Session(Connection connection, String sessionKey) throws PersismException {
        this.connection = connection;
        helper = new SessionHelper(this);
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

        converter = new Converter();
        reader = new Reader(this);
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
            throw new PersismException(Messages.OperationNotSupportedForJavaType.message(objectClass, "FETCH"));
        }

        if (isRecord(objectClass)) {
            throw new PersismException(Messages.OperationNotSupportedForRecord.message(objectClass, "FETCH"));
        }

        if (objectClass.getAnnotation(View.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForView.message(objectClass, "FETCH"));
        }

        if (objectClass.getAnnotation(NotTable.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForNotTableQuery.message(objectClass, "FETCH"));
        }

        List<String> primaryKeys = metaData.getPrimaryKeys(objectClass, connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeys.message("FETCH", metaData.getTableName(objectClass)));
        }

        Map<String, PropertyInfo> properties = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);
        Parameters params = new Parameters();

        List<ColumnInfo> columnInfos = new ArrayList<>(properties.size());
        Map<String, ColumnInfo> cols = metaData.getColumns(objectClass, connection);
        JDBCResult result = new JDBCResult();
        try {
            for (String column : primaryKeys) {
                PropertyInfo propertyInfo = properties.get(column);
                params.add(propertyInfo.getValue(object));
                columnInfos.add(cols.get(column));
            }
            assert params.size() == columnInfos.size();

            String sql = metaData.getDefaultSelectStatement(object.getClass(), connection);
            log.debug("FETCH %s PARAMS: %s", sql, params);
            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, converter.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }

            helper.exec(result, sql, params.toArray());

            verifyPropertyInfoForQuery(objectClass, properties, result.rs);

            if (result.rs.next()) {
                reader.readObject(object, properties, result.rs);
                helper.handleJoins(object, objectClass, sql, params);
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
     * Fetch object by primary key(s)
     *
     * @param objectClass      Type to return (should be a POJO data class or a record)
     * @param primaryKeyValues primary key values
     * @param <T>              Type
     * @return Instance of object type T or NULL if not found
     * @throws PersismException if you pass a Java primitive or other invalid type for objectClass or something else goes wrong.
     */
    public <T> T fetch(Class<T> objectClass, Parameters primaryKeyValues) {
        if (objectClass.getAnnotation(NotTable.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForNotTableQuery.message(objectClass, "FETCH w/o specifying the SQL"));
        }

        // View does not have any good way to know about primary keys
        if (objectClass.getAnnotation(View.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForView.message(objectClass, "FETCH w/o specifying the SQL with @View"));
        }

        if (Types.getType(objectClass) != null) {
            throw new PersismException(Messages.OperationNotSupportedForJavaType.message(objectClass, "FETCH"));
        }
        primaryKeyValues.areKeys = true;

        SQL sql = new SQL(metaData.getDefaultSelectStatement(objectClass, connection));
        return fetch(objectClass, sql, primaryKeyValues);
    }

    /**
     * Fetch object by arbitrary SQL
     *
     * @param objectClass Type to return
     * @param sql         SQL query
     * @param <T>         Type
     * @return Instance of object type T or NULL if not found
     * @throws PersismException if something goes wrong.
     */
    public <T> T fetch(Class<T> objectClass, SQL sql) {
        return fetch(objectClass, sql, none());
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
     * @throws PersismException Well, this is a runtime exception, so it actually could be anything really.
     */
    public <T> T fetch(Class<T> objectClass, SQL sql, Parameters parameters) {
        // If we know this type it means it's a primitive type. Not a DAO so we use a different rule to read those
        boolean isPOJO = Types.getType(objectClass) == null;
        boolean isRecord = isPOJO && isRecord(objectClass);

        JDBCResult result = JDBCResult.DEFAULT;
        try {
            result = helper.executeQuery(objectClass, sql, parameters);

            Map<String, PropertyInfo> properties = Collections.emptyMap();
            if (isPOJO) {
                if (objectClass.getAnnotation(NotTable.class) == null) {
                    properties = metaData.getTableColumnsPropertyInfo(objectClass, connection);
                } else {
                    properties = metaData.getQueryColumnsPropertyInfo(objectClass, result.rs);
                }
            }

            if (result.rs.next()) {
                if (isRecord) {
                    List<String> propertyNames = metaData.getPropertyNames(objectClass);
                    Constructor<T> selectedConstructor = helper.findConstructor(objectClass, propertyNames);
                    RecordInfo<T> recordInfo = new RecordInfo<T>(objectClass, selectedConstructor, this, result.rs);
                    var ret = reader.readRecord(recordInfo, result.rs);
                    helper.handleJoins(ret, objectClass, sql.toString(), parameters);
                    return ret;

                } else if (isPOJO) {
                    T t = objectClass.getDeclaredConstructor().newInstance();
                    verifyPropertyInfoForQuery(objectClass, properties, result.rs);
                    var ret = reader.readObject(t, properties, result.rs);
                    helper.handleJoins(ret, objectClass, sql.toString(), parameters);
                    return ret;

                } else {
                    ResultSetMetaData rsmd = result.rs.getMetaData();
                    //noinspection unchecked
                    return (T) reader.readColumn(result.rs, 1, rsmd.getColumnType(1), rsmd.getColumnLabel(1), objectClass);
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

    /**
     * @hidden
     * @deprecated
     */
    public <T> T fetch(Class<T> objectClass, String sql, Object... parameters) {
        return fetch(objectClass, new SQL(sql), new Parameters(parameters));
    }

    /**
     * Query to return all results.
     *
     * @param objectClass Type of returned value
     * @param <T>         Return type
     * @return List of type T read from the database
     * @throws PersismException Oof.
     */
    public <T> List<T> query(Class<T> objectClass) {
        if (objectClass.getAnnotation(NotTable.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForNotTableQuery.message(objectClass, "QUERY w/o specifying the SQL"));
        }

        if (Types.getType(objectClass) != null) {
            throw new PersismException(Messages.OperationNotSupportedForJavaType.message(objectClass, "QUERY w/o specifying the SQL"));
        }
        SQL sql = sql(metaData.getSelectStatement(objectClass, connection));
        return query(objectClass, sql, none());
    }

    /**
     * Query for any arbitrary SQL statement.
     *
     * @param objectClass Type of returned value
     * @param sql         SQL to use for Querying
     * @param <T>         Return type
     * @return List of type T read from the database
     * @throws PersismException He's dead Jim!
     */
    public <T> List<T> query(Class<T> objectClass, SQL sql) {
        return query(objectClass, sql, none());
    }

    /**
     * Query to return any results matching the primary key values provided.
     *
     * @param objectClass      Type of returned value
     * @param primaryKeyValues Parameters containing primary key values
     * @param <T>              Return type
     * @return List of type T read from the database of any rows matching the primary keys. If you pass multiple primaries this will use WHERE IN(?,?,?) to find them.
     * @throws PersismException Oh no. Not again.
     */
    public <T> List<T> query(Class<T> objectClass, Parameters primaryKeyValues) {

        // NotTable requires SQL - we don't know what SQL to use here.
        if (objectClass.getAnnotation(NotTable.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForNotTableQuery.message(objectClass, "QUERY w/o specifying the SQL"));
        }

        // View does not have any good way to know about primary keys
        if (objectClass.getAnnotation(View.class) != null && primaryKeyValues.size() > 0) {
            throw new PersismException(Messages.OperationNotSupportedForView.message(objectClass, "QUERY w/o specifying the SQL with @View since we don't have Primary Keys"));
        }

        // Requires a POJO or Record
        if (Types.getType(objectClass) != null) {
            throw new PersismException(Messages.OperationNotSupportedForJavaType.message(objectClass, "QUERY"));
        }

        if (primaryKeyValues.size() == 0) {
            return query(objectClass); // select all
        }

        List<String> primaryKeys = metaData.getPrimaryKeys(objectClass, connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeys.message("QUERY", metaData.getTableName(objectClass)));
        }

        primaryKeyValues.areKeys = true;

        if (primaryKeyValues.size() == primaryKeys.size()) {
            // single select
            return query(objectClass, sql(metaData.getDefaultSelectStatement(objectClass, connection)), primaryKeyValues);
        }

        String query = metaData.getPrimaryInClause(objectClass, primaryKeyValues.size(), connection);
        SQL sql = sql(query);
        return query(objectClass, sql, primaryKeyValues);
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
    public <T> List<T> query(Class<T> objectClass, SQL sql, Parameters parameters) {
        List<T> list = new ArrayList<>(32);

        // If we know this type it means it's a primitive type. Not a DAO so we use a different rule to read those
        boolean isPOJO = Types.getType(objectClass) == null;
        boolean isRecord = isPOJO && isRecord(objectClass);

        long now = System.currentTimeMillis();
        JDBCResult result = JDBCResult.DEFAULT;
        try {
            result = helper.executeQuery(objectClass, sql, parameters);


            Map<String, PropertyInfo> properties = Collections.emptyMap();
            if (isPOJO) {
                if (objectClass.getAnnotation(NotTable.class) == null) {
                    properties = metaData.getTableColumnsPropertyInfo(objectClass, connection);
                } else {
                    properties = metaData.getQueryColumnsPropertyInfo(objectClass, result.rs);
                }
            }

            if (isRecord) {
                List<String> propertyNames = metaData.getPropertyNames(objectClass);
                Constructor<T> selectedConstructor = helper.findConstructor(objectClass, propertyNames);

                RecordInfo<T> recordInfo = new RecordInfo<>(objectClass, selectedConstructor, this, result.rs);

                while (result.rs.next()) {
                    var record = reader.readRecord(recordInfo, result.rs);
                    list.add(record);
                }
            } else if (isPOJO) {
                verifyPropertyInfoForQuery(objectClass, properties, result.rs);

                while (result.rs.next()) {
                    T t = objectClass.getDeclaredConstructor().newInstance();
                    list.add(reader.readObject(t, properties, result.rs));
                }
            } else {
                ResultSetMetaData rsmd = result.rs.getMetaData();
                while (result.rs.next()) {
                    //noinspection unchecked
                    list.add((T) reader.readColumn(result.rs, 1, rsmd.getColumnType(1), rsmd.getColumnLabel(1), objectClass));
                }
            }

            //blog.debug("TIME TO READ " + objectClass + " " + (System.currentTimeMillis() - now) + " SIZE " + list.size());
            blog.debug("READ time: %s SIZE: %s %s", (System.currentTimeMillis() - now), list.size(), objectClass);

            if (list.size() > 0) {
                now = System.currentTimeMillis();
                helper.handleJoins(list, objectClass, sql.toString(), parameters);
            }

            if (blog.isDebugEnabled()) {
                blog.debug("handleJoins TIME:  " + (System.currentTimeMillis() - now) + " " + objectClass, new Throwable());
            }

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(result.st, result.rs);
        }

        return list;
    }

    private void verifyPropertyInfoForQuery(Class<?> objectClass, Map<String, PropertyInfo> properties, ResultSet rs) throws SQLException {

        // Test if all properties have column mapping (skipping joins) and throw PersismException if not
        // This block verifies that the object is fully initialized.
        // Any properties not marked by NotColumn should have been set (or if they have a getter only)
        // If not throw a PersismException
        Collection<PropertyInfo> allProperties = MetaData.getPropertyInfo(objectClass).stream().filter(p -> !p.isJoin).toList();
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

            throw new PersismException(Messages.ObjectNotProperlyInitialized.message(objectClass, sb));
        }

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<String> foundColumns = new ArrayList<>(columnCount);

        for (int j = 1; j <= columnCount; j++) {

            String columnName = rsmd.getColumnLabel(j);
            PropertyInfo columnProperty = reader.getPropertyInfo(columnName, properties);
            //ColumnInfo columnInfo = getMetaData().get
            if (columnProperty != null) {
                foundColumns.add(columnName);
            }
        }

        // This tests for when a user writes their own SQL and forgets a column.
        if (foundColumns.size() < properties.keySet().size()) {

            Set<String> missing = new LinkedHashSet<>(columnCount);
            missing.addAll(properties.keySet());
            foundColumns.forEach(missing::remove);

            throw new PersismException(Messages.ObjectNotProperlyInitializedByQuery.message(objectClass, foundColumns, missing));
        }

    }

    /**
     * @hidden
     * @deprecated
     */
    public <T> List<T> query(Class<T> objectClass, String sql, Object... parameters) {
        return query(objectClass, new SQL(sql), new Parameters(parameters));
    }


    /* ****************************** Write methods ****************************************/

    /**
     * Updates the data object in the database.
     *
     * @param object data object to update.
     * @return Result object containing rows changed (usually 1 to indicate rows changed via JDBC) and the data object itself which may have been changed.
     * @throws PersismException Indicating the upcoming robot uprising.
     */
    public <T> Result<T> update(T object) throws PersismException {
        helper.checkIfOkForWriteOperation(object, "UPDATE");

        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeys.message("UPDATE", metaData.getTableName(object.getClass())));
        }

        PreparedStatement st = null;
        try {

            String updateStatement = null;
            try {
                updateStatement = metaData.getUpdateStatement(object, connection);
                log.debug(updateStatement);
            } catch (NoChangesDetectedForUpdateException e) {
                log.info("No properties changed. No update required for Object: " + object + " class: " + object.getClass().getName());
                return new Result<>(0, (T) object);
            }

            st = connection.prepareStatement(updateStatement);

            // These keys should always be in sorted order.
            Map<String, PropertyInfo> allProperties = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);
            Map<String, PropertyInfo> changedProperties;
            if (object instanceof Persistable<?> pojo) {
                changedProperties = metaData.getChangedProperties(pojo, connection);
            } else {
                changedProperties = allProperties;
            }

            List<Object> params = new ArrayList<>(primaryKeys.size());
            List<ColumnInfo> columnInfos = new ArrayList<>(changedProperties.size());

            Map<String, ColumnInfo> columns = metaData.getColumns(object.getClass(), connection);

            for (String column : changedProperties.keySet()) {
                ColumnInfo columnInfo = columns.get(column);

                if (primaryKeys.contains(column)) {
                    log.debug("Session update: skipping column %s", column);
                } else {
                    Object value = allProperties.get(column).getValue(object);
                    params.add(value);
                    columnInfos.add(columnInfo);
                }
            }

            for (String column : primaryKeys) {
                params.add(allProperties.get(column).getValue(object));
                columnInfos.add(metaData.getColumns(object.getClass(), connection).get(column));
            }
            assert params.size() == columnInfos.size();
            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, converter.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }
            helper.setParameters(st, params.toArray());
            int ret = st.executeUpdate();

            if (object instanceof Persistable<?> pojo) {

                // Save this object state to later detect changed properties
                pojo.saveReadState();
            }

            return new Result<>(ret, object);

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
     * @param <T>    Type of the returning data object in Result.
     * @return Result object containing rows changed (usually 1 to indicate rows changed via JDBC) and the data object itself which may have been changed by auto-inc or column defaults.
     * @throws PersismException When planet of the apes starts happening.
     */
    public <T> Result<T> insert(T object) throws PersismException {
        helper.checkIfOkForWriteOperation(object, "INSERT");

        String insertStatement = metaData.getInsertStatement(object, connection);
        log.debug(insertStatement);

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
                    throw new PersismException(Messages.ClassHasNoGetterForProperty.message(object.getClass(), propertyInfo.propertyName));
                }
                if (!columnInfo.autoIncrement) {

                    if (columnInfo.hasDefault) {
                        // Do not include if this column has a default and no value has been
                        // set on it's associated property.
                        if (propertyInfo.getter.getReturnType().isPrimitive()) {
                            log.warnNoDuplicates(Messages.PropertyShouldBeAnObjectType.message(propertyInfo.propertyName, columnInfo.columnName, object.getClass()));
                        }

                        if (propertyInfo.getValue(object) == null) {

                            if (columnInfo.primary) {
                                // This is supported with PostgreSQL but otherwise throw this an exception
                                if (!(metaData.getConnectionType() == ConnectionTypes.PostgreSQL)) {
                                    throw new PersismException(Messages.NonAutoIncGeneratedNotSupported.message());
                                }
                            }

                            refreshAfterInsert = true;
                            continue;
                        }
                    }

                    Object value = propertyInfo.getValue(object);

                    params.add(value);
                    columnInfos.add(columnInfo);
                }
            }

            assert params.size() == columnInfos.size();

            for (int j = 0; j < params.size(); j++) {
                ColumnInfo columnInfo = columnInfos.get(j);
                if (params.get(j) != null) {
                    params.set(j, converter.convert(params.get(j), columnInfo.columnType.getJavaType(), columnInfo.columnName));
                }
            }

            helper.setParameters(st, params.toArray());
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
                            value = helper.getTypedValueReturnedFromGeneratedKeys(setter.getParameterTypes()[0], rs);
                            setter.invoke(object, value);
                        } else {
                            // Set read-only property by field ONLY FOR NON-RECORDS.
                            value = helper.getTypedValueReturnedFromGeneratedKeys(propertyInfo.field.getType(), rs);
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

            Object returnObject = null;
            if (refreshAfterInsert) {
                // these 2 fetches need a fetchAfterInsert flag
                // Read the full object back to update any properties which had defaults
                if (isRecord(object.getClass())) {
                    SQL sql = new SQL(metaData.getDefaultSelectStatement(object.getClass(), connection));
                    returnObject = fetch(object.getClass(), sql, params(primaryKeyValues.toArray()));
                } else {
                    fetch(object);
                    returnObject = object;
                }
            } else {
                returnObject = object;
            }

            if (object instanceof Persistable<?> pojo) {
                // Save this object new state to later detect changed properties
                pojo.saveReadState();
            }

            //noinspection unchecked
            return new Result<>(ret, (T) returnObject);
        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(st, rs);
        }
    }

    /**
     * Deletes the data object from the database.
     *
     * @param object data object to delete
     * @return usually 1 to indicate rows changed via JDBC.
     * @throws PersismException Perhaps when asteroid 1999 RQ36 hits us?
     */
    public <T> Result<T> delete(T object) throws PersismException {

        helper.checkIfOkForWriteOperation(object, "DELETE");

        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeys.message("DELETE", metaData.getTableName(object.getClass())));
        }

        PreparedStatement st = null;
        try {
            String deleteStatement = metaData.getDeleteStatement(object, connection);
            log.debug(deleteStatement);

            st = connection.prepareStatement(deleteStatement);

            // These keys should always be in sorted order.
            Map<String, PropertyInfo> columns = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);

            List<Object> params = new ArrayList<>(primaryKeys.size());
            List<ColumnInfo> columnInfos = new ArrayList<>(columns.size());
            for (String column : primaryKeys) {
                params.add(columns.get(column).getValue(object));
                columnInfos.add(metaData.getColumns(object.getClass(), connection).get(column));
            }

            for (int j = 0; j < params.size(); j++) {
                if (params.get(j) != null) {
                    params.set(j, converter.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }
            helper.setParameters(st, params.toArray());
            int rows = st.executeUpdate();
            return new Result<>(rows, object);

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);

        } finally {
            Util.cleanup(st, null);
        }
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


    MetaData getMetaData() {
        return metaData;
    }

    Converter getConverter() {
        return converter;
    }

    Connection getConnection() {
        return connection;
    }

    // this is a maybe....
//    public static synchronized void clearMetaData() {
//        log.warn("Clearing meta data");
//        MetaData.metaData.clear();
//        log.warn("meta data cleared");
//    }

}
