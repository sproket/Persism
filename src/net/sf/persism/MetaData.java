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

import static net.sf.persism.Conversions.*;
import static net.sf.persism.Util.*;

/**
 * DB and POJO related Metadata collected based connection url
 *
 * @author Dan Howard
 * @since 3/31/12 4:19 PM
 */
final class MetaData {

    private static final Log log = Log.getLogger(MetaData.class);

    // properties for each class - static because this won't need to change between MetaData instances
    private static final Map<Class<?>, Collection<PropertyInfo>> propertyMap = new ConcurrentHashMap<>(32);

    // column to property map for each class
    private final Map<Class<?>, Map<String, PropertyInfo>> propertyInfoMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, Map<String, ColumnInfo>> columnInfoMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, List<String>> propertyNames = new ConcurrentHashMap<>(32); // not static since this is by column order which may vary

    // table/view name for each class
    private final Map<Class<?>, String> tableOrViewMap = new ConcurrentHashMap<>(32);

    // SQL for updates/inserts/deletes/selects for each class
    private final Map<Class<?>, String> updateStatementsMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, String> insertStatementsMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, String> deleteStatementsMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, String> selectStatementsMap = new ConcurrentHashMap<>(32);
    private final Map<Class<?>, String> whereClauseMap = new ConcurrentHashMap<>(32);

    // Key is SQL with named params, Value is SQL with ?
    // private Map<String, String> sqlWitNamedParams = new ConcurrentHashMap<String, String>(32);

    // Key is SQL with named params, Value list of named params
    // private Map<String, List<String>> namedParams = new ConcurrentHashMap<String, List<String>>(32);

    // private Map<Class, List<String>> primaryKeysMap = new ConcurrentHashMap<Class, List<String>>(32); // remove later maybe?

    // list of tables in the DB
    private final Set<String> tableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, TableInfo> tableInfos = new HashMap<>();

    // list of views in the DB
    private final Set<String> viewNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    // Map of table names + meta data
    // private Map<String, TableInfo> tableInfoMap = new ConcurrentHashMap<String, TableInfo>(32);

    private static final Map<String, MetaData> metaData = new ConcurrentHashMap<String, MetaData>(4);

    private final ConnectionTypes connectionType;

    // the "extra" characters that can be used in unquoted identifier names (those beyond a-z, A-Z, 0-9 and _)
    // Was using DatabaseMetaData getExtraNameCharacters() but some drivers don't provide these and still allow
    // for non alpha-numeric characters in column names. We'll just use a static set.
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
    private synchronized <T> Map<String, PropertyInfo> determinePropertyInfo(Class<T> objectClass, String tableName, Connection connection) {
        // double check map
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();
        String schema = tableInfos.get(tableName).schema();

        ResultSet rs = null;
        Statement st = null;
        try {
            st = connection.createStatement();
            // gives us real column names with case.
            String sql;
            if (isEmpty(schema)) {
                sql = MessageFormat.format(SELECT_FOR_COLUMNS, sd, tableName, ed);
            } else {
                sql = MessageFormat.format(SELECT_FOR_COLUMNS_WITH_SCHEMA, sd, schema, ed, sd, tableName, ed);
            }
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
        // double check map - note this could be called with a Query where we never have that in here
        if (propertyInfoMap.containsKey(objectClass)) {
            return propertyInfoMap.get(objectClass);
        }

        List<String> propertyNames = new ArrayList<>(32);
        try {
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
                    String checkName = propertyInfo.propertyName().toLowerCase().replace("_", "");
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
                    propertyNames.add(foundProperty.propertyName());
                } else {
                    log.warn(Messages.NoPropertyFoundForColumn.message(realColumnName, objectClass));
                }
            }

            // Do not put query classes into the metadata. It's possible the 1st run has a query with missing columns
            // any calls afterward would fail because I never would refresh the columns again. Table is fine since we
            // can do a SELECT * to get all columns up front but we can't do that with a query.
            //if (objectClass.getAnnotation(NotTable.class) == null) {

            // If we have properties > columns then we will later have an uninitialized object which is an error
            // so in that case, we won't cache this. The assumption is that in the case of a long-running app which
            // may catch and continue, we would always have a bad cache.
            //if (properties.size() <= columnCount) {
            if (objectClass.getAnnotation(NotTable.class) == null) {
                propertyInfoMap.put(objectClass, columns);
            }
            this.propertyNames.put(objectClass, propertyNames);

            return columns;

        } catch (SQLException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    // todo check NULL before calling
    private void determineConverter(Convertable propOrColumnInfo, Types sourceType, Types targetType) {
        if (true) {
            return;
        }
        log.debug("determineConverter " + connectionType + " FOR " + propOrColumnInfo);

        switch (sourceType) {
            case booleanType:
            case BooleanType:
                if (targetType == Types.BooleanType || targetType == Types.booleanType) {
                    break;
                }

            case byteType:
            case ByteType:
                break;

            case shortType:
            case ShortType:
                break;

            case integerType:
            case IntegerType:

                if (targetType == Types.IntegerType || targetType == Types.integerType) {
                    break;
                }

                // int to bool
                if (targetType == Types.BooleanType || targetType == Types.booleanType) {
                    propOrColumnInfo.setConverter(IntToBoolean(), "intToBool");
                    break;
                }

                if (targetType == Types.TimeType) {
                    // SQLite when a Time is defined VIA a convert from LocalTime via Time.valueOf (see getContactForTest)
                    propOrColumnInfo.setConverter(IntToTime(), "intToTime");
                    break;
                }

                if (targetType == Types.LocalTimeType) {
                    // SQLite for Time SQLite sees Long, for LocalTime it sees Integer
                    propOrColumnInfo.setConverter(IntToLocalTime(), "intToLocalTime");
                    break;
                }

                if (targetType == Types.ShortType || targetType == Types.shortType) {
                    // todo where/when to warn
//                    log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "SHORT", "INT"));
                    propOrColumnInfo.setConverter(IntToShort(), "intToShort");
                    break;

                }

                if (targetType == Types.ByteType || targetType == Types.byteType) {
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "BYTE", "INT"));
                    propOrColumnInfo.setConverter(IntToByte(), "intToByte");
                    break;
                }
                break;

            case longType:
            case LongType:

                if (targetType == Types.LongType || targetType == Types.longType) {
                    break;
                }

                if (targetType == Types.SQLDateType) {
                    propOrColumnInfo.setConverter(LongToSqlDate(), "longToSqlDate");
                    break;

                }

                if (targetType == Types.UtilDateType) {
                    propOrColumnInfo.setConverter(LongToUtilDate(), "longToUtilDate");
                    break;

                }

                if (targetType == Types.TimestampType) {
                    propOrColumnInfo.setConverter(LongToTimestamp(), "longToTimestamp");
                    break;
                }

                if (targetType == Types.IntegerType || targetType == Types.integerType) {
//                    log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "INT", "LONG"));
                    propOrColumnInfo.setConverter(LongToInt(), "longToInt");
                    break;

                }

                if (targetType == Types.LocalDateType) {
                    // SQLite reads long as date.....
                    propOrColumnInfo.setConverter(LongToLocalDate(), "longToLocalDate");
                    break;
                }

                if (targetType == Types.LocalDateTimeType) {
                    propOrColumnInfo.setConverter(LongToLocalDateTime(), "longToLocalDateTime");
                    break;
                }

                if (targetType == Types.TimeType) {
                    // SQLite.... Again.....
                    propOrColumnInfo.setConverter(LongToTime(), "longToTime");
                    break;
                }
                break;

            case floatType:
            case FloatType:
                break;

            case doubleType:
            case DoubleType:
                if (targetType == Types.DoubleType || targetType == Types.doubleType) {
                    break;
                }

                // float or doubles to BigDecimal
                if (targetType == Types.BigDecimalType) {
                    propOrColumnInfo.setConverter(DoubleToBigDecimal(), "doubleToBigDecimal");
                    break;
                }

                if (targetType == Types.FloatType || targetType == Types.floatType) {
//                    log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "FLOAT", "DOUBLE"));
                    propOrColumnInfo.setConverter(DoubleToFloat(), "doubleToFloat");
                    break;
                }

                if (targetType == Types.IntegerType || targetType == Types.integerType) {
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "INT", "DOUBLE"));
                    propOrColumnInfo.setConverter(DoubleToInt(), "doubleToInt");
                    break;
                }

                if (targetType == Types.LongType || targetType == Types.longType) {
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "LONG", "DOUBLE"));
                    propOrColumnInfo.setConverter(DoubleToLong(), "doubleToLong");
                    break;
                }
                break;

            case BigDecimalType:

                if (targetType == Types.BigDecimalType) {
                    break;
                }

                if (targetType == Types.FloatType || targetType == Types.floatType) {
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "FLOAT", "BigDecimal"));
                    propOrColumnInfo.setConverter(BigDecimalToFloat(), "bigDecimalToFloat");
                    break;
                }

                if (targetType == Types.DoubleType || targetType == Types.doubleType) {
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "DOUBLE", "BigDecimal"));
                    propOrColumnInfo.setConverter(BigDecimalToDouble(), "bigDecimalToDouble");
                    break;

                } else if (targetType == Types.LongType || targetType == Types.longType) {
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "LONG", "BigDecimal"));
                    propOrColumnInfo.setConverter(BigDecimalToLong(), "bigDecimalToLong");
                    break;

                } else if (targetType == Types.IntegerType || targetType == Types.integerType) {
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "INT", "BigDecimal"));
                    propOrColumnInfo.setConverter(BigDecimalToInt(), "bigDecimalToInt");
                    break;
                }

                if (targetType == Types.ShortType || targetType == Types.shortType) {
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "SHORT", "BigDecimal"));
                    propOrColumnInfo.setConverter(BigDecimalToShort(), "bigDecimalToShort");
                    break;
                }

                if (targetType == Types.BooleanType || targetType == Types.booleanType) {
                    // BigDecimal to Boolean. Oracle (sigh) - Additional for a Char to Boolean as then (see TestOracle for links)
                    // log.warnNoDuplicates(Messages.PossibleOverflow.message(col, "BOOLEAN", "BigDecimal"));
                    propOrColumnInfo.setConverter(BigDecimalToBool(), "bigDecimalToBool");
                    break;
                }

                if (targetType == Types.StringType) {
                    propOrColumnInfo.setConverter(BigDecimalToString(), "bigDecimalToString");
                    break;
                }
                break;

            case StringType:
                if (targetType == Types.StringType) {
                    break;
                }

                if (targetType == Types.UtilDateType) {
                    propOrColumnInfo.setConverter(StringToUtilDate(), "stringToUtilDate");
                    break;
                }

                if (targetType == Types.SQLDateType) {
                    propOrColumnInfo.setConverter(StringToSqlDate(), "stringToSqlDate");
                    break;
                }

                if (targetType == Types.TimestampType) {
                    propOrColumnInfo.setConverter(StringToTimestamp(), "stringToTimestamp");
                    break;
                }

                if (targetType.getJavaType().isEnum()) {
                    // If this is an enum do a case-insensitive comparison
                    // todo BiFunction != Function so add maybe a enumConverter?
                    //propertyInfo.converter = conversions.stringToEnum;
                    propOrColumnInfo.setConverter(StringToEnum());
                    break;
                }

                if (targetType == Types.UUIDType) {
                    propOrColumnInfo.setConverter(StringToUUID(), "stringToUUID");
                    break;
                }

                if (targetType == Types.BooleanType || targetType == Types.booleanType) {
                    propOrColumnInfo.setConverter(StringToBoolean(), "stringToBoolean");
                    break;
                }

                if (targetType == Types.BigDecimalType) {
                    propOrColumnInfo.setConverter(StringToBigDecimal(), "stringToBigDecimal");
                    break;
                }

                break;

            case characterType:
            case CharacterType:
                break;

            case LocalDateType:
                propOrColumnInfo.setConverter(LocalDateToSqlDate(), "localDateToSqlDate");
                break;

            case LocalDateTimeType:
                propOrColumnInfo.setConverter(LocalDateToTimestamp(), "localDateToTimestamp");
                break;

            case LocalTimeType:
                propOrColumnInfo.setConverter(LocalTimeToTime(), "localTimeToTime");
                break;

            case UtilDateType:
                if (targetType == Types.UtilDateType) {
                    break;
                }

                if (targetType == Types.SQLDateType) {
                    propOrColumnInfo.setConverter(UtilDateToSqlDate(), "utilDateToSqlDate");
                    break;
                }

                if (targetType == Types.TimestampType) {
                    propOrColumnInfo.setConverter(UtilDateToTimestamp(), "utilDateToTimestamp");
                    break;
                }

                if (targetType == Types.LocalDateType) {
                    propOrColumnInfo.setConverter(UtilDateToLocalDate(), "utilDateToLocalDate");
                    break;
                }

                if (targetType == Types.LocalDateTimeType) {
                    propOrColumnInfo.setConverter(UtilDateToLocalDateTime(), "utilDateToLocalDateTime");
                    break;
                }

                if (targetType == Types.TimeType) {
                    propOrColumnInfo.setConverter(UtilDateToTime(), "utilDateToTime");
                    break;
                }

                if (targetType == Types.LocalTimeType) {
                    propOrColumnInfo.setConverter(UtilDateToLocalTime(), "utilDateToLocalTime");
                    break;
                }
                break;

            case SQLDateType:
                if (targetType == Types.SQLDateType) {
                    break;
                }

                if (targetType == Types.UtilDateType) {
                    propOrColumnInfo.setConverter(SqlDateToUtilDate(), "sqlDateToUtilDate");
                    break;
                }

                if (targetType == Types.TimestampType) {
                    propOrColumnInfo.setConverter(SqlDateToTimestamp(), "sqlDateToTimestamp");
                    break;
                }

                if (targetType == Types.LocalDateType) {
                    propOrColumnInfo.setConverter(SqlDateToLocalDate(), "sqlDateToLocalDate");
                    break;
                }

                if (targetType == Types.LocalDateTimeType) {
                    propOrColumnInfo.setConverter(SqlDateToLocalDateTime(), "sqlDateToLocalDateTime");
                    break;
                }

                if (targetType == Types.TimeType) {
                    propOrColumnInfo.setConverter(SqlDateToTime(), "sqlDateToTime");
                    break;
                }

                if (targetType == Types.LocalTimeType) {
                    propOrColumnInfo.setConverter(SqlDateToLocalTime(), "sqlDateToLocalTime");
                    break;
                }
                break;

            case TimestampType:
                if (targetType == Types.TimestampType) {
                    break;
                }

                if (targetType == Types.UtilDateType) {
                    propOrColumnInfo.setConverter(TimestampToUtilDate(), "timestampToUtilDate");
                    break;
                }

                if (targetType == Types.SQLDateType) {
                    propOrColumnInfo.setConverter(TimestampToSqlDate(), "timestampToSqlDate");
                    break;
                }

                if (targetType == Types.LocalDateType) {
                    propOrColumnInfo.setConverter(TimestampToLocalDate(), "timestampToLocalDate");
                    break;
                }

                if (targetType == Types.LocalDateTimeType) {
                    propOrColumnInfo.setConverter(TimestampToLocalDateTime(), "timestampToLocalDateTime");
                    break;
                }

                if (targetType == Types.TimeType) {
                    propOrColumnInfo.setConverter(TimestampToTime(), "timestampToTime");
                    break;
                }

                if (targetType == Types.LocalTimeType) {
                    propOrColumnInfo.setConverter(TimestampToLocalTime(), "timestampToLocalTime");
                    break;
                }
                break;

            case TimeType:
                if (targetType == Types.TimeType) {
                    break;
                }
                if (targetType == Types.LocalTimeType) {
                    propOrColumnInfo.setConverter(TimeToLocalTime(), "timeToLocalTime");
                    break;
                }
                break;

            case InstantType:
            case OffsetDateTimeType:
            case ZonedDateTimeType:
                log.warn(Messages.ConverterValueTypeNotYetSupported.message(sourceType.getJavaType()), new Throwable());
                break;

            case byteArrayType:
            case ByteArrayType:
                if (targetType == Types.ByteArrayType) {
                    break;
                }

                if (targetType == Types.UUIDType) {
                    propOrColumnInfo.setConverter(ByteArrayToUUID(), "byteArrayToUUID");
                    break;
                }
                break;

            case ClobType:
            case BlobType:
                // todo we don't know which direction. If this is a column it's OK but what conversion?
                log.warn(Messages.ConverterDoNotUseClobOrBlobAsAPropertyType.message(), new Throwable(""));
                break;

            case EnumType:
                // No need to convert it here.
                // If it's being used for the property setter then it's OK
                // If it's being used by setParameters it's converted to String
                // The String case above converts from the String to the Enum
                log.debug("EnumType");
                break;

            case UUIDType:
                if (targetType == Types.UUIDType) {
                    break;
                }
                // todo how can blob work here?
                if (targetType == Types.BlobType || targetType == Types.byteArrayType || targetType == Types.ByteArrayType) {
                    propOrColumnInfo.setConverter(UUIDtoByteArray(), "UUIDtoByteArray");
                }
                break;

            case ObjectType:
                break;
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
        String schema = tableInfos.get(tableName).schema();

        try {

            st = connection.createStatement();
            String sql;
            if (isEmpty(schema)) {
                sql = MessageFormat.format(SELECT_FOR_COLUMNS, sd, tableName, ed);
            } else {
                sql = MessageFormat.format(SELECT_FOR_COLUMNS_WITH_SCHEMA, sd, schema, ed, sd, tableName, ed);
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

                if (isEmpty(schema)) {
                    rs = dmd.getPrimaryKeys(null, connectionType.getSchemaPattern(), tableName);
                } else {
                    rs = dmd.getPrimaryKeys(null, schema, tableName);
                }

                // Iterate primary keys and update column infos
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
                    log.warn(Messages.DatabaseMetaDataCouldNotFindPrimaryKeys.message(tableName));
                }
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
                log.warn(Messages.DatabaseMetaDataCouldNotFindColumns.message(tableName));
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
                log.warn(Messages.NoPrimaryKeyFoundForTable.message(tableName));
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

    private static final String[] tableTypes = {"TABLE"};
    private static final String[] viewTypes = {"VIEW"};

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
                String name = rs.getString("TABLE_NAME");
                tableNames.add(name);
                tableInfos.put(name, new TableInfo(name, rs.getString("TABLE_SCHEM")));
            }

            rs = con.getMetaData().getTables(null, connectionType.getSchemaPattern(), null, viewTypes);
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                viewNames.add(name);
                tableInfos.put(name, new TableInfo(name, rs.getString("TABLE_SCHEM"))); // why do we seperate 2 lists?
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

        if (object instanceof Persistable<?> pojo) {
            Map<String, PropertyInfo> changes = getChangedProperties(pojo, connection);
            if (changes.size() == 0) {
                throw new NoChangesDetectedForUpdateException();
            }
            // Note we don't add Persistable updates to updateStatementsMap since they will be different each time.
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
            String schema = tableInfos.get(tableName).schema();

            String sd = connectionType.getKeywordStartDelimiter();
            String ed = connectionType.getKeywordEndDelimiter();

            Map<String, ColumnInfo> columns = getColumns(object.getClass(), connection);
            Map<String, PropertyInfo> properties = getTableColumnsPropertyInfo(object.getClass(), connection);

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ");
            if (isNotEmpty(schema)) {
                sb.append(sd).append(schema).append(ed).append(".");
            }
            sb.append(sd).append(tableName).append(ed).append(" (");

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
                        if (properties.get(column.columnName).getValue(object) == null) {
                            continue;
                        }

                    }

                    sb.append(sep).append(sd).append(column.columnName).append(ed);
                    sbp.append(sep).append("?");
                    sep = ", ";
                }
            }

            sb.append(sbp).append(") ");

            String insertStatement;
            insertStatement = sb.toString();

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
        String schema = tableInfos.get(tableName).schema();

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        List<String> primaryKeys = getPrimaryKeys(object.getClass(), connection);

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        if (isNotEmpty(schema)) {
            sb.append(sd).append(schema).append(ed).append(".");
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

    String getWhereClause(Class<?> objectClass, Connection connection) {
        if (whereClauseMap.containsKey(objectClass)) {
            return whereClauseMap.get(objectClass);
        }
        return determineWhereClause(objectClass, connection);
    }

    private synchronized String determineWhereClause(Class<?> objectClass, Connection connection) {
        if (whereClauseMap.containsKey(objectClass)) {
            return whereClauseMap.get(objectClass);
        }

        String sep = "";

        StringBuilder sb = new StringBuilder();
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        List<String> primaryKeys = getPrimaryKeys(objectClass, connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException(Messages.TableHasNoPrimaryKeysForWhere.message(objectClass.getName()));
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
        whereClauseMap.put(objectClass, where);
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

        String tableName = getTableName(objectClass, connection);
        String schema = tableInfos.get(tableName).schema();

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
        if (isNotEmpty(schema)) {
            sb.append(sd).append(schema).append(ed).append('.');
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

        // todo maybe we should exclude columns where there is no setter? Unless it's a record?
        String tableName = getTableName(object.getClass(), connection);
        String schema = tableInfos.get(tableName).schema();

        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        List<String> primaryKeys = getPrimaryKeys(object.getClass(), connection);

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        if (isNotEmpty(schema)) {
            sb.append(sd).append(schema).append(ed).append(".");
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

        try {
            Persistable<?> original = (Persistable<?>) persistable.readOriginalValue();

            Map<String, PropertyInfo> columns = getTableColumnsPropertyInfo(persistable.getClass(), connection);

            if (original == null) {
                // Could happen in the case of cloning or other operation - so it's never read, so it never sets original.
                return columns;
            } else {
                Map<String, PropertyInfo> changedColumns = new HashMap<>(columns.keySet().size());
                for (String column : columns.keySet()) {

                    PropertyInfo propertyInfo = columns.get(column);

                    Object newValue = null;
                    Object orgValue = null;
                    newValue = propertyInfo.getValue(persistable);
                    orgValue = propertyInfo.getValue(original);

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
        // should not be mapped since ResultSet could contain different # of columns at different times. OK NOW. If properties > columns we won't cache it
        // nope breaks records tests
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
        if (tableOrViewMap.containsKey(objectClass)) {
            return tableOrViewMap.get(objectClass);
        }
        return determineTable(objectClass);
    }

    <T> String getFullTableName(Class<T> objectClass) {
        // todo return schema name as well..
        return connectionType.getKeywordStartDelimiter() + getTableName(objectClass) + connectionType.getKeywordEndDelimiter();
    }

    <T> List<String> getPropertyNames(Class<T> objectClass) {
        return propertyNames.get(objectClass);
    }

    // internal version to retrieve meta information about this table's columns
    // at the same time we find the table name itself.
    // TODO This is in a synchronized context - is it aways?
    private <T> String getTableName(Class<T> objectClass, Connection connection) {

        String tableName = getTableName(objectClass);

        if (!columnInfoMap.containsKey(objectClass)) {
            determineColumnInfo(objectClass, tableName, connection);
        }

        if (!propertyInfoMap.containsKey(objectClass)) {
            determinePropertyInfo(objectClass, tableName, connection);
        }

        // todo this needs to happen only once. Add to cache? synchronized?
        // assign convertors
        Map<String, ColumnInfo> columns = getColumns(objectClass, connection);
        Map<String, PropertyInfo> properties = getTableColumnsPropertyInfo(objectClass, connection);

        for (String col : columns.keySet()) {
            ColumnInfo columnInfo = columns.get(col);
            PropertyInfo propertyInfo = properties.get(col);

            Types propertyType = Types.getType(propertyInfo.getter.getReturnType());

            if (propertyType != null) {
                determineConverter(columnInfo, columnInfo.columnType, propertyType);
                determineConverter(propertyInfo, propertyType, columnInfo.columnType);
            }
        }
        return tableName;
    }

    private synchronized <T> String determineTable(Class<T> objectClass) {

        if (tableOrViewMap.containsKey(objectClass)) {
            return tableOrViewMap.get(objectClass);
        }

        String tableName;
        Table tableAnnotation = objectClass.getAnnotation(Table.class);
        View viewAnnotation = objectClass.getAnnotation(View.class);

        if (tableAnnotation != null) {
            tableName = tableAnnotation.value();
            // double check against stored table names to get the actual case of the name
            boolean found = false;
            for (String name : tableNames) {
                if (name.equalsIgnoreCase(tableName)) {
                    tableName = name;
                    found = true;
                }
            }
            if (!found) {
                throw new PersismException(Messages.CouldNotFindTableNameInTheDatabase.message(tableName, objectClass.getName()));
            }
        } else if (viewAnnotation != null && isNotEmpty(viewAnnotation.value())) {

            tableName = viewAnnotation.value();

            // double check against stored view names to get the actual case of the name
            boolean found = false;
            for (String name : viewNames) {
                if (name.equalsIgnoreCase(tableName)) {
                    tableName = name;
                    found = true;
                }
            }
            if (!found) {
                throw new PersismException(Messages.CouldNotFindViewNameInTheDatabase.message(tableName, objectClass.getName()));
            }
        } else {
            tableName = guessTableOrViewName(objectClass);
        }
        tableOrViewMap.put(objectClass, tableName);
        return tableName;
    }

    // Returns the table/view name found in the DB in the same case as in the DB.
    // throws PersismException if we cannot guess any table/view name for this class.
    private <T> String guessTableOrViewName(Class<T> objectClass) throws PersismException {
        Set<String> guesses = new LinkedHashSet<>(6); // guess order is important
        List<String> guessedTables = new ArrayList<>(6);

        String className = objectClass.getSimpleName();

        Set<String> list;
        boolean isView = false;
        if (objectClass.getAnnotation(View.class) != null) {
            list = viewNames;
            isView = true;
        } else {
            list = tableNames;
        }

        addTableGuesses(className, guesses);
        for (String tableName : list) {
            for (String guess : guesses) {
                if (guess.equalsIgnoreCase(tableName)) {
                    guessedTables.add(tableName);
                }
            }
        }
        if (guessedTables.size() == 0) {
            throw new PersismException(Messages.CouldNotDetermineTableOrViewForType.message(isView ? "view" : "table", objectClass.getName(), guesses));
        }

        if (guessedTables.size() > 1) {
            throw new PersismException(Messages.CouldNotDetermineTableOrViewForTypeMultipleMatches.message(isView ? "view" : "table", objectClass.getName(), guesses, guessedTables));
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
