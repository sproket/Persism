package net.sf.persism;

import net.sf.persism.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.sf.persism.Util.*;

/**
 * DB and POJO related Metadata collected based connection url
 *
 * @author Dan Howard
 * @since 3/31/12 4:19 PM
 */
final class MetaData {

    private static final Log log = Log.getLogger(MetaData.class);

    // properties for each class - static because this won't need to change between MetaData instances (collection is unmodifiable)
    private static final Map<Class<?>, Collection<PropertyInfo>> propertyMap = new ConcurrentHashMap<>(32);

    // column to property map for each class
    private final Map<Class<?>, Map<String, PropertyInfo>> propertyInfoMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, Map<String, ColumnInfo>> columnInfoMap = new ConcurrentHashMap<>(32);

    // SQL for updates/inserts/deletes/selects for each class
    private final Map<Class<?>, String> updateStatementsMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, String> deleteStatementsMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, String> selectStatementsMap = new ConcurrentHashMap<>(32);

    // key - class, value - map key: columns to include, value: associated INSERT statement - this handles defaults on columns which may not need to be specified
    private final Map<Class<?>, Map<String, String>> insertStatements = new ConcurrentHashMap<>(32);

    // key - class, value - map key: changed columns, value: associated UPDATE statement
    private final Map<Class<?>, Map<String, String>> variableUpdateStatements = new ConcurrentHashMap<>(32);

    // Where clauses for primary key queries for tables
    private final Map<Class<?>, String> primaryWhereClauseMap = new ConcurrentHashMap<>(32);

    // Where ID IN (?, ?, ?) kinds of queries when no SQL is used.
    private final Map<Class<?>, Map<Integer, String>> primaryInClauseMap = new ConcurrentHashMap<>(32);

    // SQL parsed from SQL.where() - key is WHERE, value is full SELECT (maintained by SessionHelper)
    Map<Class<?>, Map<String, String>> whereClauses = new ConcurrentHashMap<>(32);

    // WHERE clauses defined by JOIN operations (maintained by SessionHelper)
    Map<JoinInfo, Map<String, String>> childWhereClauses = new ConcurrentHashMap<>(32);

    // table/view for each class
    private final Map<Class<?>, TableInfo> tableOrViewMap = new ConcurrentHashMap<>(32);

    // list of tables in the DB
    private final Set<TableInfo> tables = new HashSet<>();

    // list of views in the DB
    private final Set<TableInfo> views = new HashSet<>();

    static final Map<String, MetaData> metaData = new ConcurrentHashMap<>(4);

    private final ConnectionTypes connectionType;

    // the "extra" characters that can be used in unquoted identifier names (those beyond a-z, A-Z, 0-9 and _)
    // Was using DatabaseMetaData getExtraNameCharacters() but some drivers don't provide these and still allow
    // for non-alphanumeric characters in column names. We'll just use a static set.
    private static final String EXTRA_NAME_CHARACTERS = "`~!@#$%^&*()-+=/|\\{}[]:;'\".,<>*";
    private static final String SELECT_FOR_COLUMNS = "SELECT * FROM {0}{1}{2} WHERE 1=0";
    private static final String SELECT_FOR_COLUMNS_WITH_SCHEMA = "SELECT * FROM {0}{1}{2}.{3}{4}{5} WHERE 1=0";

    private MetaData(Connection con, String sessionKey) throws SQLException {

        log.debug("MetaData CREATING instance [%s] ", sessionKey);

        connectionType = ConnectionTypes.get(sessionKey);
        if (connectionType == ConnectionTypes.Other) {
            log.warn(Messages.UnknownConnectionType.message(con.getMetaData().getDatabaseProductName()));
        }
        populateTableList(con);
    }

    static synchronized MetaData getInstance(Connection con, String sessionKey) throws SQLException {

        if (sessionKey == null) {
            sessionKey = con.getMetaData().getURL();
        }

        if (metaData.get(sessionKey) == null) {
            metaData.put(sessionKey, new MetaData(con, sessionKey));
        }
        log.debug("MetaData getting instance %s", sessionKey);
        return metaData.get(sessionKey);
    }

    // Should only be called IF the map does not contain the column meta information yet.
    // Version for Tables
    private synchronized <T> Map<String, PropertyInfo> determinePropertyInfo(Class<T> objectClass, TableInfo table, Connection connection) {
        // double check map
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }

        // Not for @NotTable classes
        assert objectClass.getAnnotation(NotTable.class) == null;

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        ResultSet rs = null;
        Statement st = null;
        try {
            st = connection.createStatement();
            // gives us real column names with case.
            String sql;
            if (isEmpty(table.schema())) {
                sql = MessageFormat.format(SELECT_FOR_COLUMNS, sd, table.name(), ed);
            } else {
                sql = MessageFormat.format(SELECT_FOR_COLUMNS_WITH_SCHEMA, sd, table.schema(), ed, sd, table.name(), ed);
            }
            if (log.isDebugEnabled()) {
                log.debug("determineColumns: %s", sql);
            }
            rs = st.executeQuery(sql);
            Map<String, PropertyInfo> columns = determinePropertyInfoFromResultSet(objectClass, rs);
            propertyInfoMap.put(objectClass, columns);
            return columns;
        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        } finally {
            cleanup(st, rs);
        }
    }

    private <T> Map<String, PropertyInfo> determinePropertyInfoFromResultSet(Class<T> objectClass, ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        Collection<PropertyInfo> properties = getPropertyInfo(objectClass);

        int columnCount = rsmd.getColumnCount();

        Map<String, PropertyInfo> columns = new LinkedHashMap<>(columnCount);
        for (int j = 1; j <= columnCount; j++) {
            String realColumnName = rsmd.getColumnLabel(j);
            String columnName = realColumnName.toLowerCase().replace("_", "").replace(" ", "");
            // also replace these characters
            for (int x = 0; x < EXTRA_NAME_CHARACTERS.length(); x++) {
                columnName = columnName.replace("" + EXTRA_NAME_CHARACTERS.charAt(x), "");
            }
            PropertyInfo foundProperty = null;
            for (PropertyInfo propertyInfo : properties) {
                String checkName = propertyInfo.propertyName.toLowerCase().replace("_", "");
                if (checkName.equalsIgnoreCase(columnName)) {
                    foundProperty = propertyInfo;
                    break;
                } else {
                    // check annotation against column name
                    Column column = (Column) propertyInfo.getAnnotation(Column.class);
                    if (column != null) {
                        if (column.name().equalsIgnoreCase(realColumnName)) {
                            foundProperty = propertyInfo;
                            break;
                        }
                    }
                }
            }

            if (foundProperty != null) {
                columns.put(realColumnName, foundProperty);
            } else {
                log.warn(Messages.NoPropertyFoundForColumn.message(realColumnName, objectClass));
            }
        }
        return columns;
    }

    @SuppressWarnings({"JDBCExecuteWithNonConstantString", "SqlDialectInspection"})
    private synchronized <T> Map<String, ColumnInfo> determineColumnInfo(Class<T> objectClass, TableInfo table, Connection connection) {
        if (columnInfoMap.containsKey(objectClass)) {
            return columnInfoMap.get(objectClass);
        }

        Statement st = null;
        ResultSet rs = null;
        Map<String, PropertyInfo> properties = getTableColumnsPropertyInfo(objectClass, connection);
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        String schemaName = table.schema();
        String tableName = table.name();

        try {
            st = connection.createStatement();
            String sql;
            if (isEmpty(schemaName)) {
                sql = MessageFormat.format(SELECT_FOR_COLUMNS, sd, tableName, ed);
            } else {
                sql = MessageFormat.format(SELECT_FOR_COLUMNS_WITH_SCHEMA, sd, schemaName, ed, sd, tableName, ed);
            }
            log.debug("determineColumnInfo %s", sql);
            rs = st.executeQuery(sql);

            // Make sure primary keys sorted by column order in case we have more than 1
            // then we'll know the order to apply the parameters.
            Map<String, ColumnInfo> map = new LinkedHashMap<>(32);

            boolean primaryKeysFound = false;

            // Grab all columns and make first pass to detect primary auto-inc
            ResultSetMetaData rsMetaData = rs.getMetaData();
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                // only include columns where we have a property
                if (properties.containsKey(rsMetaData.getColumnLabel(i))) {
                    ColumnInfo columnInfo = new ColumnInfo();
                    columnInfo.columnName = rsMetaData.getColumnLabel(i);
                    columnInfo.autoIncrement = rsMetaData.isAutoIncrement(i);
                    columnInfo.primary = columnInfo.autoIncrement;
                    columnInfo.sqlColumnType = rsMetaData.getColumnType(i);
                    columnInfo.sqlColumnTypeName = rsMetaData.getColumnTypeName(i);
                    columnInfo.columnType = Types.convert(columnInfo.sqlColumnType);
                    columnInfo.length = rsMetaData.getColumnDisplaySize(i);

                    if (!primaryKeysFound) {
                        primaryKeysFound = columnInfo.primary;
                    }

                    PropertyInfo propertyInfo = properties.get(rsMetaData.getColumnLabel(i));
                    Annotation annotation = propertyInfo.getAnnotation(Column.class);

                    if (annotation != null) {
                        Column col = (Column) annotation;
                        if (col.hasDefault()) {
                            columnInfo.hasDefault = true;
                        }

                        if (col.primary()) {
                            columnInfo.primary = true;
                            if (objectClass.getAnnotation(NotTable.class) != null || objectClass.getAnnotation(View.class) != null) {
                                log.warn(Messages.PrimaryAnnotationOnViewOrQueryMakesNoSense.message(objectClass, propertyInfo.propertyName));
                            }
                        }

                        if (col.autoIncrement()) {
                            columnInfo.autoIncrement = true;
                            if (!columnInfo.columnType.isEligibleForAutoinc()) {
                                // This will probably cause some error or other problem. Notify the user.
                                log.warn(Messages.ColumnAnnotatedAsAutoIncButNAN.message(columnInfo.columnName, columnInfo.columnType));
                            }
                        }

                        if (!primaryKeysFound) {
                            primaryKeysFound = columnInfo.primary;
                        }
                    }

                    map.put(columnInfo.columnName, columnInfo);
                }
            }
            rs.close();

            DatabaseMetaData dmd = connection.getMetaData();

            if (objectClass.getAnnotation(View.class) == null) {

                if (isEmpty(schemaName)) {
                    rs = dmd.getPrimaryKeys(null, connectionType.getSchemaPattern(), tableName);
                } else {
                    rs = dmd.getPrimaryKeys(null, schemaName, tableName);
                }
                int primaryKeysCount = 0;
                while (rs.next()) {
                    ColumnInfo columnInfo = map.get(rs.getString("COLUMN_NAME"));
                    if (columnInfo != null) {
                        columnInfo.primary = true;

                        if (!primaryKeysFound) {
                            primaryKeysFound = columnInfo.primary;
                        }
                    }
                    primaryKeysCount++;
                }

                if (primaryKeysCount == 0 && !primaryKeysFound) {
                    log.warn(Messages.DatabaseMetaDataCouldNotFindPrimaryKeys.message(table));
                }
            }

            /*
             Get columns from database metadata since we don't get Type from resultSetMetaData
             with SQLite. + We also need to know if there's a default on a column.
             */
            if (isEmpty(schemaName)) {
                rs = dmd.getColumns(null, connectionType.getSchemaPattern(), tableName, null);
            } else {
                rs = dmd.getColumns(null, schemaName, tableName, null);
            }
            int columnsCount = 0;
            while (rs.next()) {
                ColumnInfo columnInfo = map.get(rs.getString("COLUMN_NAME"));
                if (columnInfo != null) {
                    if (!columnInfo.hasDefault) {
                        columnInfo.hasDefault = containsColumn(rs, "COLUMN_DEF") && rs.getString("COLUMN_DEF") != null;
                    }

                    // Do we not have autoinc info here? Yes.
                    // IS_AUTOINCREMENT = NO or YES
                    if (!columnInfo.autoIncrement) {
                        columnInfo.autoIncrement = containsColumn(rs, "IS_AUTOINCREMENT") && "YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT"));
                    }

                    // Re-assert the type since older version of SQLite could not detect types with empty resultsets
                    // It seems OK now in the newer JDBC driver.
                    // See testTypes unit test in TestSQLite
                    if (containsColumn(rs, "DATA_TYPE")) {
                        columnInfo.sqlColumnType = rs.getInt("DATA_TYPE");
                        if (containsColumn(rs, "TYPE_NAME")) {
                            columnInfo.sqlColumnTypeName = rs.getString("TYPE_NAME");
                        }
                        columnInfo.columnType = Types.convert(columnInfo.sqlColumnType);
                    }
                }
                columnsCount++;
            }
            rs.close();

            if (columnsCount == 0) {
                log.warn(Messages.DatabaseMetaDataCouldNotFindColumns.message(table));
            }

            // FOR Oracle which doesn't set autoinc in metadata even if we have:
            // "ID" NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY
            // Apparently that's not enough for the Oracle JDBC driver to indicate this is autoinc.
            // If we have a primary that's NUMERIC and HAS a default AND autoinc is not set then set it.
            if (connectionType == ConnectionTypes.Oracle) {
                Optional<ColumnInfo> autoInc = map.values().stream().filter(e -> e.autoIncrement).findFirst();
                if (autoInc.isEmpty()) {
                    // Do a second check if we have a primary that's numeric with a default.
                    Optional<ColumnInfo> primaryOpt = map.values().stream().filter(e -> e.primary).findFirst();
                    if (primaryOpt.isPresent()) {
                        ColumnInfo primary = primaryOpt.get();
                        if (primary.columnType.isEligibleForAutoinc() && primary.hasDefault) {
                            primary.autoIncrement = true;
                            primaryKeysFound = true;
                        }
                    }
                }
            }

            if (!primaryKeysFound && objectClass.getAnnotation(View.class) == null) {
                // Should we fail-fast? Actually no, we should not fail here.
                // It's very possible the user has a table that they will never
                // update, delete or select (by primary).
                // They may only want to do read operations with specified queries and in that
                // context we don't need any primary keys. (same with insert)
                log.warn(Messages.NoPrimaryKeyFoundForTable.message(table));
            }

            columnInfoMap.put(objectClass, map);
            return map;

        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        } finally {
            cleanup(st, rs);
        }
    }

    static <T> Collection<PropertyInfo> getPropertyInfo(Class<T> objectClass) {
        if (propertyMap.containsKey(objectClass)) {
            return propertyMap.get(objectClass);
        }
        return determinePropertyInfo(objectClass);
    }

    private static synchronized <T> Collection<PropertyInfo> determinePropertyInfo(Class<T> objectClass) {
        if (propertyMap.containsKey(objectClass)) {
            return propertyMap.get(objectClass);
        }

        Map<String, PropertyInfo> propertyInfos = new HashMap<>(32);

        List<Field> fields = new ArrayList<>(32);

        // getDeclaredFields does not get fields from super classes.....
        fields.addAll(Arrays.asList(objectClass.getDeclaredFields()));
        Class<?> sup = objectClass.getSuperclass();
        log.debug("fields for %s", sup);
        while (!sup.equals(Object.class) && !sup.equals(PersistableObject.class)) {
            fields.addAll(Arrays.asList(sup.getDeclaredFields()));
            sup = sup.getSuperclass();
            log.debug("fields for %s", sup);
        }

        Method[] methods = objectClass.getMethods();

        for (Field field : fields) {
            // Skip static fields
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
//            log.debug("Field Name: %s", field.getName());
            String propertyName = field.getName();
//            log.debug("Property Name: *%s* ", propertyName);

            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.propertyName = propertyName;
            propertyInfo.field = field;
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                propertyInfo.annotations.put(annotation.annotationType(), annotation);
            }

            for (Method method : methods) {
                String propertyNameToTest = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                // log.debug("property name for testing %s", propertyNameToTest);
                if (propertyNameToTest.startsWith("Is") && propertyNameToTest.length() > 2 && Character.isUpperCase(propertyNameToTest.charAt(2))) {
                    propertyNameToTest = propertyName.substring(2);
                }

                String[] candidates = {"set" + propertyNameToTest, "get" + propertyNameToTest, "is" + propertyNameToTest, field.getName()};

                if (Arrays.asList(candidates).contains(method.getName())) {
                    //log.debug("  METHOD: %s", method.getName());

                    annotations = method.getAnnotations();
                    for (Annotation annotation : annotations) {
                        propertyInfo.annotations.put(annotation.annotationType(), annotation);
                    }

                    // OR added to fix to builder pattern style when your setters are just the field name
                    if (method.getName().equalsIgnoreCase("set" + propertyNameToTest) || method.getParameterCount() > 0) {
                        propertyInfo.setter = method;
                    } else {
                        propertyInfo.getter = method;
                    }
                }
            }

            propertyInfo.readOnly = propertyInfo.setter == null;
            propertyInfo.isJoin = propertyInfo.getAnnotation(Join.class) != null;
            propertyInfos.put(propertyName.toLowerCase(), propertyInfo);
        }

        // Remove any properties found with the NotColumn annotation
        // http://stackoverflow.com/questions/2026104/hashmap-keyset-foreach-and-remove
        Iterator<Map.Entry<String, PropertyInfo>> it = propertyInfos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PropertyInfo> entry = it.next();
            PropertyInfo info = entry.getValue();
            // added support for transient
            if (info.getAnnotation(NotColumn.class) != null || Modifier.isTransient(info.field.getModifiers())) {
                it.remove();
            }
        }

        Collection<PropertyInfo> properties = Collections.unmodifiableCollection(propertyInfos.values());
        propertyMap.put(objectClass, properties);

        // If a view or query - warn if we find any setters
        if (objectClass.getAnnotation(NotTable.class) != null || objectClass.getAnnotation(View.class) != null) {
            List<String> setters = new ArrayList<>();
            for (PropertyInfo propertyInfo : properties) {
                if (propertyInfo.setter != null) {
                    setters.add(propertyInfo.propertyName);
                }
            }

            if (setters.size() > 0) {
                log.warn(Messages.SettersFoundInReadOnlyObject.message(objectClass, setters));
            }
        }

        return properties;
    }

    private static final String TABLE = "TABLE";
    private static final String VIEW = "VIEW";
    private static final String[] tableTypes = {TABLE, VIEW};

    // Populates the tables list with table names from the DB.
    // This list is used for discovery of the table name from a class.
    // ONLY to be called from Init in a synchronized way.
    // NULL POINTER WITH
    // http://social.msdn.microsoft.com/Forums/en-US/sqldataaccess/thread/5c74094a-8506-4278-ac1c-f07d1bfdb266
    // solution:
    // http://stackoverflow.com/questions/8988945/java7-sqljdbc4-sql-error-08s01-on-getconnection
    private void populateTableList(Connection con) throws PersismException {
        try (ResultSet rs = con.getMetaData().getTables(null, connectionType.getSchemaPattern(), null, tableTypes)) {
            String name;
            while (rs.next()) {
                name = rs.getString("TABLE_NAME");
                if (VIEW.equalsIgnoreCase(rs.getString("TABLE_TYPE"))) {
                    views.add(new TableInfo(name, rs.getString("TABLE_SCHEM")));
                } else {
                    tables.add(new TableInfo(name, rs.getString("TABLE_SCHEM")));
                }
            }
        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    /**
     * @param object
     * @param connection
     * @return sql update string
     * @throws NoChangesDetectedForUpdateException if the data object implements Persistable and there are no changes detected
     */
    String getUpdateStatement(Object object, Connection connection) throws PersismException, NoChangesDetectedForUpdateException {

        String sql;
        if (object instanceof Persistable<?> pojo) {
            Map<String, PropertyInfo> changes = getChangedProperties(pojo, connection);
            if (changes.size() == 0) {
                throw new NoChangesDetectedForUpdateException();
            }

            Class<?> objectClass = object.getClass();
            String key = changes.keySet().toString();
            if (variableUpdateStatements.containsKey(objectClass) && variableUpdateStatements.get(objectClass).containsKey(key)) {
                sql = variableUpdateStatements.get(objectClass).get(key);
            } else {
                sql = determineUpdateStatement(pojo, connection);
            }

            if (log.isDebugEnabled()) {
                log.debug("getUpdateStatement for %s for changed fields is %s", objectClass, sql);
            }
            return sql;
        }

        if (updateStatementsMap.containsKey(object.getClass())) {
            sql = updateStatementsMap.get(object.getClass());
        } else {
            sql = determineUpdateStatement(object, connection);
        }
        if (log.isDebugEnabled()) {
            log.debug("getUpdateStatement for: %s %s", object.getClass(), sql);
        }
        return sql;
    }

    // Used by Objects not implementing Persistable since they will always use the same update statement
    private synchronized String determineUpdateStatement(Object object, Connection connection) {
        if (updateStatementsMap.containsKey(object.getClass())) {
            return updateStatementsMap.get(object.getClass());
        }

        Class<?> objectClass = object.getClass();
        Map<String, PropertyInfo> columns;
        if (object instanceof Persistable<?> pojo) {
            columns = getChangedProperties(pojo, connection);
        } else {
            columns = getTableColumnsPropertyInfo(objectClass, connection);
        }
        String updateStatement = buildUpdateString(object, columns.keySet().iterator(), connection);

        if (object instanceof Persistable<?>) {
            String key = columns.keySet().toString();
            if (variableUpdateStatements.containsKey(objectClass) && variableUpdateStatements.get(objectClass).containsKey(key)) {
                return variableUpdateStatements.get(objectClass).get(key);
            }

            variableUpdateStatements.putIfAbsent(objectClass, new HashMap<>());
            variableUpdateStatements.get(objectClass).put(key, updateStatement);
        } else {
            updateStatementsMap.put(objectClass, updateStatement);
        }


        if (log.isDebugEnabled()) {
            log.debug("determineUpdateStatement for %s is %s", objectClass, updateStatement);
        }

        return updateStatement;
    }

    String getInsertStatement(Object object, Connection connection) throws PersismException {
        String sql;
        String key = getColumnsForInsert(object, connection).stream().map(columnInfo -> columnInfo.columnName).toList().toString();
        Class<?> objectClass = object.getClass();
        if (insertStatements.containsKey(objectClass) && insertStatements.get(objectClass).containsKey(key)) {
            sql = insertStatements.get(objectClass).get(key);
        } else {
            sql = determineInsertStatement(object, connection);
        }
        if (log.isDebugEnabled()) {
            log.debug("getInsertStatement for: %s %s", objectClass, sql);
        }
        return sql;
    }

    // The insert statement may vary if the column has a default and the default was not specified but if it WAS specified it should be included.
    private synchronized String determineInsertStatement(Object object, Connection connection) {
        List<ColumnInfo> columnsForInsert = getColumnsForInsert(object, connection);
        String key = columnsForInsert.stream().map(columnInfo -> columnInfo.columnName).toList().toString();
        Class<?> objectClass = object.getClass();

        if (insertStatements.containsKey(objectClass) && insertStatements.get(objectClass).containsKey(key)) {
            return insertStatements.get(objectClass).get(key);
        }

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        TableInfo tableInfo = getTableInfo(objectClass);
        String tableName = tableInfo.name();
        String schemaName = tableInfo.schema();

        StringBuilder sbi = new StringBuilder();
        sbi.append("INSERT INTO ");
        if (isNotEmpty(schemaName)) {
            sbi.append(sd).append(schemaName).append(ed).append(".");
        }
        sbi.append(sd).append(tableName).append(ed).append(" (");

        StringBuilder sbp = new StringBuilder();
        sbp.append(") VALUES (");

        String sep = "";

        for (ColumnInfo column : columnsForInsert) {
            sbi.append(sep).append(sd).append(column.columnName).append(ed);
            sbp.append(sep).append("?");
            sep = ", ";
        }

        sbi.append(sbp).append(") ");

        String insertStatement;
        insertStatement = sbi.toString();

        if (log.isDebugEnabled()) {
            log.debug("determineInsertStatement for %s is %s", object.getClass(), insertStatement);
        }

        insertStatements.putIfAbsent(objectClass, new HashMap<>());
        insertStatements.get(objectClass).put(key, insertStatement);

        return insertStatement;
    }

    private List<ColumnInfo> getColumnsForInsert(Object object, Connection connection) {
        Map<String, ColumnInfo> columns = getColumns(object.getClass(), connection);
        Map<String, PropertyInfo> properties = getTableColumnsPropertyInfo(object.getClass(), connection);
        return columns.values().stream().
                filter(columnInfo -> !columnInfo.autoIncrement).
                filter(columnInfo -> !columnInfo.hasDefault || properties.get(columnInfo.columnName).getValue(object) != null).
                toList();
    }

    String getDeleteStatement(Object object, Connection connection) {
        if (deleteStatementsMap.containsKey(object.getClass())) {
            return deleteStatementsMap.get(object.getClass());
        }
        return determineDeleteStatement(object, connection);
    }

    private synchronized String determineDeleteStatement(Object object, Connection connection) {
        Class<?> objectClass = object.getClass();

        if (deleteStatementsMap.containsKey(objectClass)) {
            return deleteStatementsMap.get(objectClass);
        }

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        TableInfo tableInfo = getTableInfo(objectClass);
        String tableName = tableInfo.name();
        String schemaName = tableInfo.schema();

        List<String> primaryKeys = getPrimaryKeys(objectClass, connection);

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        if (isNotEmpty(schemaName)) {
            sb.append(sd).append(schemaName).append(ed).append(".");
        }
        sb.append(sd).append(tableName).append(ed).append(" WHERE ");
        String sep = "";
        for (String column : primaryKeys) {
            sb.append(sep).append(sd).append(column).append(ed).append(" = ?");
            sep = " AND ";
        }

        String deleteStatement = sb.toString();

        if (log.isDebugEnabled()) {
            log.debug("determineDeleteStatement for %s is %s", object.getClass(), deleteStatement);
        }

        deleteStatementsMap.put(object.getClass(), deleteStatement);
        return deleteStatement;
    }

    String getPrimaryInClause(Class<?> objectClass, int paramCount, Connection connection) {
        if (primaryInClauseMap.containsKey(objectClass) && primaryInClauseMap.get(objectClass).containsKey(paramCount)) {
            return primaryInClauseMap.get(objectClass).get(paramCount);
        }
        return determinePrimaryInClause(objectClass, paramCount, connection);
    }

    private synchronized String determinePrimaryInClause(Class<?> objectClass, int paramCount, Connection connection) {
        if (primaryInClauseMap.containsKey(objectClass) && primaryInClauseMap.get(objectClass).containsKey(paramCount)) {
            return primaryInClauseMap.get(objectClass).get(paramCount);
        }

        Map<Integer, String> map = primaryInClauseMap.get(objectClass);
        if (map == null) {
            map = new HashMap<>();
        }

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();
        String andSep = "";

        String query = getDefaultSelectStatement(objectClass, connection);
        int n = query.indexOf(" WHERE");
        query = query.substring(0, n + 7);

        List<String> primaryKeys = getPrimaryKeys(objectClass, connection);

        StringBuilder sb = new StringBuilder(query);
        int groups = paramCount / primaryKeys.size();
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
        query = sb.toString();

        map.put(paramCount, query);
        primaryInClauseMap.put(objectClass, map);

        return query;
    }


    String getWhereClause(Class<?> objectClass, Connection connection) {
        if (primaryWhereClauseMap.containsKey(objectClass)) {
            return primaryWhereClauseMap.get(objectClass);
        }
        return determineWhereClause(objectClass, connection);
    }

    private synchronized String determineWhereClause(Class<?> objectClass, Connection connection) {
        if (primaryWhereClauseMap.containsKey(objectClass)) {
            return primaryWhereClauseMap.get(objectClass);
        }

        String sep = "";

        StringBuilder sb = new StringBuilder();
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        List<String> primaryKeys = getPrimaryKeys(objectClass, connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeysForWhere.message(getTableInfo(objectClass)));
        }

        sb.append(" WHERE ");

        sep = "";
        for (String column : primaryKeys) {
            sb.append(sep).append(sd).append(column).append(ed).append(" = ?");
            sep = " AND ";
        }

        String where = sb.toString();
        if (log.isDebugEnabled()) {
            log.debug("determineWhereClause: %s %s", objectClass.getName(), where);
        }
        primaryWhereClauseMap.put(objectClass, where);
        return where;
    }

    /**
     * Default SELECT including WHERE Primary Keys
     *
     * @param objectClass
     * @param connection
     * @return
     */
    String getDefaultSelectStatement(Class<?> objectClass, Connection connection) {
        if (objectClass.getAnnotation(View.class) != null) {
            return getSelectStatement(objectClass, connection);
        }

        return getSelectStatement(objectClass, connection) + getWhereClause(objectClass, connection);
    }

    /**
     * SQL SELECT COLUMNS ONLY - make public? or put a delegate somewhere else?
     *
     * @param objectClass
     * @param connection
     * @return
     */
    String getSelectStatement(Class<?> objectClass, Connection connection) {
        if (selectStatementsMap.containsKey(objectClass)) {
            return selectStatementsMap.get(objectClass);
        }
        return determineSelectStatement(objectClass, connection);
    }

    private synchronized String determineSelectStatement(Class<?> objectClass, Connection connection) {

        if (selectStatementsMap.containsKey(objectClass)) {
            return selectStatementsMap.get(objectClass);
        }

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        TableInfo tableInfo = getTableInfo(objectClass);
        String tableName = tableInfo.name();
        String schemaName = tableInfo.schema();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        String sep = "";

        Map<String, ColumnInfo> columns = getColumns(objectClass, connection);
        for (String column : columns.keySet()) {
            ColumnInfo columnInfo = columns.get(column);
            sb.append(sep).append(sd).append(columnInfo.columnName).append(ed);
            sep = ", ";
        }
        sb.append(" FROM ");
        if (isNotEmpty(schemaName)) {
            sb.append(sd).append(schemaName).append(ed).append('.');
        }
        sb.append(sd).append(tableName).append(ed);


        String selectStatement = sb.toString();

        if (log.isDebugEnabled()) {
            log.debug("determineSelectStatement for %s is %s", objectClass, selectStatement);
        }

        selectStatementsMap.put(objectClass, selectStatement);

        return selectStatement;
    }

    private String buildUpdateString(Object object, Iterator<String> it, Connection connection) throws PersismException {
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        TableInfo tableInfo = getTableInfo(object.getClass());
        String tableName = tableInfo.name();
        String schemaName = tableInfo.schema();

        List<String> primaryKeys = getPrimaryKeys(object.getClass(), connection);

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        if (isNotEmpty(schemaName)) {
            sb.append(sd).append(schemaName).append(ed).append(".");
        }
        sb.append(sd).append(tableName).append(ed).append(" SET ");
        String sep = "";

        Map<String, ColumnInfo> columns = getColumns(object.getClass(), connection);
        while (it.hasNext()) {
            String column = it.next();
            ColumnInfo columnInfo = columns.get(column);
            if (columnInfo.autoIncrement || columnInfo.primary) {
                log.debug("buildUpdateString: skipping " + column);
            } else {
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

    Map<String, PropertyInfo> getChangedProperties(Persistable<?> persistable, Connection connection) throws PersismException {

        Persistable<?> original = (Persistable<?>) persistable.readOriginalValue();

        Map<String, PropertyInfo> columns = getTableColumnsPropertyInfo(persistable.getClass(), connection);

        if (original == null) {
            // Could happen in the case of cloning or other operation - so it's never read, so it never sets original.
            return columns;
        } else {
            Map<String, PropertyInfo> changedColumns = new LinkedHashMap<>(columns.keySet().size());

            for (String column : columns.keySet()) {
                PropertyInfo propertyInfo = columns.get(column);

                Object newValue = propertyInfo.getValue(persistable);
                Object orgValue = propertyInfo.getValue(original);
                if (!Objects.equals(newValue, orgValue)) {
                    changedColumns.put(column, propertyInfo);
                }
            }
            return changedColumns;
        }

    }

    <T> Map<String, ColumnInfo> getColumns(Class<T> objectClass, Connection connection) throws PersismException {
        // Realistically at this point this objectClass will always be in the map since it's defined early
        // when we get the table name, but I'll double-check it for determineColumnInfo anyway.
        if (columnInfoMap.containsKey(objectClass)) {
            return columnInfoMap.get(objectClass);
        }
        return determineColumnInfo(objectClass, getTableInfo(objectClass), connection);
    }

    <T> Map<String, PropertyInfo> getQueryColumnsPropertyInfo(Class<T> objectClass, ResultSet rs) throws PersismException {
        try {
            return determinePropertyInfoFromResultSet(objectClass, rs);
        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    <T> Map<String, PropertyInfo> getTableColumnsPropertyInfo(Class<T> objectClass, Connection connection) throws PersismException {
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }
        return determinePropertyInfo(objectClass, getTableInfo(objectClass), connection);
    }

    <T> TableInfo getTableInfo(Class<T> objectClass) {
        if (tableOrViewMap.containsKey(objectClass)) {
            return tableOrViewMap.get(objectClass);
        }

        return determineTableInfo(objectClass);
    }

    private synchronized <T> TableInfo determineTableInfo(Class<T> objectClass) {
        if (tableOrViewMap.containsKey(objectClass)) {
            return tableOrViewMap.get(objectClass);
        }

        String tableName;
        String schemaName; // todo schemaName if they specify it in the annotation!
        TableInfo foundInfo = null;

        Table tableAnnotation = objectClass.getAnnotation(Table.class);
        View viewAnnotation = objectClass.getAnnotation(View.class);
        if (tableAnnotation != null) {
            tableName = tableAnnotation.value();
            if (tableName.contains(".")) {
                // needed....
            }
            // double check against stored table names to get the actual case of the name
            // todo also check if there's more than 1 table or view if no schema name is provided.
            boolean found = false;
            for (TableInfo table : tables) {
                if (table.name().equalsIgnoreCase(tableName)) {
                    foundInfo = table;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new PersismException(Messages.CouldNotFindTableNameInTheDatabase.message(tableName, objectClass.getName()));
            }
        } else if (viewAnnotation != null && isNotEmpty(viewAnnotation.value())) {

            tableName = viewAnnotation.value();

            // double check against stored view names to get the actual case of the name
            boolean found = false;
            for (TableInfo view : views) {
                if (view.name().equalsIgnoreCase(tableName)) {
                    foundInfo = view;
                    found = true;
                }
            }
            if (!found) {
                throw new PersismException(Messages.CouldNotFindViewNameInTheDatabase.message(tableName, objectClass.getName()));
            }
        } else {
            foundInfo = guessTableOrView(objectClass);
        }
        tableOrViewMap.put(objectClass, foundInfo);
        return foundInfo;
    }

    private TableInfo parseTableInfo(String tableNameAndSchema) {
        assert tableNameAndSchema.contains(".");
        return null; // todo parseTableInfo
    }

    // Returns the table/view name found in the DB in the same case as in the DB.
    // throws PersismException if we cannot guess any table/view name for this class.
    private <T> TableInfo guessTableOrView(Class<T> objectClass) throws PersismException {
        Set<String> guesses = new LinkedHashSet<>(6); // guess order is important
        List<TableInfo> guessedTables = new ArrayList<>(6);

        String className = objectClass.getSimpleName();

        Set<TableInfo> list;
        boolean isView = false;
        if (objectClass.getAnnotation(View.class) != null) {
            list = views;
            isView = true;
        } else {
            list = tables;
        }

        addTableGuesses(className, guesses);
        for (TableInfo table : list) {
            for (String guess : guesses) {
                if (guess.equalsIgnoreCase(table.name())) {
                    guessedTables.add(table);
                }
            }
        }
        if (guessedTables.size() == 0) {
            throw new PersismException(Messages.CouldNotDetermineTableOrViewForType.message(isView ? "view" : "table", objectClass.getName(), guesses));
        }

        if (guessedTables.size() > 1) {
            Set<String> multipleGuesses = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            multipleGuesses.addAll(guessedTables.stream().map(TableInfo::name).toList());
            throw new PersismException(Messages.CouldNotDetermineTableOrViewForTypeMultipleMatches.message(isView ? "view" : "table", objectClass.getName(), guesses, multipleGuesses));
        }
        return guessedTables.get(0);
    }

    private void addTableGuesses(String className, Collection<String> guesses) {
        // PascalCasing class name should make
        // PascalCasing
        // PascalCasings
        // Pascal Casing
        // Pascal Casings
        // Pascal_Casing
        // Pascal_Casings
        // Order is important.

        String guess;
        String pluralClassName;
        String pluralClassName2 = null;

        if (className.endsWith("y")) {
            // supply - supplies, category - categories
            pluralClassName = className.substring(0, className.length() - 1) + "ies";
            pluralClassName2 = className + "s"; // holiday
        } else if (className.endsWith("x")) {
            // tax - taxes, mailbox - mailboxes
            pluralClassName = className + "es";
        } else {
            pluralClassName = className + "s";
        }

        guesses.add(className);
        guesses.add(pluralClassName);
        if (pluralClassName2 != null) {
            guesses.add(pluralClassName2);
        }

        guess = camelToTitleCase(className);
        guesses.add(guess); // name with spaces
        guesses.add(guess.replaceAll(" ", "_")); // name with spaces changed to _

        guess = camelToTitleCase(pluralClassName);
        guesses.add(guess); // plural name with spaces
        guesses.add(guess.replaceAll(" ", "_")); // plural name with spaces changed to _

        if (pluralClassName2 != null) {
            guess = camelToTitleCase(pluralClassName2);
            guesses.add(guess); // plural name with spaces
            guesses.add(guess.replaceAll(" ", "_")); // plural name with spaces changed to _
        }
    }

    List<String> getPrimaryKeys(Class<?> objectClass, Connection connection) throws PersismException {

        // ensures meta-data will be available because this method could be called before getting the table info object
        TableInfo tableInfo = getTableInfo(objectClass);

        List<String> primaryKeys = new ArrayList<>(4);
        Map<String, ColumnInfo> map = getColumns(objectClass, connection);
        for (ColumnInfo col : map.values()) {
            if (col.primary) {
                primaryKeys.add(col.columnName);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("getPrimaryKeys for %s %s", tableInfo, primaryKeys);
        }
        return primaryKeys;
    }

    ConnectionTypes getConnectionType() {
        return connectionType;
    }

}
