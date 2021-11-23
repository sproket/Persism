package net.sf.persism;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.NotTable;
import net.sf.persism.annotations.View;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.where;

// Non-public code Session uses.
final class SessionHelper {

    // leave this using the Session.class for logging
    private static final Log log = Log.getLogger(Session.class);
    private static final Log blog = Log.getLogger("net.sf.persism.Benchmarks");

    private final Session session;

    public SessionHelper(Session session) {
        this.session = session;
    }

    JDBCResult executeQuery(Class<?> objectClass, SQL sql, Parameters parameters) throws SQLException {

        JDBCResult result = new JDBCResult();
        String sqlQuery = sql.sql;
        sql.storedProc = !sql.whereOnly && isStoredProc(sqlQuery);

        if (parameters.areNamed) {
            if (sql.storedProc) {
                log.warn(Messages.NamedParametersUsedWithStoredProc.message());
            }

            char delim = '@';
            Map<String, List<Integer>> paramMap = new HashMap<>();
            sqlQuery = parseParameters(delim, sqlQuery, paramMap);
            parameters.setParameterMap(paramMap);

        } else if (parameters.areKeys) {
            // convert parameters - usually it's the UUID type that may need a conversion to byte[16]
            // Probably we don't want to auto-convert here since it's inconsistent. DO WE? YES.
            List<String> keys = session.metaData.getPrimaryKeys(objectClass, session.connection);
            if (keys.size() == 1) {
                Map<String, ColumnInfo> columns = session.metaData.getColumns(objectClass, session.connection);
                String key = keys.get(0);
                ColumnInfo columnInfo = columns.get(key);
                for (int j = 0; j < parameters.size(); j++) {
                    if (parameters.get(j) != null) {
                        parameters.set(j, session.converter.convert(parameters.get(j), columnInfo.columnType.getJavaType(), columnInfo.columnName));
                    }
                }
            }
        }

        if (sql.whereOnly) {
            if (objectClass.getAnnotation(NotTable.class) != null) {
                throw new PersismException(Messages.WhereNotSupportedForNotTableQueries.message());
            }
            String select = session.metaData.getSelectStatement(objectClass, session.connection);
            sqlQuery = select + " " + parsePropertyNames(sqlQuery, objectClass, session.connection);
            sql.processedSQL = sqlQuery;
        } else {
            checkIfStoredProcOrSQL(objectClass, sql);
        }

        exec(result, sqlQuery, parameters.toArray());
        return result;
    }

    void exec(JDBCResult result, String sql, Object... parameters) throws SQLException {
        long now = System.currentTimeMillis();

        try {
            if (isSelect(sql)) {
                if (session.metaData.getConnectionType() == ConnectionTypes.Firebird) {
                    // https://stackoverflow.com/questions/935511/how-can-i-avoid-resultset-is-closed-exception-in-java
                    result.st = session.connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                } else {
                    result.st = session.connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                }

                PreparedStatement pst = (PreparedStatement) result.st;
                setParameters(pst, parameters);
                result.rs = pst.executeQuery();
            } else {
                if (!sql.trim().toLowerCase().startsWith("{call")) {
                    sql = "{call " + sql + "} ";
                }
                // Don't need If Firebird here. Firebird would call a selectable stored proc with SELECT anyway
                result.st = session.connection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

                CallableStatement cst = (CallableStatement) result.st;
                setParameters(cst, parameters);
                result.rs = cst.executeQuery();
            }

        } catch (SQLException e) {
            throw new SQLException(e.getMessage() + " SQL: " + sql + " params: " + Arrays.asList(parameters), e);
        } finally {
            if (blog.isDebugEnabled()) {
                blog.debug("exec Time: " + (System.currentTimeMillis() - now) + " " + sql + " params: " + Arrays.asList(parameters));
            }
        }
    }

    // For unit tests only for now.
    boolean execute(String sql, Object... parameters) {

        log.debug("execute: %s params: %s", sql, Arrays.asList(parameters));

        Statement st = null;
        try {

            if (parameters.length == 0) {
                st = session.connection.createStatement();
                return st.execute(sql);
            } else {
                st = session.connection.prepareStatement(sql);
                PreparedStatement pst = (PreparedStatement) st;
                setParameters(pst, parameters);
                return pst.execute();
            }

        } catch (Exception e) {
            Util.rollback(session.connection);
            throw new PersismException(e.getMessage(), e);
        } finally {
            Util.cleanup(st, null);
        }
    }

    /**
     * Original from Adam Crume, JavaWorld.com, 04/03/07
     * https://www.infoworld.com/article/2077706/named-parameters-for-preparedstatement.html
     *
     * @param sql      query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    String parseParameters(char delim, String sql, Map<String, List<Integer>> paramMap) {
        log.debug("parseParameters using " + delim);

        int length = sql.length();
        StringBuilder parsedQuery = new StringBuilder(length);

        Set<Character> startDelims = new HashSet<>(4);
        startDelims.add('"');
        startDelims.add('\'');

        if (Util.isNotEmpty(session.metaData.getConnectionType().getKeywordStartDelimiter())) {
            startDelims.add(session.metaData.getConnectionType().getKeywordStartDelimiter().charAt(0));
        }

        Set<Character> endDelims = new HashSet<>(4);
        endDelims.add('"');
        endDelims.add('\'');

        if (Util.isNotEmpty(session.metaData.getConnectionType().getKeywordEndDelimiter())) {
            endDelims.add(session.metaData.getConnectionType().getKeywordEndDelimiter().charAt(0));
        }

        boolean inDelimiter = false;
        int index = 1;

        for (int i = 0; i < length; i++) {
            char c = sql.charAt(i);
            if (inDelimiter) {
                if (endDelims.contains(c)) {
                    inDelimiter = false;
                }
            } else if (startDelims.contains(c)) {
                inDelimiter = true;
            } else if (c == delim && i + 1 < length && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                int j = i + 2;
                while (j < length && Character.isJavaIdentifierPart(sql.charAt(j))) {
                    j++;
                }
                String name = sql.substring(i + 1, j);
                c = '?'; // replace the parameter with a question mark
                i += name.length(); // skip past the end of the parameter

                List<Integer> indexList = paramMap.get(name);
                if (indexList == null) {
                    indexList = new LinkedList<>();
                    paramMap.put(name, indexList);
                }
                indexList.add(index);

                index++;
            }
            parsedQuery.append(c);
        }

        return parsedQuery.toString();
    }

    String parsePropertyNames(String sql, Class<?> objectClass, Connection connection) {
        log.debug("parsePropertyNames using : with SQL: %s", sql);

        int length = sql.length();
        StringBuilder parsedQuery = new StringBuilder(length);

        String sd = session.metaData.getConnectionType().getKeywordStartDelimiter();
        String ed = session.metaData.getConnectionType().getKeywordEndDelimiter();

        Set<Character> startDelims = new HashSet<>(4);
        startDelims.add('"');
        startDelims.add('\'');

        if (Util.isNotEmpty(session.metaData.getConnectionType().getKeywordStartDelimiter())) {
            startDelims.add(sd.charAt(0));
        }

        Set<Character> endDelims = new HashSet<>(4);
        endDelims.add('"');
        endDelims.add('\'');

        if (Util.isNotEmpty(session.metaData.getConnectionType().getKeywordEndDelimiter())) {
            endDelims.add(ed.charAt(0));
        }

        Map<String, PropertyInfo> properties = session.metaData.getTableColumnsPropertyInfo(objectClass, connection);

        Set<String> propertiesNotFound = new LinkedHashSet<>();

        boolean inDelimiter = false;
        boolean appendChar;
        for (int i = 0; i < length; i++) {
            appendChar = true;
            char c = sql.charAt(i);
            if (inDelimiter) {
                if (endDelims.contains(c)) {
                    inDelimiter = false;
                }
            } else if (startDelims.contains(c)) {
                inDelimiter = true;
            } else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                int j = i + 2;
                while (j < length && Character.isJavaIdentifierPart(sql.charAt(j))) {
                    j++;
                }
                String name = sql.substring(i + 1, j);
                log.debug("parsePropertyNames property name: %s", name);
                i += name.length(); // skip past the end if the property name

                boolean found = false;
                for (String col : properties.keySet()) {
                    PropertyInfo propertyInfo = properties.get(col);
                    // ignore case?
                    if (propertyInfo.propertyName.equals(name)) {
                        parsedQuery.append(sd).append(col).append(ed);
                        appendChar = false;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    propertiesNotFound.add(name);
                }
            }
            if (appendChar) {
                parsedQuery.append(c);
            }
        }

        if (propertiesNotFound.size() > 0) {
            throw new PersismException(Messages.QueryPropertyNamesMissingOrNotFound.message(propertiesNotFound, sql));
        }
        String parsedSql = parsedQuery.toString();
        log.debug("parsePropertyNames SQL: %s", parsedSql);
        return parsedSql;
    }

    void checkIfOkForWriteOperation(Object object, String operation) {
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

    Object getTypedValueReturnedFromGeneratedKeys(Class<?> objectClass, ResultSet rs) throws SQLException {

        Object value = null;
        Types type = Types.getType(objectClass);

        if (type == null) {
            log.warn(Messages.UnknownTypeForPrimaryGeneratedKey.message(objectClass));
            return rs.getObject(1);
        }

        value = switch (type) {
            case integerType, IntegerType -> rs.getInt(1);
            case longType, LongType -> rs.getLong(1);
            default -> rs.getObject(1);
        };
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
                        value = session.converter.convert(param, Time.class, "Parameter " + n);
                        st.setObject(n, value);
                        break;

                    case UtilDateType:
                    case LocalDateType:
                    case LocalDateTimeType:
                        value = session.converter.convert(param, Timestamp.class, "Parameter " + n);
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
                        if (session.metaData.getConnectionType() == ConnectionTypes.PostgreSQL) {
                            st.setObject(n, param.toString(), java.sql.Types.OTHER);
                        } else {
                            st.setString(n, param.toString());
                        }
                        break;

                    case UUIDType:
                        if (session.metaData.getConnectionType() == ConnectionTypes.PostgreSQL) {
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
                if (session.metaData.getConnectionType() == ConnectionTypes.UCanAccess) {
                    st.setNull(n, java.sql.Types.OTHER);
                } else {
                    st.setObject(n, param);
                }
            }

            n++;
        }
    }


    // parent is a POJO or a List of POJOs
    void handleJoins(Object parent, Class<?> parentClass, String parentSql, Parameters parentParams) throws IllegalAccessException, InvocationTargetException {
        // todo maybe we could add a check for fetch after insert. In those cases there's no need to query for child lists - BUT we do need query for child SINGLES

        List<PropertyInfo> joinProperties = MetaData.getPropertyInfo(parentClass).stream().filter(PropertyInfo::isJoin).collect(Collectors.toList());

        for (PropertyInfo joinProperty : joinProperties) {
            Join joinAnnotation = (Join) joinProperty.getAnnotation(Join.class);
            JoinInfo joinInfo = new JoinInfo(joinAnnotation, joinProperty, parent, parentClass);

            if (joinInfo.parentIsAQuery) {
                assert ((Collection<?>) parent).size() > 0;
            }

            String parentWhere;
            if (parentSql.toUpperCase().contains(" WHERE ")) {
                parentWhere = parentSql.substring(parentSql.toUpperCase().indexOf(" WHERE ") + 7);
            } else {
                parentWhere = ""; // todo test this condition
            }
            String whereClause = getChildWhereClause(joinInfo, parentWhere);

            List<Object> params = parentParams.parameters;

            if (Collection.class.isAssignableFrom(joinProperty.field.getType())) {
                // query
                List<?> childList;
                childList = session.query(joinInfo.childClass, where(whereClause), params(params.toArray()));

                if (joinInfo.parentIsAQuery) {
                    List<?> parentList = (List<?>) parent;
                    stitch(joinInfo, parentList, childList);
                } else {
                    assignJoinedList(joinProperty, parent, childList);
                }
            } else {
                // single object property

                // fetch
                List<?> childList;
                childList = session.query(joinInfo.childClass, where(whereClause), params(params.toArray()));

                if (joinInfo.parentIsAQuery) {

                    stitch(joinInfo, (List<?>) parent, childList);

                } else {
                    // TODO currently can't work with record? Nope. We'd need to work in reverse order or something. Wait for refactor of this method.
                    if (childList.size() > 0) {
                        if (joinProperty.setter != null) {
                            joinProperty.setter.invoke(parent, childList.get(0));
                        } else {
                            throw new PersismException("No Setter for " + joinProperty.propertyName + " in " + parentClass);
                        }
                    }
                    if (childList.size() > 1) {
                        log.warn("why do I have more than 1?");
                    }
                }
            }
        }
    }

    private void stitch(JoinInfo joinInfo, List<?> parentList, List<?> childList) throws InvocationTargetException, IllegalAccessException {

        long now = System.currentTimeMillis();

        Map<Object, Object> parentMap;
        if (joinInfo.parentProperties.size() == 1 && joinInfo.childProperties.size() == 1) {
            PropertyInfo propertyInfo = joinInfo.parentProperties.get(0);
            PropertyInfo childPropertyInfo = joinInfo.childProperties.get(0);

            // todo this parent map is created for each join. Maybe only do it once?
            // https://stackoverflow.com/questions/32312876/ignore-duplicates-when-producing-map-using-streams
            parentMap = parentList.stream().collect(Collectors.toMap(o -> propertyInfo.getValue(o), o -> o, (o1, o2) -> o1));

            for (Object child : childList) {
                Object parent = parentMap.get(childPropertyInfo.getValue(child));
                if (Collection.class.isAssignableFrom(joinInfo.joinProperty.field.getType())) {
                    var list = (Collection) joinInfo.joinProperty.getValue(parent);
                    if (list == null) {
                        log.warn("list should be instantiated? ");
                    }
                    list.add(child);

                } else {
                    joinInfo.joinProperty.setter.invoke(parent, child);
                }
            }
        } else {
            // todo test this junk
            parentMap = parentList.stream().
                    collect(Collectors.
                            toMap(o -> {
                                List<Object> values = new ArrayList<>();
                                for (int j = 0; j < joinInfo.parentProperties.size(); j++) {
                                    values.add(joinInfo.parentProperties.get(j).getValue(o));
                                }
                                return new KeyBox(joinInfo.caseSensitive, values.toArray());
                            }, o -> o, (o1, o2) -> o1));


            for (Object child : childList) {
                List<Object> values = new ArrayList<>();
                for (int j = 0; j < joinInfo.childProperties.size(); j++) {
                    values.add(joinInfo.childProperties.get(j).getValue(child));
                }

                KeyBox keyBox = new KeyBox(joinInfo.caseSensitive, values.toArray());

                Object parent = parentMap.get(keyBox);
                if (Collection.class.isAssignableFrom(joinInfo.joinProperty.field.getType())) {
                    var list = (Collection) joinInfo.joinProperty.getValue(parent);
                    if (list == null) {
                        log.warn("list should be instantiated? ");
                    }
                    list.add(child);

                } else {
                    joinInfo.joinProperty.setter.invoke(parent, child);
                }

            }
        }

        log.debug("stitch Time: " + (System.currentTimeMillis() - now));
    }

    private void assignJoinedList(PropertyInfo joinProperty, Object parentObject, List list) throws IllegalAccessException, InvocationTargetException {

        // no null test - the object should have some List initialized.

        List joinedList = (List) joinProperty.getter.invoke(parentObject);
        if (joinedList == null) {
            throw new PersismException("Cannot join to null for property: " + joinProperty.propertyName + ". Instantiate the property as ArrayList<T> in your constructor.");
        }
        joinedList.clear();
        joinedList.addAll(list);
    }

    private String getChildWhereClause(JoinInfo joinInfo, String parentWhere) {
        String sep = "";
        StringBuilder where = new StringBuilder();


        Map<String, PropertyInfo> properties = session.metaData.getTableColumnsPropertyInfo(joinInfo.parentClass, session.connection);

        for (int j = 0; j < joinInfo.parentPropertyNames.length; j++) {
            String parentColumnName = null;

            for (String key : properties.keySet()) {
                if (properties.get(key).propertyName.equals(joinInfo.parentPropertyNames[j])) {
                    parentColumnName = key;
                    break;
                }
            }

            assert parentColumnName != null;

            String parentTable = session.metaData.getTableName(joinInfo.parentClass);

            String inOrEqualsStart;
            String inOrEqualsEnd;

            inOrEqualsStart = " IN (";
            inOrEqualsEnd = ") ";

            where.append(sep).append(":").append(joinInfo.childPropertyNames[j]).append(inOrEqualsStart);
            where.append("SELECT ").append(parentColumnName).append(" FROM ").append(parentTable);
            if (!parentWhere.isEmpty()) {
                where.append(" WHERE ").append(parentWhere);
            }
            where.append(inOrEqualsEnd);
            sep = " AND ";
        }
        return where.toString();
    }

    boolean isSelect(String sql) {
        assert sql != null;
        return sql.trim().substring(0, 7).trim().equalsIgnoreCase("select");
    }

    boolean isStoredProc(String sql) {
        return !isSelect(sql);
    }

    private <T> void checkIfStoredProcOrSQL(Class<T> objectClass, SQL sql) {
        boolean startsWithSelect = isSelect(sql.sql);
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
