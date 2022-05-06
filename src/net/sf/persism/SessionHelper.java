package net.sf.persism;

import net.sf.persism.annotations.Join;
import net.sf.persism.annotations.NotTable;
import net.sf.persism.annotations.View;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
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

        if (log.isDebugEnabled()) {
            log.debug("exec: %s params: %s", sql, Arrays.asList(parameters));
        }
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
                blog.debug("exec time: " + (System.currentTimeMillis() - now) + " " + sql + " params: " + Arrays.asList(parameters));
            }
        }
    }

    // For unit tests only for now.
    void execute(String sql, Object... parameters) {

        log.debug("execute: %s params: %s", sql, Arrays.asList(parameters));

        Statement st = null;
        try {

            if (parameters.length == 0) {
                st = session.connection.createStatement();
                st.execute(sql);
            } else {
                st = session.connection.prepareStatement(sql);
                PreparedStatement pst = (PreparedStatement) st;
                setParameters(pst, parameters);
                pst.execute();
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
     * https://archive.ph/au5XM and https://archive.ph/4OOze
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

        String sd = session.metaData.getConnectionType().getKeywordStartDelimiter();
        String ed = session.metaData.getConnectionType().getKeywordEndDelimiter();

        if (Util.isNotEmpty(sd)) {
            startDelims.add(sd.charAt(0));
        }

        Set<Character> endDelims = new HashSet<>(4);
        endDelims.add('"');

        if (Util.isNotEmpty(ed)) {
            endDelims.add(ed.charAt(0));
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

        if (Util.isNotEmpty(sd)) {
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

    <T> Constructor<T> findConstructor(Class<T> objectClass, List<String> propertyNames) {
        Constructor<?>[] constructors = objectClass.getConstructors();
        Constructor<T> selectedConstructor = null;

        for (Constructor<?> constructor : constructors) {
            // Check with canonical or maybe -parameters
            List<String> parameterNames = Arrays.stream(constructor.getParameters()).
                    map(Parameter::getName).collect(Collectors.toList());

            if (listEqualsIgnoreOrder(propertyNames, parameterNames)) {
                // re-arrange the propertyNames to match parameterNames
                propertyNames.clear();
                propertyNames.addAll(parameterNames);
                selectedConstructor = (Constructor<T>) constructor;
                break;
            }

            // Check with ConstructorProperties
            ConstructorProperties constructorProperties = constructor.getAnnotation(ConstructorProperties.class);
            if (constructorProperties != null) {
                parameterNames = Arrays.asList(constructorProperties.value());
                if (listEqualsIgnoreOrder(propertyNames, parameterNames)) {
                    // re-arrange the propertyNames to match parameterNames
                    propertyNames.clear();
                    propertyNames.addAll(parameterNames);
                    selectedConstructor = (Constructor<T>) constructor;
                    break;
                }
            }
        }

        if (selectedConstructor == null) {
            throw new PersismException(Messages.CouldNotFindConstructorForRecord.message(objectClass, propertyNames));
        }
        return selectedConstructor;
    }

    // https://stackoverflow.com/questions/1075656/simple-way-to-find-if-two-different-lists-contain-exactly-the-same-elements
    private static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }


    void setParameters(PreparedStatement st, Object[] parameters) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("setParameters PARAMS: %s", Arrays.asList(parameters));
        }

        Object value; // used for conversions
        int n = 1;
        for (Object param : parameters) {

            if (param != null) {

                Types paramType = Types.getType(param.getClass());
                if (paramType == null) {
                    log.warn(Messages.UnknownTypeInSetParameters.message(param.getClass()));
                    paramType = Types.ObjectType;
                }

                switch (paramType) {

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
                        log.warn(Messages.UnSupportedTypeInSetParameters.message(paramType));
                        st.setObject(n, param);
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
                if (session.metaData.getConnectionType() == ConnectionTypes.UCanAccess) {
                    st.setNull(n, java.sql.Types.OTHER);
                } else {
                    st.setObject(n, null);
                }
            }

            n++;
        }
    }


    // todo we really need a way to detect infinite loops and FAIL FAST
    // parent is a POJO or a List of POJOs
    void handleJoins(Object parent, Class<?> parentClass, String parentSql, Parameters parentParams) {
        // maybe we could add a check for fetch after insert. In those cases there's no need to query for child lists - BUT we do need query for child SINGLES
        // NOT really important. At most 1 extra query per type will be run (and no child queries after that)

        List<PropertyInfo> joinProperties = MetaData.getPropertyInfo(parentClass).stream().filter(PropertyInfo::isJoin).toList();

        for (PropertyInfo joinProperty : joinProperties) {
            Join joinAnnotation = (Join) joinProperty.getAnnotation(Join.class);
            JoinInfo joinInfo = new JoinInfo(joinAnnotation, joinProperty, parent, parentClass);

            if (joinInfo.parentIsAQuery()) {
                // We expect this method not to be called if the result query has 0 rows.
                assert ((Collection<?>) parent).size() > 0;
            }

            String parentWhere;
            if (parentSql.toUpperCase().contains(" WHERE ")) {
                parentWhere = parentSql.substring(parentSql.toUpperCase().indexOf(" WHERE ") + 7);
            } else {
                parentWhere = "";
            }
            String whereClause = getChildWhereClause(joinInfo, parentWhere);
            List<Object> params = new ArrayList<>(parentParams.parameters);

            if (params.size() > 0) {
                // normalize params to ? since we may have repeated the SELECT IN query
                long qmCount = whereClause.chars().filter(ch -> ch == '?').count();
                int index = 0;
                while (qmCount > params.size()) {
                    params.add(params.get(index));
                    index++;
                }
            }

            // join to a collection
            if (Collection.class.isAssignableFrom(joinProperty.field.getType())) {
                // query
                List<?> childList = session.query(joinInfo.childClass(), where(whereClause), params(params.toArray()));

                if (joinInfo.parentIsAQuery()) {
                    // many to many
                    List<?> parentList = (List<?>) parent;
                    stitch(joinInfo, parentList, childList);
                } else {
                    // one to many
                    assignJoinedList(joinProperty, parent, childList);
                }

            } else { // join to single object

                if (joinInfo.parentIsAQuery()) {
                    // many to one
                    List<?> childList = session.query(joinInfo.childClass(), where(whereClause), params(params.toArray()));
                    stitch(joinInfo.swapParentAndChild(), childList, (List<?>) parent);

                } else {
                    // one to one
                    Object child = session.fetch(joinInfo.childClass(), where(whereClause), params(params.toArray()));
                    if (child != null) {
                        joinProperty.setValue(parent, child);
                    }
                }
            }
        }
    }

    private void stitch(JoinInfo joinInfo, List<?> parentList, List<?> childList) {
        blog.debug("STITCH " + joinInfo);

        if (childList.size() == 0) {
            return;
        }
        long now = System.currentTimeMillis();

        Map<Object, Object> parentMap;
        if (joinInfo.parentProperties().size() == 1 && joinInfo.childProperties().size() == 1) {

            PropertyInfo parentPropertyInfo = joinInfo.parentProperties().get(0);
            PropertyInfo childPropertyInfo = joinInfo.childProperties().get(0);

            // https://stackoverflow.com/questions/32312876/ignore-duplicates-when-producing-map-using-streams
            parentMap = parentList.stream().collect(Collectors.
                    toMap(obj -> {
                        Object value = parentPropertyInfo.getValue(obj);
                        if (!joinInfo.caseSensitive() && value instanceof String s) {
                            return s.toUpperCase();
                        }
                        return value;
                    }, o -> o, (o1, o2) -> o1));

            for (Object child : childList) {
                Object parent = parentMap.get(childPropertyInfo.getValue(child));
                setPropertyFromJoinInfo(joinInfo, parent, child);
            }

        } else {
            parentMap = parentList.stream().collect(Collectors.
                    toMap(obj -> {
                        List<Object> values = new ArrayList<>();
                        for (int j = 0; j < joinInfo.parentProperties().size(); j++) {
                            values.add(joinInfo.parentProperties().get(j).getValue(obj));
                        }
                        return new KeyBox(joinInfo.caseSensitive(), values.toArray());
                    }, o -> o, (o1, o2) -> o1));

            for (Object child : childList) {
                List<Object> values = new ArrayList<>();
                for (int j = 0; j < joinInfo.childProperties().size(); j++) {
                    values.add(joinInfo.childProperties().get(j).getValue(child));
                }

                KeyBox keyBox = new KeyBox(joinInfo.caseSensitive(), values.toArray());
                Object parent = parentMap.get(keyBox);
                if (parent == null) {
                    log.warn("parent not found: " + keyBox); // Should not usually occur. Why would we not find a parent?
                } else {
                    setPropertyFromJoinInfo(joinInfo, parent, child);
                }
            }
        }

        blog.debug("stitch Time: " + (System.currentTimeMillis() - now));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setPropertyFromJoinInfo(JoinInfo joinInfo, Object parent, Object child) {
        if (Collection.class.isAssignableFrom(joinInfo.joinProperty().field.getType())) {
            var list = (Collection) joinInfo.joinProperty().getValue(parent);
            if (list == null) {
                throw new PersismException(Messages.CannotNotJoinToNullProperty.message(joinInfo.joinProperty().propertyName));
            }
            list.add(child);
        } else {
            if (joinInfo.reversed()) {
                joinInfo.joinProperty().setValue(child, parent);
            } else {
                joinInfo.joinProperty().setValue(parent, child);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void assignJoinedList(PropertyInfo joinProperty, Object parentObject, List list) {

        // no null test - the object should have some List initialized.

        List joinedList = (List) joinProperty.getValue(parentObject);
        if (joinedList == null) {
            throw new PersismException(Messages.CannotNotJoinToNullProperty.message(joinProperty.propertyName));
        }
        joinedList.clear();
        joinedList.addAll(list);
    }

// todo cache these Strings
    private String getChildWhereClause(JoinInfo joinInfo, String parentWhere) {

        StringBuilder where = new StringBuilder();
        String sd = session.metaData.getConnectionType().getKeywordStartDelimiter();
        String ed = session.metaData.getConnectionType().getKeywordEndDelimiter();
        String parentTableName = session.metaData.getTableName(joinInfo.parentClass());
        String childTableName = session.metaData.getTableName(joinInfo.childClass());

        String parentAlias = "";
        if (parentTableName.equals(childTableName)) {
            // for self join we need an alias
            parentAlias = session.metaData.getTableName(joinInfo.parentClass()).substring(0, 1).toUpperCase();
        }

        int n = parentWhere.toUpperCase().indexOf("ORDER BY");
        if (n > -1) {
            parentWhere = parentWhere.substring(0, n);
        }

        Map<String, PropertyInfo> parentProperties = session.metaData.getTableColumnsPropertyInfo(joinInfo.parentClass(), session.connection);
        Map<String, PropertyInfo> childProperties = session.metaData.getTableColumnsPropertyInfo(joinInfo.childClass(), session.connection);
        where.append("EXISTS (SELECT ");
        String sep = "";
        for (int j = 0; j < joinInfo.parentPropertyNames().length; j++) {
            String parentColumnName = sd + getColumnName(joinInfo.parentPropertyNames()[j], parentProperties) + ed;
            where.append(sep).append(parentColumnName);
            sep = ",";
        }

        //where.append(" FROM ").append(parentTableName).append(" ").append(parentAlias).append(" WHERE ").append(parentWhere).append(" ");
        where.append(" FROM ").append(parentTableName);
        if (Util.isNotEmpty(parentAlias)) {
            where.append(" ").append(parentAlias);
        }
        where.append(" WHERE ").append(parentWhere).append(" ");
        sep = Util.isEmpty(parentWhere) ? "" : " AND ";
        for (int j = 0; j < joinInfo.parentPropertyNames().length; j++) {
            String parentColumnName = sd + getColumnName(joinInfo.parentPropertyNames()[j], parentProperties) + ed;
            String childColumnName = sd + getColumnName(joinInfo.childPropertyNames()[j], childProperties) + ed;

            if (Util.isNotEmpty(parentAlias)) {
                where.append(sep).append(parentAlias);
            } else {
                where.append(sep).append(parentTableName);
            }

            where.append(".").append(parentColumnName).append(" = ").
                    append(childTableName).append(".").append(childColumnName);
            sep = " AND ";
        }
        where.append(") ");

        return where.toString();
    }

    private String getColumnName(String propertyName, Map<String, PropertyInfo> properties) {
        for (String key : properties.keySet()) {
            if (properties.get(key).propertyName.equals(propertyName)) {
                return key;
            }
        }
        return null;
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
