package net.sf.persism;

import net.sf.persism.annotations.Join;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

final class Joiner {

    private static final Log log = Log.getLogger(Joiner.class);

    final Session session;

  //  private List<PropertyInfo> joinProperties;
    private String[] parentPropertyNames;
    private String[] childPropertyNames;
    private Collection<PropertyInfo> parentProperties;
    private Collection<PropertyInfo> childProperties;
    private Object parent; // may be a POJO or a list of POJOs
    Class<?> objectClass;
    private Object parentObject;
    private Object childObject;
    private boolean caseSensitive;

    Joiner(Session session) {
        this.session = session;
    }

    void init(Object parent, Class<?> objectClass) {
        this.parent = parent;
        this.objectClass = objectClass;
//        joinProperties = MetaData.getPropertyInfo(objectClass).stream().filter(PropertyInfo::isJoin).collect(Collectors.toList());
    }

    Object getObjectFromJoin() {
        // this might be a better way to do this rather than a void method.
        return null;
    }

    void join(PropertyInfo joinProperty)  throws IllegalAccessException, InvocationTargetException  {
        Join joinAnnotation = (Join) joinProperty.getAnnotation(Join.class);
        parentPropertyNames = joinAnnotation.onProperties().split(",");
        childPropertyNames = joinAnnotation.toProperties().split(",");

        if (parentPropertyNames.length != childPropertyNames.length) {
            throw new PersismException("how would I match these?");
        }
        Util.trimArray(parentPropertyNames);
        Util.trimArray(childPropertyNames);
        // todo test these properties exist and fail otherwise

        caseSensitive = joinAnnotation.caseSensitive();

        Class<?> childClass = joinAnnotation.to();

        boolean parentIsAQuery = Collection.class.isAssignableFrom(parent.getClass());
        if (parentIsAQuery) {
            // this method should not be called if the list parent list is empty
            assert ((Collection<?>) parent).size() > 0;
            // where parent is a query collect parent values for each foreign property name
            // construct a WHERE IN query
            // execute once
            // match results to parent record by parent values
            log.info("called from a query? " + parent.getClass() + " of class " + objectClass);
        }

        Collection<PropertyInfo> parentProperties = MetaData.getPropertyInfo(objectClass); // todo parentClassProperties

        Map<String, Set<Object>> parentPropertyValuesMap = new HashMap<>();
        List<Object> parentPropertyValues = new ArrayList<>();

        for (int j = 0; j < parentPropertyNames.length; j++) {
            var propertyName = parentPropertyNames[j];
            //parentPropertyValuesMap.put(propertyName)

            Optional<PropertyInfo> propertyInfo = parentProperties.stream().filter(p -> p.propertyName.equals(propertyName)).findFirst();
            if (propertyInfo.isPresent()) {
                propertyInfo.get().field.setAccessible(true);
                if (parentIsAQuery) {
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


        String sep = "";
        String inSep = "";
        StringBuilder where = new StringBuilder();
        for (int j = 0; j < parentPropertyNames.length; j++) {
            Set<Object> values = parentPropertyValuesMap.get(parentPropertyNames[j]);
            String inOrEqualsStart;
            String inOrEqualsEnd;
            if (values == null) {
                throw new PersismException("Could not find toProperty: " + parentPropertyNames[j]);
            }
            if (values.size() > 1) {
                inOrEqualsStart = " IN (";
                inOrEqualsEnd = ") ";
            } else {
                inOrEqualsStart = " = ";
                inOrEqualsEnd = " ";
            }

            where.append(sep).append(":").append(childPropertyNames[j]).append(inOrEqualsStart);
            for (Object val : values) {
                where.append(inSep).append("?");
                inSep = ", ";
            }
            where.append(inOrEqualsEnd);
            sep = " AND ";
            inSep = "";
        }

        // https://stackoverflow.com/questions/47224319/flatten-lists-in-map-into-single-list
        List<Object> params = parentPropertyValuesMap.values().stream().flatMap(Set::stream).toList();

        Collection<PropertyInfo> childProperties = MetaData.getPropertyInfo(childClass);

    }

}
