package net.sf.persism;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static net.sf.persism.Util.listEqualsIgnoreOrder;

class RecordInfo<T> {

    private final Map<String, PropertyInfo> propertyInfoByConstructorOrder;
    private final Constructor<T> constructor;
    private final Map<String, Integer> ordinals;

    public RecordInfo(Class<T> objectClass, Map<String, PropertyInfo> properties, ResultSet rs) throws SQLException {
        // resultset may not have columns in the proper order
        // resultset may not have all columns
        // get column order based on which properties are found
        // get matching constructor

        List<String> propertyNames = new ArrayList<>(properties.values().stream().map(propertyInfo -> propertyInfo.propertyName).toList());
        Constructor<T> selectedConstructor = findConstructor(objectClass, propertyNames);

        // now re-arrange by property order
        propertyInfoByConstructorOrder = new LinkedHashMap<>(selectedConstructor.getParameterCount());
        for (String paramName : propertyNames) {
            for (String col : properties.keySet()) {
                if (paramName.equals(properties.get(col).field.getName())) {
                    propertyInfoByConstructorOrder.put(col, properties.get(col));
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
                throw new PersismException(Message.ReadRecordColumnNotFound.message(objectClass, col));
            }
        }

        try {
            constructor = objectClass.getConstructor(constructorTypes.toArray(new Class<?>[0]));
            assert constructor.equals(selectedConstructor);
        } catch (NoSuchMethodException e) {
            throw new PersismException(Message.ReadRecordCouldNotInstantiate.message(objectClass, constructorTypes));
        }
    }

    private Constructor<T> findConstructor(Class<T> objectClass, List<String> propertyNames) {

        //noinspection unchecked
        Constructor<T>[] constructors = (Constructor<T>[]) objectClass.getConstructors();
        Constructor<T> selectedConstructor = null;

        for (Constructor<T> constructor : constructors) {
            // Check with canonical or maybe -parameters
            List<String> parameterNames = Arrays.stream(constructor.getParameters()).
                    map(Parameter::getName).collect(Collectors.toList());

            // why don't I just use the parameterNames instead of modifying property list?
            if (listEqualsIgnoreOrder(propertyNames, parameterNames)) {
                // re-arrange the propertyNames to match parameterNames
                propertyNames.clear();
                propertyNames.addAll(parameterNames);
                selectedConstructor = constructor;
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
                    selectedConstructor = constructor;
                    break;
                }
            }
        }

        if (selectedConstructor == null) {
            throw new PersismException(Message.CouldNotFindConstructorForRecord.message(objectClass.getName(), propertyNames));
        }
        return selectedConstructor;

    }

    public Map<String, PropertyInfo> propertyInfoByConstructorOrder() {
        return propertyInfoByConstructorOrder;
    }

    public Constructor<T> constructor() {
        return constructor;
    }

    public Map<String, Integer> ordinals() {
        return ordinals;
    }

}
