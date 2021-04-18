package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
Weak attempt at jooq like dsl. Not worth it.
 */
class Query<T> {

    private static final Log log = Log.getLogger(Query.class);


    private String sd;
    private String ed;

    T pojo;
    Session session;

    private StringBuilder sb;
    private List<Object> parameters;

    private boolean selectAdded;
    private boolean whereAdded;

    public Query(T pojo, Session session) {
        this.pojo = pojo;
        this.session = session;
        log.warn(pojo.getClass());

        sb = new StringBuilder();

        sd = session.getMetaData().getConnectionType().getKeywordStartDelimiter();
        ed = session.getMetaData().getConnectionType().getKeywordEndDelimiter();
        parameters = new ArrayList<>();

    }

    public Query select(String... propertyNames) {
        String cols = "*";
        if (propertyNames.length > 0) {
            cols = convertToColumns(propertyNames);
        }
        sb.append("SELECT ").append(cols).append(" FROM ").append(sd).append(session.getMetaData().getTableName(pojo.getClass())).append(ed).append(" ");
        selectAdded = true;
        return this;
    }

    private String convertToColumns(String[] propertyNames) {

        String sep = "";
        StringBuilder sb1 = new StringBuilder();
        for (int i = 0; i < propertyNames.length; i++) {
            sb1.append(sep).append(convertToColumn(propertyNames[i]));
            sep = ", ";
        }
        return sb1.toString();
    }

    private String convertToColumn(String propertyName) {

        Map<String, PropertyInfo> properties;
        if (pojo.getClass().getAnnotation(NotTable.class) == null) {
            properties = session.getMetaData().getTableColumnsPropertyInfo(pojo.getClass(), session.getConnection());
        } else {
            throw new PersismException("not yet supported");
            // properties = session.getMetaData().getQueryColumnsPropertyInfo(pojo.getClass(), rs);
        }
        String ret = "";

        for (String col : properties.keySet()) {
            PropertyInfo propertyInfo = properties.get(col);
            if (propertyName.equalsIgnoreCase(propertyInfo.propertyName)) {
                ret = sd + col + ed;
            }
        }
        if (ret.isEmpty()) {
            throw new PersismException("property " + propertyName + " not found!");
        }
        return ret;
    }

    public Query where(String propertyName) {
        // String colu todo translate to colymn wuth sd / ed
        if (!selectAdded) {
            select();
        }
        if (!whereAdded) {
            sb.append("WHERE ");
            whereAdded = true;
        }
        sb.append(propertyName).append(" ");
        return this;
    }

    public Query eq(Object value) {
        sb.append("= ? ");
        parameters.add(value);
        return this;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
