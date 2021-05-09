package net.sf.persism;

import net.sf.persism.annotations.Column;
import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotTable;
import net.sf.persism.annotations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.text.MessageFormat.format;
import static net.sf.persism.Util.*;

/**
 * Meta data collected in a map singleton based on connection url
 *
 * @author Dan Howard
 * @since 3/31/12 4:19 PM
 */
final class MetaData {

    private static final Log log = Log.getLogger(MetaData.class);

    // properties for each class - static because this won't change between MetaData instances
    private static final Map<Class<?>, Collection<PropertyInfo>> propertyMap = new ConcurrentHashMap<>(32);
    // private static final Map<Class<?>, List<String>> propertyNames = new ConcurrentHashMap<>(32);

    // column to property map for each class
    private Map<Class<?>, Map<String, PropertyInfo>> propertyInfoMap = new ConcurrentHashMap<>(32);
    private Map<Class<?>, Map<String, ColumnInfo>> columnInfoMap = new ConcurrentHashMap<>(32);

    // table name for each class
    private Map<Class<?>, String> tableMap = new ConcurrentHashMap<>(32);

    // SQL for updates/inserts/deletes/selects for each class
    private Map<Class<?>, String> updateStatementsMap = new ConcurrentHashMap<>(32);
    private Map<Class<?>, String> insertStatementsMap = new ConcurrentHashMap<>(32);
    private Map<Class<?>, String> deleteStatementsMap = new ConcurrentHashMap<>(32);
    private Map<Class<?>, String> selectStatementsMap = new ConcurrentHashMap<>(32);


    // Key is SQL with named params, Value is SQL with ?
    // private Map<String, String> sqlWitNamedParams = new ConcurrentHashMap<String, String>(32);

    // Key is SQL with named params, Value list of named params
    // private Map<String, List<String>> namedParams = new ConcurrentHashMap<String, List<String>>(32);

    // private Map<Class, List<String>> primaryKeysMap = new ConcurrentHashMap<Class, List<String>>(32); // remove later maybe?

    // list of tables in the DB
    private Set<String> tableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    // Map of table names + meta data
    // private Map<String, TableInfo> tableInfoMap = new ConcurrentHashMap<String, TableInfo>(32);

    private static final Map<String, MetaData> metaData = new ConcurrentHashMap<String, MetaData>(4);

    private ConnectionTypes connectionType;

    // the "extra" characters that can be used in unquoted identifier names (those beyond a-z, A-Z, 0-9 and _)
    // Was using DatabaseMetaData getExtraNameCharacters() but some drivers don't provide these and still allow
    // for non alpha-numeric characters in column names. We'll just use a static set.
    private static final String EXTRA_NAME_CHARACTERS = "`~!@#$%^&*()-+=/|\\{}[]:;'\".,<>*";

    private MetaData(Connection con, String sessionKey) throws SQLException {

        log.debug("MetaData CREATING instance [%s] ", sessionKey);

        connectionType = ConnectionTypes.get(sessionKey);
        if (connectionType == ConnectionTypes.Other) {
            log.warn("Unknown connection type. Please contact Persism to add support for " + con.getMetaData().getDatabaseProductName());
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
    private synchronized <T> Map<String, PropertyInfo> determinePropertyInfo(Class<T> objectClass, String tableName, Connection connection) {
        // double check map
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        ResultSet rs = null;
        Statement st = null;
        try {
            st = connection.createStatement();
            // gives us real column names with case.
            String sql = MessageFormat.format("SELECT * FROM {0}{1}{2} WHERE 1=0", sd, tableName, ed); // todo this is repeated - put the string in a static final
            if (log.isDebugEnabled()) {
                log.debug("determineColumns: %s", sql);
            }
            rs = st.executeQuery(sql);
            return determinePropertyInfo(objectClass, rs);
        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        } finally {
            cleanup(st, rs);
        }
    }

    // Should only be called IF the map does not contain the column meta information yet.
    private synchronized <T> Map<String, PropertyInfo> determinePropertyInfo(Class<T> objectClass, ResultSet rs) {
        // double check map - note this could be called with a Query were we never have that in here
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }

        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            Collection<PropertyInfo> properties = getPropertyInfo(objectClass);

            int columnCount = rsmd.getColumnCount();
            //Map<String, PropertyInfo> columns = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
                    String propertyName = propertyInfo.propertyName.toLowerCase().replace("_", "");
                    if (propertyName.equalsIgnoreCase(columnName)) {
                        foundProperty = propertyInfo;
                        break;
                    } else {
                        // check annotation against column name
                        Annotation annotation = propertyInfo.getAnnotation(Column.class);
                        if (annotation != null) {
                            if (((Column) annotation).name().equalsIgnoreCase(realColumnName)) {
                                foundProperty = propertyInfo;
                                break;
                            }
                        }
                    }
                }

                if (foundProperty != null) {
                    columns.put(realColumnName, foundProperty);
                } else {
                    log.warn("Property not found for column: " + realColumnName + " class: " + objectClass);
                }
            }

            // Do not put query classes into the metadata. It's possible the 1st run has a query with missing columns
            // any calls afterward would fail because I never would refresh the columns again. Table is fine since we
            // can do a SELECT * to get all columns up front but we can't do that with a query.
            if (objectClass.getAnnotation(NotTable.class) == null) {
                propertyInfoMap.put(objectClass, columns);
            }

            return columns;

        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"JDBCExecuteWithNonConstantString", "SqlDialectInspection"})
    private synchronized <T> Map<String, ColumnInfo> determineColumnInfo(Class<T> objectClass, String tableName, Connection connection) {
        if (columnInfoMap.containsKey(objectClass)) {
            return columnInfoMap.get(objectClass);
        }

        Statement st = null;
        ResultSet rs = null;
        Map<String, PropertyInfo> properties = getTableColumnsPropertyInfo(objectClass, connection);
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        try {

            st = connection.createStatement();
            rs = st.executeQuery(format("SELECT * FROM {0}{1}{2} WHERE 1=0", sd, tableName, ed));

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
                        }

                        if (col.autoIncrement()) {
                            columnInfo.autoIncrement = true;
                            if (!columnInfo.columnType.isEligibleForAutoinc()) {
                                // This will probably cause some error or other problem. Notify the user.
                                log.warn("Column " + columnInfo.columnName + " is annotated as auto-increment but it is not a number type (" + columnInfo.columnType + ").");
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

            // Iterate primary keys and update column infos
            rs = dmd.getPrimaryKeys(null, connectionType.getSchemaPattern(), tableName);
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

            if (primaryKeysCount == 0) {
                log.warn("DatabaseMetaData could not find primary keys for table " + tableName + ".");
            }

            /*
             Get columns from database metadata since we don't get Type from resultSetMetaData
             with SQLite. + We also need to know if there's a default on a column.
             */
            rs = dmd.getColumns(null, connectionType.getSchemaPattern(), tableName, null);
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
                log.warn("DatabaseMetaData could not find columns for table " + tableName + "!");
            }

            // FOR Oracle which doesn't set autoinc in metadata even if we have:
            // "ID" NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY
            // Apparently that's not enough for the Oracle JDBC driver to indicate this is autoinc.
            // If we have a primary that's NUMERIC and HAS a default AND autoinc is not set then set it.
            if (connectionType == ConnectionTypes.Oracle) {
                Optional<ColumnInfo> autoInc = map.values().stream().filter(e -> e.autoIncrement).findFirst();
                if (!autoInc.isPresent()) {
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

            if (!primaryKeysFound) {
                // Should we fail-fast? Actually no, we should not fail here.
                // It's very possible the user has a table that they will never
                // update, delete or select (by primary).
                // They may only want to do read operations with specified queries and in that
                // context we don't need any primary keys. (same with insert)
                log.warn("No primary key found for table " + tableName + ". Do not use with update/delete/fetch or add a primary key.");
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

                    if (method.getName().equalsIgnoreCase("set" + propertyNameToTest)) {
                        propertyInfo.setter = method;
                    } else {
                        propertyInfo.getter = method;
                    }
                }
            }

            propertyInfo.readOnly = propertyInfo.setter == null;
            propertyInfos.put(propertyName.toLowerCase(), propertyInfo);
        }

        // Remove any properties found with the NotColumn annotation
        // http://stackoverflow.com/questions/2026104/hashmap-keyset-foreach-and-remove
        Iterator<Map.Entry<String, PropertyInfo>> it = propertyInfos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PropertyInfo> entry = it.next();
            PropertyInfo info = entry.getValue();
            if (info.getAnnotation(NotColumn.class) != null) {
                it.remove();
            }
        }

        Collection<PropertyInfo> properties = Collections.unmodifiableCollection(propertyInfos.values());
        propertyMap.put(objectClass, properties);
        return properties;
    }

    private static final String[] tableTypes = {"TABLE"};

    // Populates the tables list with table names from the DB.
    // This list is used for discovery of the table name from a class.
    // ONLY to be called from Init in a synchronized way.
    private void populateTableList(Connection con) throws PersismException {

        ResultSet rs = null;

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
            throw new PersismException(e.getMessage(), e);

        } finally {
            cleanup(null, rs);
        }
    }

    /**
     * @param object
     * @param connection
     * @return sql update string
     * @throws NoChangesDetectedForUpdateException if the data object implements Persistable and there are no changes detected
     */
    String getUpdateStatement(Object object, Connection connection) throws PersismException, NoChangesDetectedForUpdateException {

        if (object instanceof Persistable) {
            Map<String, PropertyInfo> changes = getChangedProperties((Persistable<?>) object, connection);
            if (changes.size() == 0) {
                throw new NoChangesDetectedForUpdateException();
            }
            // Note we don't not add Persistable updates to updateStatementsMap since they will be different each time.
            String sql = buildUpdateString(object, changes.keySet().iterator(), connection);
            if (log.isDebugEnabled()) {
                log.debug("getUpdateStatement for %s for changed fields is %s", object.getClass(), sql);
            }
            return sql;
        }

        String sql;
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

        Map<String, PropertyInfo> columns = getTableColumnsPropertyInfo(object.getClass(), connection);

        String updateStatement = buildUpdateString(object, columns.keySet().iterator(), connection);

        // Store static update statement for future use.
        updateStatementsMap.put(object.getClass(), updateStatement);

        if (log.isDebugEnabled()) {
            log.debug("determineUpdateStatement for %s is %s", object.getClass(), updateStatement);
        }

        return updateStatement;
    }


    // Note this will not include columns unless they have the associated property.
    String getInsertStatement(Object object, Connection connection) throws PersismException {
        String sql;

        if (insertStatementsMap.containsKey(object.getClass())) {
            sql = insertStatementsMap.get(object.getClass());
        } else {
            sql = determineInsertStatement(object, connection);
        }

        if (log.isDebugEnabled()) {
            log.debug("getInsertStatement for: %s %s", object.getClass(), sql);
        }
        return sql;
    }

    private synchronized String determineInsertStatement(Object object, Connection connection) {
        if (insertStatementsMap.containsKey(object.getClass())) {
            return insertStatementsMap.get(object.getClass());
        }

        try {
            String tableName = getTableName(object.getClass(), connection);
            String sd = connectionType.getKeywordStartDelimiter();
            String ed = connectionType.getKeywordEndDelimiter();

            Map<String, ColumnInfo> columns = getColumns(object.getClass(), connection);
            Map<String, PropertyInfo> properties = getTableColumnsPropertyInfo(object.getClass(), connection);

            StringBuilder sbi = new StringBuilder();
            sbi.append("INSERT INTO ").append(sd).append(tableName).append(ed).append(" (");

            StringBuilder sbp = new StringBuilder();
            sbp.append(") VALUES (");

            String sep = "";
            boolean saveInMap = true;

            for (ColumnInfo column : columns.values()) {
                if (!column.autoIncrement) {

                    if (column.hasDefault) {

                        saveInMap = false;

                        // Do not include if this column has a default and no value has been
                        // set on it's associated property.
                        if (properties.get(column.columnName).getter.invoke(object) == null) {
                            continue;
                        }

                    }

                    sbi.append(sep).append(sd).append(column.columnName).append(ed);
                    sbp.append(sep).append("?");
                    sep = ", ";
                }
            }

            sbi.append(sbp).append(") ");

            String insertStatement;
            insertStatement = sbi.toString();

            if (log.isDebugEnabled()) {
                log.debug("determineInsertStatement for %s is %s", object.getClass(), insertStatement);
            }

            // Do not put this insert statement into the map if any columns have defaults
            // because the insert statement will vary by different instances of the data object.
            if (saveInMap) {
                insertStatementsMap.put(object.getClass(), insertStatement);
            } else {
                insertStatementsMap.remove(object.getClass()); // remove just in case
            }

            return insertStatement;

        } catch (Exception e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    String getDeleteStatement(Object object, Connection connection) {
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
            log.debug("determineDeleteStatement for %s is %s", object.getClass(), deleteStatement);
        }

        deleteStatementsMap.put(object.getClass(), deleteStatement);

        return deleteStatement;
    }

    String getSelectStatement(Object object, Connection connection) {
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
        sb.append("SELECT ");

        String sep = "";

        Map<String, ColumnInfo> columns = getColumns(object.getClass(), connection);
        for (String column : columns.keySet()) {
            ColumnInfo columnInfo = columns.get(column);
            sb.append(sep).append(sd).append(columnInfo.columnName).append(ed);
            sep = ", ";
        }
        sb.append(" FROM ").append(sd).append(tableName).append(ed).append(" WHERE ");

        sep = "";
        for (String column : primaryKeys) {
            sb.append(sep).append(sd).append(column).append(ed).append(" = ?");
            sep = " AND ";
        }

        String selectStatement = sb.toString();

        if (log.isDebugEnabled()) {
            log.debug("determineSelectStatement for %s is %s", object.getClass(), selectStatement);
        }

        selectStatementsMap.put(object.getClass(), selectStatement);

        return selectStatement;
    }

    private String buildUpdateString(Object object, Iterator<String> it, Connection connection) throws PersismException {
        // todo STUPID UPDATE STATEMENT IS IN ALPHABETICAL ORDER FFS

        String tableName = getTableName(object.getClass(), connection);
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        List<String> primaryKeys = getPrimaryKeys(object.getClass(), connection);

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(sd).append(tableName).append(ed).append(" SET ");
        String sep = "";

        Map<String, ColumnInfo> columns = getColumns(object.getClass(), connection);
        while (it.hasNext()) {
            String column = it.next();
            ColumnInfo columnInfo = columns.get(column);
            if (columnInfo.autoIncrement || columnInfo.primary) {
                log.info("buildUpdateString: skipping " + column);
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

        try {
            Persistable<?> original = (Persistable<?>) persistable.readOriginalValue();

            Map<String, PropertyInfo> columns = getTableColumnsPropertyInfo(persistable.getClass(), connection);

            if (original == null) {
                // Could happen in the case of cloning or other operation - so it's never read so it never sets original.
                return columns;
            } else {
                Map<String, PropertyInfo> changedColumns = new HashMap<>(columns.keySet().size());
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
            }

        } catch (Exception e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    <T> Map<String, ColumnInfo> getColumns(Class<T> objectClass, Connection connection) throws PersismException {
        // Realistically at this point this objectClass will always be in the map since it's defined early
        // when we get the table name but I'll double check it for determineColumnInfo anyway.
        if (columnInfoMap.containsKey(objectClass)) {
            return columnInfoMap.get(objectClass);
        }
        return determineColumnInfo(objectClass, getTableName(objectClass), connection);
    }

    <T> Map<String, PropertyInfo> getQueryColumnsPropertyInfo(Class<T> objectClass, ResultSet rs) throws PersismException {
        // should not be mapped since ResultSet could contain different # of columns at different times.
//        if (propertyInfoMap.containsKey(objectClass)) {
//            return propertyInfoMap.get(objectClass);
//        }

        return determinePropertyInfo(objectClass, rs);
    }

    <T> Map<String, PropertyInfo> getTableColumnsPropertyInfo(Class<T> objectClass, Connection connection) throws PersismException {
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }
        return determinePropertyInfo(objectClass, getTableName(objectClass), connection);
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
            determinePropertyInfo(objectClass, tableName, connection);
        }
        return tableName;
    }

    private synchronized <T> String determineTable(Class<T> objectClass) {

        if (tableMap.containsKey(objectClass)) {
            return tableMap.get(objectClass);
        }

        String tableName;
        Table annotation = objectClass.getAnnotation(Table.class);
        if (annotation != null) {
            tableName = annotation.value();
            // double check against stored table names to get the actual case of the name
            boolean found = false;
            for (String name : tableNames) {
                if (name.equalsIgnoreCase(tableName)) {
                    tableName = name;
                    found = true;
                }
            }
            if (!found) {
                throw new PersismException("Could not find a Table in the database named " + tableName + ". Check the @Table annotation on " + objectClass.getName());
            }

        } else {
            tableName = guessTableName(objectClass);
        }
        tableMap.put(objectClass, tableName);
        return tableName;
    }

    // Returns the table name found in the DB in the same case as in the DB.
    // throws PersismException if we cannot guess any table name for this class.
    private <T> String guessTableName(Class<T> objectClass) throws PersismException {
        Set<String> guesses = new LinkedHashSet<>(6); // guess order is important
        List<String> guessedTables = new ArrayList<String>(6);

        String className = objectClass.getSimpleName();

        addTableGuesses(className, guesses);
        for (String tableName : tableNames) {
            for (String guess : guesses) {
                if (guess.equalsIgnoreCase(tableName)) {
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

        if (className.endsWith("y")) {
            pluralClassName = className.substring(0, className.length() - 1) + "ies";
        } else {
            pluralClassName = className + "s";
        }

        guesses.add(className);
        guesses.add(pluralClassName);

        guess = camelToTitleCase(className);
        guesses.add(guess); // name with spaces
        guesses.add(replaceAll(guess, ' ', '_')); // name with spaces changed to _

        guess = camelToTitleCase(pluralClassName);
        guesses.add(guess); // plural name with spaces
        guesses.add(replaceAll(guess, ' ', '_')); // plural name with spaces changed to _
    }

    List<String> getPrimaryKeys(Class<?> objectClass, Connection connection) throws PersismException {

        // ensures meta data will be available
        String tableName = getTableName(objectClass, connection);

        List<String> primaryKeys = new ArrayList<>(4);
        Map<String, ColumnInfo> map = getColumns(objectClass, connection);
        for (ColumnInfo col : map.values()) {
            if (col.primary) {
                primaryKeys.add(col.columnName);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("getPrimaryKeys for %s %s", tableName, primaryKeys);
        }
        return primaryKeys;
    }

    ConnectionTypes getConnectionType() {
        return connectionType;
    }

}
