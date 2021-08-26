package net.sf.persism;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotTable;
import net.sf.persism.annotations.View;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.regex.MatchResult;

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

    private final Connection connection;

    private MetaData metaData;

    private Reader reader;
    private Converter converter;

    /**
     * @param connection db connection
     * @throws PersismException if something goes wrong
     */
    public Session(Connection connection) throws PersismException {
        this.connection = connection;
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
     * @return Result object containing rows changed (usually 1 to indicate rows changed via JDBC) and the data object itself which may have been changed.
     * @throws PersismException Indicating the upcoming robot uprising.
     */
    public <T> Result<T> update(T object) throws PersismException {
        checkIfView(object, "Update");

        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeys.message("UPDATE", metaData.getTableName(object.getClass())));
        }

        PreparedStatement st = null;
        try {

            String updateStatement = null;
            try {
                updateStatement = metaData.getUpdateStatement(object, connection);
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
                    params.set(j, converter.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }
            setParameters(st, params.toArray());
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
     * Performs an Insert or Update depending on whether the primary key is defined by the object parameter.
     *
     * @param object the data object to insert or update.
     * @param <T>    Type of the returning data object in Result.
     * @return Result object containing rows changed (usually 1 to indicate rows changed via JDBC) and the data object itself which may have been changed by auto-inc or column defaults.
     * @throws PersismException If this table has no primary keys or some SQL error.
     */
    public <T> Result<T> upsert(T object) throws PersismException {
        checkIfView(object, "Upsert");

        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeys.message("UPSERT", metaData.getTableName(object.getClass())));
        }

        try {
            Map<String, PropertyInfo> properties = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);
            Map<String, ColumnInfo> columns = metaData.getColumns(object.getClass(), connection);
            List<Object> params = new ArrayList<>(primaryKeys.size());
            boolean isInsert = false;

            if (primaryKeys.size() == 1 && columns.get(primaryKeys.get(0)).autoIncrement) {
                // If this is a single auto inc - we don't have to fetch back to the DB to decide
                PropertyInfo propertyInfo = properties.get(primaryKeys.get(0));
                Class<?> returnType = propertyInfo.getter.getReturnType();
                Object value = propertyInfo.getter.invoke(object);
                if (value == null || (returnType.isPrimitive() && value == Types.getDefaultValue(returnType))) {
                    isInsert = true;
                }
            } else {
                // Multiple primaries or the key(s) are set by the user - need to fetch to see if this is an insert or update
                for (String key : primaryKeys) {
                    PropertyInfo propertyInfo = properties.get(key);
                    Object value = propertyInfo.getter.invoke(object);
                    params.add(value);
                }

                Parameters parameters = params(params.toArray());
                parameters.areKeys = true;
                Object pojo = fetch(object.getClass(), parameters);
                isInsert = pojo == null;
            }

            if (isInsert) {
                return insert(object);
            } else {
                return update(object);
            }
        } catch (PersismException e) {
            throw e; // todo see how this works. do we lose information?
        } catch (Exception e) {
            throw new PersismException(e.getMessage(), e);
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
        checkIfView(object, "Insert");

        String insertStatement = metaData.getInsertStatement(object, connection);

        PreparedStatement st = null;
        ResultSet rs = null;

        Object returnObject = object;

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

                        if (propertyInfo.getter.invoke(object) == null) {

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
                    params.set(j, converter.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
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
                    SQL sql = new SQL(metaData.getDefaultSelectStatement(object.getClass(), connection));
                    returnObject = fetch(object.getClass(), sql, params(primaryKeyValues.toArray()));
                } else {
                    fetch(object);
                    returnObject = object;
                }
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

        checkIfView(object, "Delete");

        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeys.message("DELETE", metaData.getTableName(object.getClass())));
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
                    params.set(j, converter.convert(params.get(j), columnInfos.get(j).columnType.getJavaType(), columnInfos.get(j).columnName));
                }
            }
            setParameters(st, params.toArray());
            int rows = st.executeUpdate();
            return new Result<>(rows, object);

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
     * @param objectClass
     * @param sql
     * @param parameters
     * @param <T>
     * @return
     * @deprecated
     */
    public <T> List<T> query(Class<T> objectClass, String sql, Object... parameters) {
        return query(objectClass, new SQL(sql), new Parameters(parameters));
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
        return query(objectClass, none());
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
        if (objectClass.getAnnotation(NotTable.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForNotTableQuery.message(objectClass, "QUERY w/o specifying the SQL"));
        }

        //                 throw new PersismException(Messages.PrimaryKeysDontExist.message());

        if (objectClass.getAnnotation(View.class) != null) {

            // todo really we should just not support this at all. Fail like NotTable
            // TODO Also add log.warn if primary keys are defined on properties of NotTable or View
            Collection<PropertyInfo> properties = MetaData.getPropertyInfo(objectClass);
            long primaries = properties.stream().
                    filter(p -> p.annotations.get(Column.class) != null && ((Column) p.annotations.get(Column.class)).primary()).
                    count();

            if (primaries > 1) {
                throw new PersismException("Cant do this with a View and you specified multiple primaries!");
            } else if (primaries == 0) {
                throw new PersismException("Cant do this with a View and you specified NO primaries");
            }
            // if 0 or greater than 1
        }

        String sql;
        if (primaryKeyValues.size() == 0) {
            // Get the SELECT without any WHERE clause
            sql = metaData.getSelectStatement(objectClass, connection);
            return query(objectClass, sql(sql), none());
        } else {
            sql = metaData.getDefaultSelectStatement(objectClass, connection);
        }

        primaryKeyValues.areKeys = true;

        List<String> primaryKeys = metaData.getPrimaryKeys(objectClass, connection);

        if (primaryKeyValues.size() == primaryKeys.size()) {
            // single select
            return query(objectClass, sql(sql), primaryKeyValues);
        }

        String sd = metaData.getConnectionType().getKeywordStartDelimiter();
        String ed = metaData.getConnectionType().getKeywordEndDelimiter();

        String andSep = "";
        // View should not check for WHERE
        if (objectClass.getAnnotation(View.class) == null) {
            int n = sql.indexOf(" WHERE");
            sql = sql.substring(0, n + 7);
        } else {
            sql += " WHERE ";
        }

        StringBuilder sb = new StringBuilder(sql);
        int groups = primaryKeyValues.size() / primaryKeys.size(); // check for divide by zero?
        for (String column : primaryKeys) {
            String sep = "";
            sb.append(andSep).append(sd).append(column).append(ed).append(" IN (");
            for (int j = 0; j < groups; j++) {
                sb.append(sep).append("?");
                sep = ", ";
            }
            sb.append(")");
            andSep = " AND ";
        }
        sql = sb.toString();
        return query(objectClass, sql(sql), primaryKeyValues);
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

        if (isPOJO && objectClass.getAnnotation(NotTable.class) == null) {
            // Make sure columns are initialized if this is a table. TODO WHY DID WE NEED TO DO THIS?
            metaData.getTableColumnsPropertyInfo(objectClass, connection);
        }

        JDBCResult result = JDBCResult.DEFAULT;
        try {
            result = executeQuery(objectClass, sql, parameters, isPOJO);
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
            throw new PersismException(Messages.CannotReadThisType.message("primitive"));
        }

        if (isRecord(objectClass)) {
            throw new PersismException(Messages.CannotReadThisType.message("Record"));
        }

        // todo throw if View. WHAT IF PRIMARY IS DEFINED BY COLUMN? So no throw.

        List<String> primaryKeys = metaData.getPrimaryKeys(objectClass, connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeys.message("FETCH", metaData.getTableName(objectClass)));
        }

        Map<String, PropertyInfo> properties = metaData.getTableColumnsPropertyInfo(object.getClass(), connection);
        Parameters params = new Parameters();

        List<ColumnInfo> columnInfos = new ArrayList<>(properties.size());
        Map<String, ColumnInfo> cols = metaData.getColumns(objectClass, connection);
        JDBCResult JDBCResult = new JDBCResult();
        try {
            for (String column : primaryKeys) {
                PropertyInfo propertyInfo = properties.get(column);
                params.add(propertyInfo.getter.invoke(object));
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

            sql = parsePropertyNames(sql, objectClass, connection);

            exec(JDBCResult, sql, params.toArray());

            if (JDBCResult.rs.next()) {
                reader.readObject(object, JDBCResult.rs);
                return true;
            }
            return false;

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(JDBCResult.st, JDBCResult.rs);
        }
    }

    // todo Optional? totally breaks the API though...
    /**
     * @param objectClass
     * @param sql
     * @param parameters
     * @param <T>
     * @return
     * @deprecated
     */
    public <T> T fetch(Class<T> objectClass, String sql, Object... parameters) {
        return fetch(objectClass, new SQL(sql), new Parameters(parameters));
    }

    /**
     * Fetch object by primary key(s)
     *
     * @param objectClass Type to return
     * @param parameters  primary key values
     * @param <T>         Type
     * @return Instance of object type T or NULL if not found
     * @throws PersismException if something goes wrong.
     */
    public <T> T fetch(Class<T> objectClass, Parameters parameters) {
        return fetch(objectClass, new SQL(metaData.getDefaultSelectStatement(objectClass, connection)), parameters);
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
     * @throws PersismException Well, this is a runtime exception so actually it could be anything really.
     */
    public <T> T fetch(Class<T> objectClass, SQL sql, Parameters parameters) {
        // If we know this type it means it's a primitive type. Not a DAO so we use a different rule to read those
        boolean isPOJO = Types.getType(objectClass) == null;
        boolean isRecord = isPOJO && isRecord(objectClass);

        JDBCResult result = JDBCResult.DEFAULT;
        try {
            result = executeQuery(objectClass, sql, parameters, isPOJO);
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

    // TODO NEED TO REFACTOR THIS SIMILAR TO DEV version
    private JDBCResult executeQuery(Class<?> objectClass, SQL sql, Parameters parameters, boolean isPOJO) throws SQLException {
        JDBCResult result = new JDBCResult();

        String sqlQuery = sql.toString();

        // todo add unit test for named params with non-pojo like Select String from something. Why do I need isPOJO here?
        if (isPOJO && parameters.areNamed) {
            Map<String, List<Integer>> paramMap = new HashMap<>();
            sqlQuery = parseParameters(sql.toString(), paramMap);
            parameters.setParameterMap(paramMap);
        }

        // or use sql.knownSQL
        if (isPOJO && parameters.areKeys) {
            // todo this could be confusing to a user. NotTable has no way of knowing a primary key or does it? What if we just annotate it?
            // todo for View this is OK maybe but for NotTable we have no SQL so that wouldn't work
            if (objectClass.getAnnotation(NotTable.class) != null) { // || objectClass.getAnnotation(View.class) != null
                throw new PersismException(Messages.PrimaryKeysDontExist.message());
            }
            // convert parameters - usually it's the UUID type that may need a conversion to byte[16]
            // Probably we don't want to auto-convert here since it's inconsistent. DO WE? YES.
            List<String> keys = metaData.getPrimaryKeys(objectClass, connection);
            if (keys.size() == 1) {
                Map<String, ColumnInfo> columns = metaData.getColumns(objectClass, connection);
                String key = keys.get(0);
                ColumnInfo columnInfo = columns.get(key);
                for (int j = 0; j < parameters.size(); j++) {
                    if (parameters.get(j) != null) {
                        parameters.set(j, converter.convert(parameters.get(j), columnInfo.columnType.getJavaType(), columnInfo.columnName));
                    }
                }
            }
        }

        if (sql.whereOnly) {
            if (objectClass.getAnnotation(NotTable.class) != null) {
                throw new PersismException(Messages.WhereNotSupportedForNotTableQueries.message());
            }
            String select = metaData.getSelectStatement(objectClass, connection);
            String xsql = parsePropertyNames(select + " " + sqlQuery, objectClass, connection);

            if (log.isDebugEnabled()) {
                log.debug("executeQuery: %s params: %s", xsql, parameters);
            }

            exec(result, xsql, parameters.toArray());
        } else {

            checkStoredProcOrSQL(objectClass, sql);

            // forget translating properties on normal SQL - stupid idea. It's OK for a WHERE clause though
            //String xsql = translatePropertyNames(sql.toString(), objectClass, connection);

            if (log.isDebugEnabled()) {
                log.debug("executeQuery: %s params: %s", sqlQuery, parameters);
            }

            // we don't check parameter types here? Nope - we don't know anything at this point.
            exec(result, sqlQuery, parameters.toArray());
        }
        return result;
    }

    private String parsePropertyNames(String sql, Class<?> objectClass, Connection connection) {
        // look for ":"
        Scanner scanner = new Scanner(sql);
        List<MatchResult> matchResults = scanner.findAll(":").toList();
        Set<String> propertyNames = new LinkedHashSet<>();
        Set<String> propertiesNotFound = new LinkedHashSet<>();

        if (matchResults.size() > 0) {
            log.debug("Parse properties -> SQL before: %s", sql);
            for (MatchResult result : matchResults) {
                String sub = sql.substring(result.start());
                int n = 0;
                for (int j = result.start() + 1; j < sql.length(); j++) {
                    char c = sql.charAt(j);
                    if (Character.isJavaIdentifierPart(c)) {
                        n++;
                    } else {
                        propertyNames.add(sub.substring(0, n + 1));
                        break;
                    }
                }
            }

            Map<String, PropertyInfo> properties;
            properties = metaData.getTableColumnsPropertyInfo(objectClass, connection);
            String sd = metaData.getConnectionType().getKeywordStartDelimiter();
            String ed = metaData.getConnectionType().getKeywordEndDelimiter();

            String repl = sql;
            for (String propertyName : propertyNames) {
                String pname = propertyName.substring(1); // remove :
                boolean found = false;
                for (String column : properties.keySet()) {
                    PropertyInfo info = properties.get(column);
                    if (info.propertyName.equalsIgnoreCase(pname)) {
                        found = true;
                        String col = sd + column + ed;
                        repl = repl.replace(propertyName, col);
                        break;
                    }
                }
                if (!found) {
                    propertiesNotFound.add(pname);
                }
            }
            log.debug("Parse properties -> SQL after : %s", repl);
            sql = repl;
        }
        if (propertiesNotFound.size() > 0) {
            throw new PersismException(Messages.QueryPropertyNamesMissingOrNotFound.message(propertiesNotFound, sql));
        }
        return sql;
    }

    /**
     * Adam Crume, JavaWorld.com, 04/03/07
     * http://www.javaworld.com/javaworld/jw-04-2007/jw-04-jdbc.html?page=2
     * https://www.infoworld.com/article/2077706/named-parameters-for-preparedstatement.html?page=2
     * Parses a query with named parameters.  The parameter-index mappings are
     * put into the map, and the
     * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This
     * method is non-private so JUnit code can
     * test it.
     *
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    static String parseParameters(String query, Map<String, List<Integer>> paramMap) {
        // I was originally using regular expressions, but they didn't work well
        // for ignoring parameter-like strings inside quotes.

        // originally used : - which conflicts with property names so I'll use @
        // todo verify about inquote vars - we have different delimiters based on the different dbs
        // todo add unit tests
        // todo maybe use the similar method we did for parseProperties

        int length = query.length();
        StringBuilder parsedQuery = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for (int i = 0; i < length; i++) {
            char c = query.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == '@' && i + 1 < length && // was :
                        Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name = query.substring(i + 1, j);
                    c = '?'; // replace the parameter with a question mark
                    i += name.length(); // skip past the end if the parameter

                    List<Integer> indexList = paramMap.get(name);
                    if (indexList == null) {
                        indexList = new LinkedList<>();
                        paramMap.put(name, indexList);
                    }
                    indexList.add(index);

                    index++;
                }
            }
            parsedQuery.append(c);
        }

        // why bother?

//        // replace the lists of Integer objects with arrays of ints
//        for (Iterator itr = paramMap.entrySet().iterator(); itr.hasNext(); ) {
//            Map.Entry entry = (Map.Entry) itr.next();
//            List list = (List) entry.getValue();
//            int[] indexes = new int[list.size()];
//            int i = 0;
//            for (Iterator itr2 = list.iterator(); itr2.hasNext(); ) {
//                Integer x = (Integer) itr2.next();
//                indexes[i++] = x.intValue();
//            }
//            entry.setValue(indexes);
//        }

        return parsedQuery.toString();
    }


    private <T> void checkStoredProcOrSQL(Class<T> objectClass, SQL sql) {
        boolean startsWithSelect = sql.toString().trim().toLowerCase().startsWith("select ");
        if (sql.storedProc) {
            if (startsWithSelect) {
                log.warnNoDuplicates(Messages.InappropriateMethodUsedForSQLTypeInstance.message(objectClass, "sql()", "a stored proc", "proc()"));
            }
        } else {
            if (!startsWithSelect) {
                log.warnNoDuplicates(Messages.InappropriateMethodUsedForSQLTypeInstance.message(objectClass, "proc()", "an SQL query", "sql()"));
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

    /*
    Private methods
     */

    private void exec(JDBCResult result, String sql, Object... parameters) throws SQLException {
        // bug fix for Java 16 TRIM in case
        if (sql.trim().toLowerCase().startsWith("select ")) {
            if (metaData.getConnectionType() == ConnectionTypes.Firebird) {
                // https://stackoverflow.com/questions/935511/how-can-i-avoid-resultset-is-closed-exception-in-java
                result.st = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
            } else {
                result.st = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            }

            PreparedStatement pst = (PreparedStatement) result.st;
            setParameters(pst, parameters);
            result.rs = pst.executeQuery();
        } else {
            if (!sql.trim().toLowerCase().startsWith("{call")) {
                sql = "{call " + sql + "} ";
            }
            // Don't need If Firebird here. Firebird would call a selectable stored proc with SELECT anyway
            result.st = connection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

            CallableStatement cst = (CallableStatement) result.st;
            setParameters(cst, parameters);
            result.rs = cst.executeQuery();
        }
    }

    private void checkIfView(Object object, String operation) {
        if (object.getClass().getAnnotation(View.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForView.message(object.getClass(), operation));
        }
    }

    private <T> T getTypedValueReturnedFromGeneratedKeys(Class<T> objectClass, ResultSet rs) throws SQLException {

        Object value = null;
        Types type = Types.getType(objectClass);

        if (type == null) {
            log.warn(Messages.UnknownTypeForPrimaryGeneratedKey.message(objectClass));
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
            log.debug("setParameters PARAMS: %s", Arrays.asList(parameters));
        }

        Object value; // used for conversions
        int n = 1;
        for (Object param : parameters) {

            if (param != null) {

                Types type = Types.getType(param.getClass());
                if (type == null) {
                    log.warn(Messages.UnknownTypeInSetParameters.message(param.getClass()));
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

                    case LocalTimeType:
                        value = converter.convert(param, Time.class, "Parameter " + n);
                        st.setObject(n, value);
                        break;

                    case UtilDateType:
                    case LocalDateType:
                    case LocalDateTimeType:
                        value = converter.convert(param, Timestamp.class, "Parameter " + n);
                        st.setObject(n, value);
                        break;

                    case OffsetDateTimeType:
                    case ZonedDateTimeType:
                    case InstantType:
                        log.warn(Messages.UnSupportedTypeInSetParameters.message(type));
                        st.setObject(n, param);
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
                        // so this should not occur unless they were passed in by the user.
                        // We are most probably about to fail here.
                        log.warn(Messages.ParametersDoNotUseClobOrBlob.message(), new Throwable());
                        st.setObject(n, param);
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
                            // todo mysql seems to set the byte array this way? But it won't match!
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
