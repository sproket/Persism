package net.sf.persism;

import net.sf.persism.annotations.Join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class JoinInfo {

    String[] parentPropertyNames;
    String[] childPropertyNames;
    List<PropertyInfo> parentProperties;
    List<PropertyInfo> childProperties;
    Class<?> parentClass;
    Class<?> childClass;
    boolean caseSensitive;
    boolean parentIsAQuery;
    PropertyInfo joinProperty;

    // note parent may be a POJO or a list of POJOs
    public JoinInfo(Join joinAnnotation, PropertyInfo joinProperty, Object parent, Class<?> parentClass) {
        this.joinProperty = joinProperty;
        parentPropertyNames = joinAnnotation.onProperties().split(",");
        childPropertyNames = joinAnnotation.toProperties().split(",");
        if (parentPropertyNames.length != childPropertyNames.length) {
            throw new PersismException("how would I match these?");
        }
        Util.trimArray(parentPropertyNames);
        Util.trimArray(childPropertyNames);
        // todo test these properties exist and fail otherwise

        caseSensitive = joinAnnotation.caseSensitive();

        childClass = joinAnnotation.to();
        this.parentClass = parentClass;

        parentIsAQuery = Collection.class.isAssignableFrom(parent.getClass());

        parentProperties = new ArrayList<>(MetaData.getPropertyInfo(parentClass)).stream().filter(propertyInfo -> {
            boolean found = false;
            for (int j = 0; j < parentPropertyNames.length; j++) {
                if (parentPropertyNames[j].equals(propertyInfo.propertyName)) {
                    found = true;
                    break;
                }
            }
            return found;

        }).collect(Collectors.toList());

        childProperties = new ArrayList<>(MetaData.getPropertyInfo(childClass)).stream().filter(propertyInfo -> {
            boolean found = false;
            for (int j = 0; j < childPropertyNames.length; j++) {
                if (childPropertyNames[j].equals(propertyInfo.propertyName)) {
                    found = true;
                    break;
                }
            }
            return found;

        }).collect(Collectors.toList());;

    }

    @Override
    public String toString() {
        return "JoinInfo{" +
                "parentPropertyNames=" + Arrays.toString(parentPropertyNames) +
                ", childPropertyNames=" + Arrays.toString(childPropertyNames) +
                ", parentProperties=" + parentProperties +
                ", childProperties=" + childProperties +
                ", parentClass=" + parentClass +
                ", childClass=" + childClass +
                ", caseSensitive=" + caseSensitive +
                ", parentIsAQuery=" + parentIsAQuery +
                '}';
    }
}
