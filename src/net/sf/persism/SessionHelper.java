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

    private final Session session;

    public SessionHelper(Session session) {
        this.session = session;
    }


    JDBCResult executeQuery(Class<?> objectClass, SQL sql, Parameters parameters) throws SQLException {

        JDBCResult result = new JDBCResult();
        String sqlQuery = sql.toString();
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
        } else {
            checkIfStoredProcOrSQL(objectClass, sql);
        }

        if (log.isDebugEnabled()) {
            log.debug("executeQuery: %s params: %s", sqlQuery, parameters);
        }

        exec(result, sqlQuery, parameters.toArray());

        return result;
    }

    void exec(JDBCResult result, String sql, Object... parameters) throws SQLException {
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
            throw new SQLException(e.getMessage() + " SQL: " + sql, e);
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
    void handleJoins(Object parent, Class<?> parentClass) throws IllegalAccessException, InvocationTargetException {

        List<PropertyInfo> joinProperties = MetaData.getPropertyInfo(parentClass).stream().filter(PropertyInfo::isJoin).collect(Collectors.toList());

        for (PropertyInfo joinProperty : joinProperties) {
            Join joinAnnotation = (Join) joinProperty.getAnnotation(Join.class);
            JoinInfo joinInfo = new JoinInfo(parent, parentClass, joinAnnotation);

            if (joinInfo.parentIsAQuery) {
                assert ((Collection<?>) parent).size() > 0;
            }

            Map<String, Set<Object>> parentPropertyValuesMap = initParentPropertyValuesMap(parent, joinInfo);

            String whereClause = getChildWhereClause(joinInfo, parentPropertyValuesMap);

            // https://stackoverflow.com/questions/47224319/flatten-lists-in-map-into-single-list
            List<Object> params = parentPropertyValuesMap.values().stream().flatMap(Set::stream).toList();

            if (Collection.class.isAssignableFrom(joinProperty.field.getType())) {
                // query
                var result = session.query(joinInfo.childClass, where(whereClause), params(params.toArray()));
                if (joinInfo.parentIsAQuery) {
                    List<?> parentList = (List<?>) parent;
                    // loop and find id for each parent and set the setter after matching IDs....
                    for (Object parentObject : parentList) {
                        List<?> list = result.stream().
                                filter(childObject -> filterChildData(joinInfo, parentObject, childObject)).
                                collect(Collectors.toList());

                        assignJoinedList(joinProperty, parentObject, list);
                    }

                } else {
                    assignJoinedList(joinProperty, parent, result);
                }
            } else {
                // single object property

                // fetch
                var result = session.query(joinInfo.childClass, where(whereClause), params(params.toArray()));

                if (joinInfo.parentIsAQuery) {
                    List<?> parentList = (List<?>) parent;
                    // loop and find id for each parent and set the setter after matching IDs....
                    for (Object parentObject : parentList) {
                        var opt = result.stream().
                                filter(childObject -> filterChildData(joinInfo, parentObject, childObject)).
                                findFirst();

                        if (opt.isPresent()) {
                            joinProperty.setter.invoke(parentObject, opt.get());
                        }
                    }
                } else {
                    // TODO currently can't work with record? Nope. We'd need to work in reverse order or something. Wait for refactor of this method.
                    if (result.size() > 0) {
                        joinProperty.setter.invoke(parent, result.get(0));
                    }
                    if (result.size() > 1) {
                        log.warn("why do I have more than 1?");
                    }
                }
            }

        }
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

    private String getChildWhereClause(JoinInfo joinInfo, Map<String, Set<Object>> parentPropertyValuesMap) {
        String sep = "";
        String inSep = "";
        StringBuilder where = new StringBuilder();
        for (int j = 0; j < joinInfo.parentPropertyNames.length; j++) {
            Set<Object> values = parentPropertyValuesMap.get(joinInfo.parentPropertyNames[j]);
            String inOrEqualsStart;
            String inOrEqualsEnd;
            if (values == null) {
                throw new PersismException("Could not find toProperty: " + joinInfo.parentPropertyNames[j]);
            }
            if (values.size() > 1) {
                inOrEqualsStart = " IN (";
                inOrEqualsEnd = ") ";
            } else {
                inOrEqualsStart = " = ";
                inOrEqualsEnd = " ";
            }

            where.append(sep).append(":").append(joinInfo.childPropertyNames[j]).append(inOrEqualsStart);
            for (Object val : values) {
                where.append(inSep).append("?");
                inSep = ", ";
            }
            where.append(inOrEqualsEnd);
            sep = " AND ";
            inSep = "";
        }
        return where.toString();
    }

    // return map of property names of primary keys and values used for the child query where clause
    private Map<String, Set<Object>> initParentPropertyValuesMap(Object parent, JoinInfo joinInfo) throws IllegalAccessException {

        Map<String, Set<Object>> parentPropertyValuesMap = new HashMap<>();

        List<Object> parentPropertyValues = new ArrayList<>();
        for (int j = 0; j < joinInfo.parentPropertyNames.length; j++) {
            var propertyName = joinInfo.parentPropertyNames[j];
            Optional<PropertyInfo> propertyInfo = joinInfo.parentProperties.stream().filter(p -> p.propertyName.equals(propertyName)).findFirst();
            if (propertyInfo.isPresent()) {
                propertyInfo.get().field.setAccessible(true);
                if (joinInfo.parentIsAQuery) {
                    List<?> list = (List<?>) parent;
                    for (Object pojo : list) {
                        parentPropertyValues.add(propertyInfo.get().field.get(pojo));
                    }
                } else {
                    parentPropertyValues.add(propertyInfo.get().field.get(parent)); // here the object is a list of POJOS. FAIL.
                }
                propertyInfo.get().field.setAccessible(false);
            } else {
                throw new PersismException("COULD NOT FIND " + propertyName);
            }
            parentPropertyValuesMap.put(propertyName, new LinkedHashSet<>(parentPropertyValues));
            parentPropertyValues.clear();
        }

        return parentPropertyValuesMap;
    }

    private boolean filterChildData(JoinInfo joinInfo, Object parentObject, Object childObject) {
        for (int j = 0; j < joinInfo.parentPropertyNames.length; j++) {
            final int index = j;

            var parentPropertyInfo = joinInfo.parentProperties.stream().filter(p -> p.propertyName.equals(joinInfo.parentPropertyNames[index])).findFirst();
            var childPropertyInfo = joinInfo.childProperties.stream().filter(p -> p.propertyName.equals(joinInfo.childPropertyNames[index])).findFirst();

            if (parentPropertyInfo.isPresent() && childPropertyInfo.isPresent()) {

                // Allow join on null values? I guess so...
                // https://bertwagner.com/posts/joining-on-nulls/
                try {
                    var parentValue = parentPropertyInfo.get().getter.invoke(parentObject);
                    var childValue = childPropertyInfo.get().getter.invoke(childObject);

                    if (parentValue == null) {
                        if (childValue != null) {
                            return false;
                        }
                    }

                    if (joinInfo.caseSensitive) {
                        if (parentValue != null && !parentValue.equals(childValue)) {
                            return false;
                        }

                    } else {
                        // todo the problem with this is you could have a string = "null"
                        if (parentValue != null && !parentValue.toString().equalsIgnoreCase("" + childValue)) {
                            return false;
                        }

                    }

                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new PersismException(e.getMessage(), e);
                }
            }
        }
        return true;
    }


    boolean isSelect(String sql) {
        assert sql != null;
        return sql.trim().substring(0, 7).trim().equalsIgnoreCase("select");
    }

    boolean isStoredProc(String sql) {
        return !isSelect(sql);
    }


    private <T> void checkIfStoredProcOrSQL(Class<T> objectClass, SQL sql) {
        boolean startsWithSelect = isSelect(sql.toString());
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
