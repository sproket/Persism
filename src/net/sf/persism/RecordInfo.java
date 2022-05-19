package net.sf.persism;

import net.sf.persism.annotations.NotTable;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static net.sf.persism.Util.listEqualsIgnoreOrder;

// todo cache RecordInfo key class, map key propertyNames (string) value RecordInfo
class RecordInfo<T> {

    Map<String, PropertyInfo> propertiesByColumn;
    Map<String, PropertyInfo> propertyInfoByConstructorOrder;
    Constructor<T> constructor;
    Map<String, Integer> ordinals;

    public RecordInfo(Class<T> objectClass, Session session, ResultSet rs) throws SQLException {
        // resultset may not have columns in the proper order
        // resultset may not have all columns
        // get column order based on which properties are found
        // get matching constructor

        if (objectClass.getAnnotation(NotTable.class) == null) {
            propertiesByColumn = session.metaData.getTableColumnsPropertyInfo(objectClass, session.connection);
        } else {
            propertiesByColumn = session.metaData.getQueryColumnsPropertyInfo(objectClass, rs);
        }

        List<String> propertyNames = new ArrayList<>(session.metaData.getPropertyNames(objectClass));
        Constructor<T> selectedConstructor = findConstructor(objectClass, propertyNames);

        // now re-arrange by property order
        propertyInfoByConstructorOrder = new LinkedHashMap<>(selectedConstructor.getParameterCount());
        for (String paramName : propertyNames) {
            for (String col : propertiesByColumn.keySet()) {
                if (paramName.equals(propertiesByColumn.get(col).field.getName())) {
                    propertyInfoByConstructorOrder.put(col, propertiesByColumn.get(col));
                }
            }
        }

        List<Class<?>> constructorTypes = new ArrayList<>(12);
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

    private Constructor<T> findConstructor(Class<T> objectClass, List<String> propertyNames) {

        Constructor<?>[] constructors = objectClass.getConstructors();
        Constructor<T> selectedConstructor = null;

        for (Constructor<?> constructor : constructors) {
            // Check with canonical or maybe -parameters
            List<String> parameterNames = Arrays.stream(constructor.getParameters()).
                    map(Parameter::getName).collect(Collectors.toList());

            // why don't I just use the paremeterNames instead of modifying property list?
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
}
