package net.sf.persism;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

/**
 * The Command object is used to perform updates in the databases with data objects.
 * @deprecated this will be removed in 1.0
 * @see net.sf.persism.Session
 * @author Dan Howard
 * @since 4/4/12 6:42 PM
 */
public final class Command {

    private static final Log log = Log.getLogger(Command.class);


    private Connection connection;

    private MetaData metaData;

    private Query query = null; // lazy load it - we may not need this object for all cases.

    /**
     * @deprecated
     * @see net.sf.persism.Session
     * @param connection
     * @throws PersismException
     */
    public Command(Connection connection) throws PersismException {
        this.connection = connection;
        init(connection);
    }

    private void init(Connection connection) {

        // place any DB specific properties here.
        try {
            metaData = MetaData.getInstance(connection);
        } catch (SQLException e) {
            throw new PersismException(e);
        }
    }

    /**
     * Updates the data object in the database.
     * @deprecated
     * @see net.sf.persism.Session#update(Object)
     * @param object data object to update.
     * @return usually 1 to indicate rows changed via JDBC.
     * @throws PersismException Indicating the upcoming robot uprising.
     */
    public int update(Object object) throws PersismException {
        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException("Cannot perform update. " + metaData.getTableName(object.getClass()) + " has no primary keys.");
        }

        PreparedStatement st = null;
        try {

            String updateStatement = metaData.getUpdateStatement(object, connection);
            if (updateStatement == null || updateStatement.trim().length() == 0) {
                log.warn("No properties changed. No update required for Object: " + object + " class: " + object.getClass().getName());
                return 0;
            }

            st = connection.prepareStatement(updateStatement);

            // These keys should always be in sorted order.
            Map<String, PropertyInfo> allProperties = metaData.getTableColumns(object.getClass(), connection);
            Map<String, PropertyInfo> changedProperties;
            if (object instanceof Persistable) {
                changedProperties = metaData.getChangedColumns((Persistable) object, connection);
            } else {
                changedProperties = allProperties;
            }

            List<Object> params = new ArrayList<Object>(primaryKeys.size());
            for (String column : changedProperties.keySet()) {
                if (!primaryKeys.contains(column)) {
                    Object value = allProperties.get(column).getter.invoke(object);

                    if (value != null) {
                        if (value.getClass().isEnum()) {
                            value = "" + value; // convert enum to string.
                        }

                        if (value instanceof java.util.Date) {
                            java.util.Date dt = (java.util.Date) value;
                            value = new Timestamp(dt.getTime());
                        }

                        if (value instanceof String) {
                            // check width
                            PropertyInfo propertyInfo = allProperties.get(column);
                            String str = (String) value;
                            if (str.length() > propertyInfo.length) {
                                str = str.substring(0, propertyInfo.length);
                                // todo Should Persism strict should throw this as an exception?
                                log.warn("TRUNCATION with Column: " + column + " for table: " + metaData.getTableName(object.getClass()) + ". Old value: \"" + value + "\" New value: \"" + str + "\"");
                                value = str;
                            }
                        }
                    }
                    params.add(value);
                }
            }

            for (String column : primaryKeys) {
                params.add(allProperties.get(column).getter.invoke(object));
            }
            Util.setParameters(st, params.toArray());
            int ret = st.executeUpdate();
            return ret;

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e);

        } finally {
            Util.cleanup(st, null);
        }
    }

    /**
     * Inserts the data object in the database.
     * @deprecated
     * @see net.sf.persism.Session#insert(Object)
     * @param object the data object to insert.
     * @return usually 1 to indicate rows changed via JDBC.
     * @throws PersismException When planet of the apes starts happening.
     */
    public int insert(Object object) throws PersismException {
        String insertStatement = metaData.getInsertStatement(object, connection);

        PreparedStatement st = null;
        java.sql.ResultSet rs = null;

        try {
            // These keys should always be in sorted order.
            Map<String, PropertyInfo> properties = metaData.getTableColumns(object.getClass(), connection);
            Map<String, ColumnInfo> columns = metaData.getColumns(object.getClass(), connection);

            List<String> generatedKeys = new ArrayList<String>(4);
            for (ColumnInfo column : columns.values()) {
                if (column.generated) {
                    generatedKeys.add(column.columnName);
                }
            }

            if (generatedKeys.size() > 0) {
                String[] keyArray = generatedKeys.toArray(new String[0]);
                st = connection.prepareStatement(insertStatement, keyArray);
            } else {
                st = connection.prepareStatement(insertStatement);
            }

            boolean tableHasDefaultColumnValues = false;

            List<Object> params = new ArrayList<Object>();
            for (ColumnInfo column : columns.values()) {

                PropertyInfo propertyInfo = properties.get(column.columnName);

                // todo is propertyInfo null ever? I don't think so. We only include columns where we know the property.
                if (!column.generated) {

                    // TODO This condition is repeated 3 times. We need to rearrange this code.
                    // See MetaData getInsertStatement - Maybe we should return a new Object type for InsertStatement
                    if (column.hasDefault) {
                        // Do not include if this column has a default and no value has been
                        // set on it's associated property.
                        if (properties.get(column.columnName).getter.invoke(object) == null) {
                            tableHasDefaultColumnValues = true;
                            continue;
                        }
                    }


                    Object value = propertyInfo.getter.invoke(object);

                    if (value != null) {

                        if (value.getClass().isEnum()) {
                            value = "" + value; // convert enum to string.
                        }

                        // sql.Date is a subclass so this would be true
                        if (value instanceof java.util.Date) {
                            java.util.Date dt = (java.util.Date) value;
                            value = new Timestamp(dt.getTime());
                        }


                        if (value instanceof String) {
                            // check width
                            String str = (String) value;
                            if (str.length() > propertyInfo.length) {
                                // todo should Persism strict throw this as an exception?
                                str = str.substring(0, propertyInfo.length);
                                log.warn("TRUNCATION with Column: " + column + " for table: " + metaData.getTableName(object.getClass()) + ". Old value: \"" + value + "\" New value: \"" + str + "\"");
                                value = str;
                            }
                        }
                    }
                    params.add(value);
                }
            }

            // https://forums.oracle.com/forums/thread.jspa?threadID=879222
            // http://download.oracle.com/javase/1.4.2/docs/guide/jdbc/getstart/statement.html
            //int ret = st.executeUpdate(insertStatement, Statement.RETURN_GENERATED_KEYS);
            Util.setParameters(st, params.toArray());
            int ret = st.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("insert ret: " + ret);
            }
            if (generatedKeys.size() > 0) {
                rs = st.getGeneratedKeys();
            }

            // TODO for now we can only support a single auto inc - need to test out other possible generated columns
            for (String column : generatedKeys) {
                if (rs.next()) {

                    Method setter = properties.get(column).setter;

                    if (setter != null) {
                        // todo do we really need to type these? Maybe if the DB uses a GUID?
                        Object value = metaData.getTypedValue(setter.getParameterTypes()[0], rs, 1);
                        if (log.isDebugEnabled()) {
                            log.debug(column + " generated " + value);
                            log.debug(setter);
                        }
                        setter.invoke(object, value);

                    } else {
                        log.warn("no setter found for column " + column);
                    }
                }
            }

            if (tableHasDefaultColumnValues) {
                if (query == null) {
                    query = new Query(connection);
                }
                // Read the full object back to update any properties which had defaults
                query.read(object);
            }

            return ret;
        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e);
        } finally {
            Util.cleanup(st, rs);
        }
    }


    /**
     * Deletes the data object object from the database.
     * @deprecated
     * @see net.sf.persism.Session#delete(Object)
     * @param object data object to delete
     * @return usually 1 to indicate rows changed via JDBC.
     * @throws PersismException Perhaps when asteroid 1999 RQ36 hits us?
     */
    public int delete(Object object) throws PersismException {

        List<String> primaryKeys = metaData.getPrimaryKeys(object.getClass(), connection);
        if (primaryKeys.size() == 0) {
            throw new PersismException("Cannot perform delete. " + metaData.getTableName(object.getClass()) + " has no primary keys.");
        }

        PreparedStatement st = null;
        try {
            String deleteStatement = metaData.getDeleteStatement(object, connection);
            st = connection.prepareStatement(deleteStatement);

            // These keys should always be in sorted order.
            Map<String, PropertyInfo> columns = metaData.getTableColumns(object.getClass(), connection);

            List<Object> params = new ArrayList<Object>(primaryKeys.size());
            for (String column : primaryKeys) {
                params.add(columns.get(column).getter.invoke(object));
            }
            Util.setParameters(st, params.toArray());
            int ret = st.executeUpdate();
            return ret;

        } catch (Exception e) {
            Util.rollback(connection);
            throw new PersismException(e);

        } finally {
            Util.cleanup(st, null);
        }
    }

    /**
     * Execute an arbitrary SQL statement.
     * @deprecated
     * @see net.sf.persism.Session#execute(String, Object...)
     * @param sql
     * @param parameters
     */
    public void execute(String sql, Object... parameters) {

        Statement st = null;

        try {

            if (parameters.length == 0) {
                st = connection.createStatement();
                st.execute(sql);
            } else {
                st = connection.prepareStatement(sql);
                PreparedStatement pst = (PreparedStatement) st;
                Util.setParameters(pst, parameters);
                pst.execute();
            }

        } catch (SQLException e) {
            throw new PersismException(e);
        } finally {
            Util.cleanup(st, null);
        }
    }


    /**
     * Execute an arbut? Why bother? Why even put in Command?
     * @param sql
     * @param parameters
     * @return
     */
     List<LinkedHashMap<String, Object>> query(String sql, Object... parameters) {

        Statement st = null;
        ResultSet rs = null;

        List<LinkedHashMap<String, Object>> results = new ArrayList<LinkedHashMap<String, Object>>(32);
        try {

            if (parameters.length == 0) {
                st = connection.createStatement();
                rs = st.executeQuery(sql);
            } else {
                st = connection.prepareStatement(sql);

                PreparedStatement pst = (PreparedStatement) st;
                int n = 1;
                for (Object o : parameters) {
                    pst.setObject(n, o);
                    n++;
                }
                rs = pst.executeQuery();
            }


            while (rs.next()) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(32);
                int n = rs.getMetaData().getColumnCount();
                for (int j = 1; j <=n; j++) {
                    map.put(rs.getMetaData().getColumnLabel(j), rs.getObject(j));
                }
                results.add(map);
            }
        } catch (SQLException e) {
            throw new PersismException(e);
        } finally {
            Util.cleanup(st, rs);
        }

        return results;
    }

    MetaData getMetaData() {
        return metaData;
    }
}
