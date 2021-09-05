package net.sf.persism;

import net.sf.persism.annotations.NotTable;
import net.sf.persism.annotations.View;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.regex.MatchResult;

// Non-public code Session uses.
abstract class SessionInternal {

    // leave this using the Session.class for logging
    private static final Log log = Log.getLogger(Session.class);

    Connection connection;
    MetaData metaData;
    Reader reader;
    Converter converter;

    final JDBCResult executeQuery(Class<?> objectClass, SQL sql, Parameters parameters) throws SQLException {

        boolean isPOJO = Types.getType(objectClass) == null;

        JDBCResult result = new JDBCResult();
        String sqlQuery = sql.toString();
        sql.storedProc = !sql.whereOnly && !sqlQuery.trim().toLowerCase().startsWith("select ");

        if (parameters.areNamed) {
            if (sql.storedProc) {
                log.warn(Messages.NamedParametersUsedWithStoredProc.message());
            }

            char delim = '@';
            Map<String, List<Integer>> paramMap = new HashMap<>();
            sqlQuery = parseParameters(delim, sql.toString(), paramMap);
            parameters.setParameterMap(paramMap);
        }

        if (parameters.areKeys) {
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
            sqlQuery = parsePropertyNames(select + " " + sqlQuery, objectClass, connection);

        } else {

            checkIfStoredProcOrSQL(objectClass, sql);

            if (isPOJO) {
                // Only for pojos or records - not for built-in types.
                sqlQuery = parsePropertyNames(sqlQuery, objectClass, connection);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("executeQuery: %s params: %s", sqlQuery, parameters);
        }

        exec(result, sqlQuery, parameters.toArray());

        return result;
    }

    final void exec(JDBCResult result, String sql, Object... parameters) throws SQLException {
        try {
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

        } catch (SQLException e) {
            throw new SQLException(e.getMessage() + " -> " + sql, e);
        }

    }

    // For unit tests only for now.
    final boolean execute(String sql, Object... parameters) {

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

    final String parseParametersx(String query, Map<String, List<Integer>> paramMap) {
        // todo placeholder to try parsing a different way
        Scanner scanner = new Scanner(query);
        List<MatchResult> matchResults = scanner.findAll(":").toList();
        return "";
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
    final String parseParameters(char delim, String query, Map<String, List<Integer>> paramMap) {
        // I was originally using regular expressions, but they didn't work well
        // for ignoring parameter-like strings inside quotes.

        // originally used : - which conflicts with property names so I'll use @
        // todo verify about inquote vars - we have different delimiters based on the different dbs
        // todo add unit tests
        log.info("parseParameters using " + delim);

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
                } else if (c == delim && i + 1 < length &&
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

        return parsedQuery.toString();
    }

    final String parsePropertyNames(String sql, Class<?> objectClass, Connection connection) {
        sql += " "; // add a space for if a property name is the last part of the string otherwise parser skips it

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


    final void checkIfOkForWriteOperation(Object object, String operation) {
        Class<?> objectClass = object.getClass();
        if (objectClass.getAnnotation(View.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForView.message(objectClass, operation));
        }
        if (objectClass.getAnnotation(NotTable.class) != null) {
            throw new PersismException(Messages.OperationNotSupportedForNotTableQuery.message(objectClass, operation));
        }
        if (Types.getType(objectClass) != null) {
            throw new PersismException(Messages.OperationNotSupportedForJavaType.message(objectClass, operation));
        }
    }

    final Object getTypedValueReturnedFromGeneratedKeys(Class<?> objectClass, ResultSet rs) throws SQLException {

        Object value = null;
        Types type = Types.getType(objectClass);

        if (type == null) {
            log.warn(Messages.UnknownTypeForPrimaryGeneratedKey.message(objectClass));
            return rs.getObject(1);
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
        return value;
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
                        // todo ZonedDateTime, OffsetDateTimeType and MAYBE Instant NAH
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
                            // PostgreSQL does work with setObject but not setString unless you set the connection property stringtype=unspecified todo document this
                            st.setObject(n, param);
                        } else {
                            // TODO mysql seems to set the byte array this way? But it won't match!
                            st.setString(n, param.toString());
                        }
                        break;

                    default:
                        // Usually SQLite with util.date - setObject works
                        // Also if it's a custom non-standard type.
                        // todo add to Messages
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

    private <T> void checkIfStoredProcOrSQL(Class<T> objectClass, SQL sql) {
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
}
