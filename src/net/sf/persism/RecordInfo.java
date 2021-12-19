package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

class RecordInfo<T> {

    Map<String, PropertyInfo> propertiesByColumn;
    Map<String, PropertyInfo> propertyInfoByConstructorOrder;
    List<Class<?>> constructorTypes;
    Constructor<T> constructor;
    Map<String, Integer> ordinals;


    public RecordInfo(Class<T> objectClass, Constructor<T> selectedConstructor, Session session, ResultSet rs) throws SQLException {

        // resultset may not have columns in the proper order
        // resultset may not have all columns
        // get column order based on which properties are found
        // get matching constructor

        List<String> propertyNames = session.metaData.getPropertyNames(objectClass);

        if (objectClass.getAnnotation(NotTable.class) == null) {
            propertiesByColumn = session.metaData.getTableColumnsPropertyInfo(objectClass, session.connection);
        } else {
            propertiesByColumn = session.metaData.getQueryColumnsPropertyInfo(objectClass, rs);
        }

        // now re-arrange by property order
        propertyInfoByConstructorOrder = new LinkedHashMap<>(selectedConstructor.getParameterCount());
        for (String paramName : propertyNames) {
            for (String col : propertiesByColumn.keySet()) {
                if (paramName.equals(propertiesByColumn.get(col).field.getName())) {
                    propertyInfoByConstructorOrder.put(col, propertiesByColumn.get(col));
                }
            }
        }
        constructorTypes = new ArrayList<>(12);

        // put into ordinals the order to read from to match the constructor
        ResultSetMetaData rsmd = rs.getMetaData();
        ordinals = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int j = 1; j <= rsmd.getColumnCount(); j++) {
            ordinals.put(rsmd.getColumnLabel(j), j);
        }
        for (String col : propertyInfoByConstructorOrder.keySet()) {
            if (ordinals.containsKey(col)) {
                constructorTypes.add(propertyInfoByConstructorOrder.get(col).field.getType());
            } else {
                // can happen if a user manually constructs the SQL and misses a column
                throw new PersismException(Messages.ReadRecordColumnNotFound.message(objectClass, col));
            }
        }

        try {
            constructor = objectClass.getConstructor(constructorTypes.toArray(new Class<?>[0]));
        } catch (NoSuchMethodException e) {
            throw new PersismException(Messages.ReadRecordCouldNotInstantiate.message(objectClass, constructorTypes));
        }
    }
}
