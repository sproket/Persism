package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;

final class RecordReader {

    private Session session;
    private MetaData metaData;

    public RecordReader(Session session) {
        this.session = session;
        metaData = session.getMetaData();
    }

    <T> T readRecord(Class<?> objectClass, ResultSet rs) {
        Connection con = session.getConnection();
        Map<String, PropertyInfo> propertiesByColumn = getPropertiesByColumn(objectClass, rs, con);



        return null;


    }

    private Map<String, PropertyInfo> getPropertiesByColumn(Class<?> objectClass, ResultSet rs, Connection con) {
        Map<String, PropertyInfo> propertiesByColumn;
        if (objectClass.getAnnotation(NotTable.class) == null) {
            propertiesByColumn = metaData.getTableColumnsPropertyInfo(objectClass, con);
        } else {
            propertiesByColumn = metaData.getQueryColumnsPropertyInfo(objectClass, rs);
        }
        return propertiesByColumn;
    }
}
