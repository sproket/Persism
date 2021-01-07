package net.sf.persism;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotMapped;
import net.sf.persism.annotations.TableName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Meta data collected in a map singleton based on connection url
 *
 * @author Dan Howard
 * @since 3/31/12 4:19 PM
 */
final class MetaData {

    private static final Log log = Log.getLogger(MetaData.class);

    // properties for each class
    private static final Map<Class, Collection<PropertyInfo>> propertyMap = new ConcurrentHashMap<Class, Collection<PropertyInfo>>(32);

    // column to property map for each class
    private Map<Class, Map<String, PropertyInfo>> propertyInfoMap = new ConcurrentHashMap<Class, Map<String, PropertyInfo>>(32);
    private Map<Class, Map<String, ColumnInfo>> columnInfoMap = new ConcurrentHashMap<Class, Map<String, ColumnInfo>>(32);

    // table name for each class
    private Map<Class, String> tableMap = new ConcurrentHashMap<Class, String>(32);

    // Table meta information for updates/inserts for each class
    private Map<Class, String> updateStatementsMap = new ConcurrentHashMap<Class, String>(32);
    private Map<Class, String> insertStatementsMap = new ConcurrentHashMap<Class, String>(32);
    private Map<Class, String> deleteStatementsMap = new ConcurrentHashMap<Class, String>(32);
    private Map<Class, String> selectStatementsMap = new ConcurrentHashMap<Class, String>(32);

    // Key is SQL with named params, Value is SQL with ?
    private Map<String, String> sqlWitNamedParams = new ConcurrentHashMap<String, String>(32);

    // Key is SQL with named params, Value list of named params
    private Map<String, List<String>> namedParams = new ConcurrentHashMap<String, List<String>>(32);

    //    private Map<Class, List<String>> primaryKeysMap = new ConcurrentHashMap<Class, List<String>>(32); // remove later maybe?

    // list of tables in the DB mapped to the connection URL string
    private List<String> tableNames = new ArrayList<String>(32);

    // Map of table names + meta data
    private Map<String, TableInfo> tableInfoMap = new ConcurrentHashMap<String, TableInfo>(32);

    private static final Map<String, MetaData> metaData = new ConcurrentHashMap<String, MetaData>(4);

    ConnectionTypes connectionType;

    private MetaData(Connection con) throws SQLException {

        connectionType = ConnectionTypes.get(con.getMetaData().getURL());
        if (connectionType == ConnectionTypes.Other) {
            log.warn("Unknown connection type. Please contact Persism to add support for " + con.getMetaData().getDatabaseProductName());
        }

        populateTableList(con);
    }

    static synchronized MetaData getInstance(Connection con) throws SQLException {

        String url = con.getMetaData().getURL();
        if (metaData.get(url) == null) {
            metaData.put(url, new MetaData(con));
        }
        log.info("MetaData getting instance " + url);
        return metaData.get(url);
    }

    // Unit tests
    static synchronized void removeInstance(Connection con) throws SQLException {
        String url = con.getMetaData().getURL();
        log.info("removing " + url);
        metaData.remove(url);
    }


    // Should only be called IF the map does not contain the column meta information yet.
    // Version for Tables
    private synchronized <T> Map<String, PropertyInfo> determineColumns(Class<T> objectClass, String tableName, Connection connection) {
        // double check map
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        java.sql.ResultSet rs = null;
        Statement st = null;
        try {
            st = connection.createStatement();
            // gives us real column names with case.
            String sql = new StringBuilder().append("SELECT * FROM ").append(sd).append(tableName).append(ed).append(" WHERE 1=0").toString();
            if (log.isDebugEnabled()) {
                log.debug("determineColumns: " + sql);
            }
            rs = st.executeQuery(sql);
            return determineColumns(objectClass, rs);
        } catch (SQLException e) {
            throw new PersismException(e);
        } finally {
            Util.cleanup(st, rs);
        }
    }

    // Should only be called IF the map does not contain the column meta information yet.
    // Version for Queries
    private synchronized <T> Map<String, PropertyInfo> determineColumns(Class<T> objectClass, java.sql.ResultSet rs) throws SQLException {

        // double check map - note this could be called with a Query were we never have that in here
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }

        ResultSetMetaData rsmd = rs.getMetaData();
        Collection<PropertyInfo> properties = getPropertyInfo(objectClass);

        int columnCount = rsmd.getColumnCount();
        Map<String, PropertyInfo> columns = new TreeMap<String, PropertyInfo>(String.CASE_INSENSITIVE_ORDER);
        // todo see DatabaseMetaData.getExtraNameCharacters() to find other chars that should be removed.
        for (int j = 1; j <= columnCount; j++) {
            String realColumnName = rsmd.getColumnLabel(j); //rsmd.getColumnName(j);
            int length = rsmd.getColumnDisplaySize(j);
            String columnName = realColumnName.toLowerCase().replace("_", "").replace(" ", "");
            PropertyInfo foundProperty = null;
            for (PropertyInfo propertyInfo : properties) {
                String propertyName = propertyInfo.propertyName.toLowerCase().replace("_", "");
                if (propertyName.equalsIgnoreCase(columnName)) {
                    foundProperty = propertyInfo;
                    break;
                } else {
                    // check annotation against column name
                    Annotation annotation = propertyInfo.getAnnotation(Column.class);
                    if (annotation != null) {
                        if (((Column) annotation).value().equalsIgnoreCase(realColumnName)) {
                            foundProperty = propertyInfo;
                            break;
                        }
                    }
                }
            }
            if (foundProperty != null) {
                foundProperty.length = length;
                columns.put(realColumnName, foundProperty);
            } else {
                log.warn("Property not found for column: " + realColumnName + " class: " + objectClass);
            }
        }

        propertyInfoMap.put(objectClass, columns);
        return columns;
    }

    private synchronized <T> Map<String, ColumnInfo> determineColumnInfo(Class<T> objectClass, String tableName, Connection connection) {
        if (columnInfoMap.containsKey(objectClass)) {
            return columnInfoMap.get(objectClass);
        }

        Statement st = null;
        ResultSet rs = null;
        Map<String, PropertyInfo> properties = getTableColumns(objectClass, connection);

        try {

            st = connection.createStatement();
            rs = st.executeQuery("SELECT * FROM " + tableName + " WHERE 1=0");

            // Make sure primary keys sorted by column order in case we have more than 1
            // then we'll know the order to apply the parameters.
            Map<String, ColumnInfo> map = new LinkedHashMap<String, ColumnInfo>(32);

            boolean primaryKeyFound = false;

            // Grab all columns and make first pass to detect primary auto-inc
            ResultSetMetaData rsMetaData = rs.getMetaData();
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                // only include columns where we have a property
                if (properties.containsKey(rsMetaData.getColumnLabel(i))) {
                    ColumnInfo columnInfo = new ColumnInfo();

                    columnInfo.columnName = rsMetaData.getColumnLabel(i);
                    columnInfo.generated = rsMetaData.isAutoIncrement(i);
                    columnInfo.primary = columnInfo.generated;

                    if (!primaryKeyFound) {
                        primaryKeyFound = columnInfo.primary;
                    }

                    PropertyInfo propertyInfo = properties.get(rsMetaData.getColumnLabel(i));
                    Annotation annotation = propertyInfo.getAnnotation(Column.class);
                    if (annotation != null) {
                        if (((Column) annotation).hasDefault()) {
                            columnInfo.hasDefault = true;
                        }

                        if (((Column) annotation).primary()) {
                            columnInfo.primary = true;
                        }

                        if (((Column) annotation).generated()) {
                            columnInfo.generated = true;
                        }

                        if (!primaryKeyFound) {
                            primaryKeyFound = columnInfo.primary;
                        }
                    }

                    map.put(columnInfo.columnName, columnInfo);
                }
            }
            rs.close();

            DatabaseMetaData dmd = connection.getMetaData();

            // does not work with SQLite - See testTypes unit test
            // columnInfo.columnType = Types.convert(rsMetaData.getColumnType(i));

            /*
             Get columns from database metadata since we don't get Type from resultSetMetaData
             with SQLite. + We also need to know if there's a default on a column.
             */
            rs = dmd.getColumns(null, connectionType.getSchemaPattern(), tableName, null);
            while (rs.next()) {
                ColumnInfo columnInfo = map.get(rs.getString("COLUMN_NAME"));
                if (columnInfo != null) {
                    if (!columnInfo.hasDefault) {
                        columnInfo.hasDefault = Util.containsColumn(rs, "COLUMN_DEF") && rs.getString("COLUMN_DEF") != null;
                    }
                    columnInfo.columnType = Types.convert(rs.getInt("DATA_TYPE"));
                }
            }
            rs.close();

            // Iterate primary keys and update column infos
            rs = dmd.getPrimaryKeys(null, connectionType.getSchemaPattern(), tableName);
            while (rs.next()) {
                ColumnInfo columnInfo = map.get(rs.getString("COLUMN_NAME"));
                if (columnInfo != null) {
                    columnInfo.primary = true;

                    if (!primaryKeyFound) {
                        primaryKeyFound = columnInfo.primary;
                    }
                }
            }

            if (!primaryKeyFound) {
                // check annotations. todo do we need this loop still?
                for (String column : properties.keySet()) {
                    PropertyInfo propertyInfo = properties.get(column);

                    Annotation annotation = propertyInfo.getAnnotation(Column.class);
                    if (annotation != null) {
                        if (((Column) annotation).primary()) {
                            primaryKeyFound = true;
                            map.get(column).primary = true;
                        }
                    }

                }
            }

            if (!primaryKeyFound) {
                // Should we fail-fast? Actually no, we should not fail here.
                // It's very possible the user has a table that they will never
                // update, delete or select (by primary).
                // They may only want to do read operations with specified queries and in that
                // context we don't need any primary keys. (same with insert)
            }

            columnInfoMap.put(objectClass, map);

            return map;

        } catch (SQLException e) {
            throw new PersismException(e);
        } finally {
            Util.cleanup(st, rs);
        }
    }

    List<String> getNamedParameters(String sql) {
        if (namedParams.containsKey(sql)) {
            return namedParams.get(sql);
        }

        if (!sql.contains(":")) {
            return Collections.emptyList();
        }

        return null;
    }

    static synchronized <T> Collection<PropertyInfo> getPropertyInfo(Class<T> objectClass) {

        if (propertyMap.containsKey(objectClass)) {
            return propertyMap.get(objectClass);
        }

        Map<String, PropertyInfo> propertyNames = new HashMap<String, PropertyInfo>(32);

        Method[] methods = objectClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                String propertyName = methodName.substring(3).toLowerCase();

                PropertyInfo propertyInfo = propertyNames.get(propertyName);
                if (propertyInfo == null) {
                    propertyInfo = new PropertyInfo();
                    propertyNames.put(propertyName, propertyInfo);
                }
                propertyInfo.setter = method;
                propertyInfo.propertyName = propertyName;

                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    propertyInfo.annotations.put(annotation.annotationType(), annotation);
                }
            }

            if (methodName.startsWith("is") || methodName.startsWith("get") && !"getClass".equalsIgnoreCase(methodName)) {
                int index = 3;
                if (methodName.startsWith("is")) {
                    index = 2;
                }
                String propertyName = methodName.substring(index).toLowerCase();

                PropertyInfo propertyInfo = propertyNames.get(propertyName);
                if (propertyInfo == null) {
                    propertyInfo = new PropertyInfo();
                    propertyNames.put(propertyName, propertyInfo);
                }
                propertyInfo.getter = method;
                propertyInfo.propertyName = propertyName;
                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    propertyInfo.annotations.put(annotation.annotationType(), annotation);
                }
            }
        }

        Field[] fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            PropertyInfo propertyInfo = propertyNames.get(field.getName().toLowerCase());
            if (propertyInfo != null) {
                // Add the field annotations
                Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    propertyInfo.annotations.put(annotation.annotationType(), annotation);
                }
            }
        }

        // Remove any properties found with the NoColumn annotation OR ones missing a setter (meaning they are calculated properties)
        // http://stackoverflow.com/questions/2026104/hashmap-keyset-foreach-and-remove
        Iterator<Map.Entry<String, PropertyInfo>> it = propertyNames.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PropertyInfo> entry = it.next();
            PropertyInfo info = entry.getValue();
            if (info.getAnnotation(NotMapped.class) != null || info.setter == null) {
                it.remove();
            }
        }
        Collection<PropertyInfo> properties = propertyNames.values();
        propertyMap.put(objectClass, properties);
        return Collections.unmodifiableCollection(properties);
    }


    private static final String[] tableTypes = {"TABLE"};

    // Populates the tables list with table names from the DB.
    // This list is used for discovery of the table name from a class.
    // ONLY to be called from Init in a synchronized way.
    private void populateTableList(Connection con) throws PersismException {

        java.sql.ResultSet rs = null;

        try {
            // NULL POINTER WITH
            // http://social.msdn.microsoft.com/Forums/en-US/sqldataaccess/thread/5c74094a-8506-4278-ac1c-f07d1bfdb266
            // solution:
            // http://stackoverflow.com/questions/8988945/java7-sqljdbc4-sql-error-08s01-on-getconnection

            rs = con.getMetaData().getTables(null, connectionType.getSchemaPattern(), null, tableTypes);
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }

        } catch (SQLException e) {
            throw new PersismException(e);

        } finally {
            Util.cleanup(null, rs);
        }
    }

    String getUpdateStatement(Object object, Connection connection) throws PersismException {

        if (object instanceof Persistable) {

            Map<String, PropertyInfo> changedColumns = getChangedColumns((Persistable) object, connection);
            if (changedColumns.size() == 0) {
                throw new PersismException("No Changes detected in " + object);
            }
            // Note we don't not add Persistable updates to updateStatementsMap since they will be different all the time.
            String sql = buildUpdateString(object, changedColumns.keySet().iterator(), connection);
            if (log.isDebugEnabled()) {
                log.debug("getUpdateStatement for " + object.getClass() + " for changed fields is " + sql);
            }
            return sql;
        }

        if (updateStatementsMap.containsKey(object.getClass())) {
            return updateStatementsMap.get(object.getClass());
        }

        return determineUpdateStatement(object, connection);
    }

    // Used by Objects not implementing Persistable since they will always use the same update statement
    private synchronized String determineUpdateStatement(Object object, Connection connection) {

        if (updateStatementsMap.containsKey(object.getClass())) {
            return updateStatementsMap.get(object.getClass());
        }

        Map<String, PropertyInfo> columns = getTableColumns(object.getClass(), connection);

        String updateStatement = buildUpdateString(object, columns.keySet().iterator(), connection);

        // Store static update statement for future use.
        updateStatementsMap.put(object.getClass(), updateStatement);

        if (log.isDebugEnabled()) {
            log.debug("determineUpdateStatement for " + object.getClass() + " is " + updateStatement);
        }

        return updateStatement;
    }


    String getInsertStatement(Object object, Connection connection) throws PersismException {
//        insertStatementsMap.computeIfAbsent(object.getClass(), key -> {
//            String value = null;
//            try {
//                value = determineInsertStatement(object, connection);
//            } catch (Exception e) {
//                throw new PersismException(e);
//            }
//            return value;
//        });
//        return insertStatementsMap.get(object.getClass());

//         Note this will not include columns unless they have the associated property.
        if (insertStatementsMap.containsKey(object.getClass())) {
            return insertStatementsMap.get(object.getClass());
        }
        try {
            return determineInsertStatement(object, connection);
        } catch (Exception e) {
            throw new PersismException(e);
        }
    }

    private synchronized String determineInsertStatement(Object object, Connection connection) throws InvocationTargetException, IllegalAccessException {


        if (insertStatementsMap.containsKey(object.getClass())) {
            return insertStatementsMap.get(object.getClass());
        }


        String tableName = getTableName(object.getClass(), connection);
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        Map<String, ColumnInfo> columns = columnInfoMap.get(object.getClass());
        Map<String, PropertyInfo> properties = getTableColumns(object.getClass(), connection);
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(sd).append(tableName).append(ed).append(" (");
        String sep = "";
        boolean columnsHaveDefaults = false;

        for (ColumnInfo column : columns.values()) {
            if (!column.generated) {

                if (column.hasDefault) {

                    columnsHaveDefaults = true;

                    // Do not include if this column has a default and no value has been
                    // set on it's associated property.
                    if (properties.get(column.columnName).getter.invoke(object) == null) {
                        continue;
                    }

                }
                sb.append(sep).append(sd).append(column.columnName).append(ed);
                sep = ", ";
            }
        }
        sb.append(") VALUES (");
        sep = "";
        for (ColumnInfo column : columns.values()) {
            if (!column.generated) {

                if (column.hasDefault) {
                    // Do not include if this column has a default and no value has been
                    // set on it's associated property.
                    if (properties.get(column.columnName).getter.invoke(object) == null) {
                        continue;
                    }
                }

                sb.append(sep).append(" ? ");
                sep = ", ";
            }
        }
        sb.append(") ");

        String insertStatement;
        insertStatement = sb.toString();

        if (log.isDebugEnabled()) {
            log.debug("determineInsertStatement for " + object.getClass() + " is " + insertStatement);
        }

        // Do not put this insert statement into the map if any columns have defaults.
        // Because the insert statement is variable.
        if (!columnsHaveDefaults) {
            insertStatementsMap.put(object.getClass(), insertStatement);
        }

        return insertStatement;
    }

    public String getDeleteStatement(Object object, Connection connection) {
        if (deleteStatementsMap.containsKey(object.getClass())) {
            return deleteStatementsMap.get(object.getClass());
        }
        return determineDeleteStatement(object, connection);
    }

    private synchronized String determineDeleteStatement(Object object, Connection connection) {

        if (deleteStatementsMap.containsKey(object.getClass())) {
            return deleteStatementsMap.get(object.getClass());
        }

        String tableName = getTableName(object.getClass(), connection);
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        List<String> primaryKeys = getPrimaryKeys(object.getClass(), connection);

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(sd).append(tableName).append(ed).append(" WHERE ");
        String sep = "";
        for (String column : primaryKeys) {
            sb.append(sep).append(sd).append(column).append(ed).append(" = ?");
            sep = " AND ";
        }

        String deleteStatement = sb.toString();

        if (log.isDebugEnabled()) {
            log.debug("getDeleteStatement for " + object.getClass() + " is " + deleteStatement);
        }

        deleteStatementsMap.put(object.getClass(), deleteStatement);

        return deleteStatement;
    }

    public String getSelectStatement(Object object, Connection connection) {
        if (selectStatementsMap.containsKey(object.getClass())) {
            return selectStatementsMap.get(object.getClass());
        }
        return determineSelectStatement(object, connection);
    }

    private synchronized String determineSelectStatement(Object object, Connection connection) {

        if (selectStatementsMap.containsKey(object.getClass())) {
            return selectStatementsMap.get(object.getClass());
        }

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        String tableName = getTableName(object.getClass(), connection);

        List<String> primaryKeys = getPrimaryKeys(object.getClass(), connection);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(sd).append(tableName).append(ed).append(" WHERE ");
        String sep = "";
        for (String column : primaryKeys) {
            sb.append(sep).append(sd).append(column).append(ed).append(" = ?");
            sep = " AND ";
        }

        String selectStatement = sb.toString();

        if (log.isDebugEnabled()) {
            log.debug("determineSelectStatement for " + object.getClass() + " is " + selectStatement);
        }

        selectStatementsMap.put(object.getClass(), selectStatement);

        return selectStatement;
    }

    /**
     * @param object     data object
     * @param it         Iterator of column names
     * @param connection
     * @return parameterized SQL Update statement
     */
    private String buildUpdateString(Object object, Iterator<String> it, Connection connection) throws PersismException {

        String tableName = getTableName(object.getClass(), connection);
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        List<String> primaryKeys = getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException("Could not find any primary key(s) for table " + tableName);
        }

        StringBuilder sb = new StringBuilder();

        sb.setLength(0);
        sb.append("UPDATE ").append(sd).append(tableName).append(ed).append(" SET ");
        String sep = "";

        Map<String, ColumnInfo> columns = columnInfoMap.get(object.getClass());
        while (it.hasNext()) {
            String column = it.next();
            ColumnInfo columnInfo = columns.get(column);
            // todo could it ever be null?
            if (!columnInfo.generated && !columnInfo.primary) {
                sb.append(sep).append(sd).append(column).append(ed).append(" = ?");
                sep = ", ";
            }
        }
        sb.append(" WHERE ");
        sep = "";
        for (String column : primaryKeys) {
            sb.append(sep).append(sd).append(column).append(ed).append(" = ?");
            sep = " AND ";
        }
        return sb.toString();
    }

    Map<String, PropertyInfo> getChangedColumns(Persistable persistable, Connection connection) throws PersismException {

        try {
            Persistable original = (Persistable) persistable.getOriginalValue();

            Map<String, PropertyInfo> columns = getTableColumns(persistable.getClass(), connection);
            Map<String, PropertyInfo> changedColumns = new HashMap<String, PropertyInfo>(columns.keySet().size());

            for (String column : columns.keySet()) {

                PropertyInfo propertyInfo = columns.get(column);

                Object newValue = null;
                Object orgValue = null;
                newValue = propertyInfo.getter.invoke(persistable);
                orgValue = propertyInfo.getter.invoke(original);

                if (newValue != null && !newValue.equals(orgValue) || orgValue != null && !orgValue.equals(newValue)) {
                    changedColumns.put(column, propertyInfo);
                }
            }

            return changedColumns;
        } catch (IllegalAccessException e) {
            throw new PersismException(e);
        } catch (InvocationTargetException e) {
            throw new PersismException(e);
        }
    }


    <T> Map<String, PropertyInfo> getQueryColumns(Class<T> objectClass, java.sql.ResultSet rs) throws PersismException {
        // Queries are not mapped since it's possible multiple queries could be used against the same class
        try {
            return determineColumns(objectClass, rs);
        } catch (SQLException e) {
            throw new PersismException(e);
        }
    }


    <T> Map<String, ColumnInfo> getColumns(Class<T> objectClass, Connection connection) throws PersismException {
        if (columnInfoMap.containsKey(objectClass)) {
            return columnInfoMap.get(objectClass);
        }
        return determineColumnInfo(objectClass, getTableName(objectClass), connection);
    }

    <T> Map<String, PropertyInfo> getTableColumns(Class<T> objectClass, Connection connection) throws PersismException {

        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }
        return determineColumns(objectClass, getTableName(objectClass), connection);
    }

    <T> String getTableName(Class<T> objectClass) {

        if (tableMap.containsKey(objectClass)) {
            return tableMap.get(objectClass);
        }

        return determineTable(objectClass);
    }

    // internal version to retrieve meta information about this table's columns
    // at the same time we find the table name itself.
    private <T> String getTableName(Class<T> objectClass, Connection connection) {

        String tableName = getTableName(objectClass);

        if (!columnInfoMap.containsKey(objectClass)) {
            determineColumnInfo(objectClass, tableName, connection);
        }

        if (!propertyInfoMap.containsKey(objectClass)) {
            determineColumns(objectClass, tableName, connection);
        }
        return tableName;
    }

    private synchronized <T> String determineTable(Class<T> objectClass) {

        if (tableMap.containsKey(objectClass)) {
            return tableMap.get(objectClass);
        }

        // todo NOTE that the annotation name may not match the case of the tablename in the DB.
        String tableName;
        Annotation annotation = objectClass.getAnnotation(TableName.class);
        if (annotation != null) {
            tableName = ((TableName) annotation).value();
        } else {
            tableName = guessTableName(objectClass);
        }
        tableMap.put(objectClass, tableName);

        // determine table meta data at this point
        return tableName;
    }

    // Returns the table name found in the DB in the same case as in the DB.
    // throws PersismException if we cannot guess any table name for this class.
    private <T> String guessTableName(Class<T> objectClass) throws PersismException {
        List<String> guesses = new ArrayList<String>(6);
        List<String> guessedTables = new ArrayList<String>(6);

        String className = objectClass.getSimpleName();

        { // this block is because 'guess' string is also used in the for loop below
            String guess;
            if (className.endsWith("y")) {
                guess = className.substring(0, className.length() - 1) + "ies";
                guesses.add(guess);

                guess = Util.camelToTitleCase(guess);
                if (!guesses.contains(guess)) {
                    guesses.add(guess);
                }

                guess = Util.replaceAll(guess, ' ', '_');
                if (!guesses.contains(guess)) {
                    guesses.add(guess);
                }
            } else {
                guess = className + "s";
                guesses.add(guess);
                guess = Util.camelToTitleCase(guess);
                if (!guesses.contains(guess)) {
                    guesses.add(guess);
                }
                guess = Util.replaceAll(guess, ' ', '_');
                if (!guesses.contains(guess)) {
                    guesses.add(guess);
                }
            }
            guesses.add(className);
            guess = Util.camelToTitleCase(className);
            if (!guesses.contains(guess)) {
                guesses.add(guess);
            }
            guess = Util.replaceAll(guess, ' ', '_');
            if (!guesses.contains(guess)) {
                guesses.add(guess);
            }
        }

        boolean exactMatchFound = false;

        for (String tableName : tableNames) {
            if (exactMatchFound) {
                break;
            }
            for (String guess : guesses) {
                if (guess.equalsIgnoreCase(tableName)) {
                    guessedTables.clear();
                    guessedTables.add(tableName);
                    exactMatchFound = true;
                    break; // exact match
                }

                if (tableName.toLowerCase().contains(guess)) {
                    guessedTables.add(tableName);
                }
            }
        }
        if (guessedTables.size() == 0) {
            throw new PersismException("Could not determine a table for type: " + objectClass.getName() + " Guesses were: " + guesses);
        }

        if (guessedTables.size() > 1) {
            throw new PersismException("Could not determine a table for type: " + objectClass.getName() + " Guesses were: " + guesses + " and we found multiple matching tables: " + guessedTables);
        }

        return guessedTables.get(0);
    }

    List<String> getPrimaryKeys(Class objectClass, Connection connection) throws PersismException {

        // ensures meta data will be available
        String tableName = getTableName(objectClass, connection);

        List<String> primaryKeys = new ArrayList<String>(4);
        Map<String, ColumnInfo> map = columnInfoMap.get(objectClass);
        for (ColumnInfo col : map.values()) {
            if (col.primary) {
                primaryKeys.add(col.columnName);
            }
        }
        return primaryKeys;
    }

    // Currently only used by Insert so the only case tested here is getInt - Used to get the primary key value(s) after an insert.
    <T> T getTypedValue(Class<T> type, java.sql.ResultSet rs, int column) throws SQLException {
        Object value = null;
        String tmp;

        Types types = Types.getType(type);

        if (types == null) {
            log.warn("Unhandled type " + type);
            return (T) rs.getObject(column);
        }

        switch (types) {

            case booleanType:
                value = rs.getBoolean(column);
                break;
            case BooleanType:
                value = rs.getBoolean(column);
                break;
            case byteType:
                value = rs.getByte(column);
                break;
            case ByteType:
                value = rs.getObject(column) == null ? null : rs.getByte(column);
                break;
            case shortType:
                value = rs.getShort(column);
                break;
            case ShortType:
                value = rs.getObject(column) == null ? null : rs.getShort(column);
                break;
            case integerType:
                value = rs.getInt(column);
                break;
            case IntegerType:
                value = rs.getObject(column) == null ? null : rs.getInt(column);
                break;
            case longType:
                value = rs.getLong(column);
                break;
            case LongType:
                value = rs.getObject(column) == null ? null : rs.getLong(column);
                break;
            case floatType:
                value = rs.getFloat(column);
                break;
            case FloatType:
                value = rs.getObject(column) == null ? null : rs.getFloat(column);
                break;
            case doubleType:
                value = rs.getDouble(column);
                break;
            case DoubleType:
                value = rs.getObject(column) == null ? null : rs.getDouble(column);
                break;
            case BigDecimalType:
                value = rs.getObject(column) == null ? null : rs.getBigDecimal(column);
                break;
            case StringType:
                value = rs.getString(column);
                break;
            case characterType:
                tmp = rs.getString(column);
                value = tmp == null || tmp.length() == 0 ? null : tmp.charAt(0);
                break;
            case CharacterType:
                tmp = rs.getString(column);
                value = tmp == null || tmp.length() == 0 ? null : tmp.charAt(0);
                break;
            case UtilDateType:
                // todo can't this just use getDate as well?
                //timestamp = rs.getTimestamp(column);
                //value = (timestamp == null) ? null : new Date(timestamp.getTime());
                value = rs.getDate(column);
                break;
            case SQLDateType:
                value = rs.getDate(column);
                break;
            case TimeType:
                value = rs.getTime(column);
                break;
            case TimestampType:
                value = rs.getTimestamp(column);
                break;
            case byteArrayType:
                value = rs.getBytes(column);
                break;
            case ByteArrayType:
                value = rs.getBytes(column);
                break;
            case charArrayType:
                tmp = rs.getString(column);
                value = tmp == null ? null : tmp.toCharArray();
                break;
            case CharArrayType:
                tmp = rs.getString(column);
                value = tmp == null ? null : tmp.toCharArray();
                break;
            case ClobType:
                value = rs.getClob(column);
                break;
            case BlobType:
                value = rs.getBlob(column);
                break;
            case InputStreamType:
                value = rs.getBinaryStream(column);
                break;
            case ReaderType:
                value = rs.getCharacterStream(column);
                break;
        }
        return (T) value;
    }
}
