package net.sf.persism;

import net.sf.persism.annotations.Join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class JoinInfo {

    private String[] parentPropertyNames;
    private String[] childPropertyNames;
    private List<PropertyInfo> parentProperties;
    private List<PropertyInfo> childProperties;
    private Class<?> parentClass;
    private Class<?> childClass;
    private final PropertyInfo joinProperty;
    private final boolean caseSensitive;
    private final boolean parentIsAQuery;
    private boolean reversed = false;

    JoinInfo(JoinInfo other) {
        parentPropertyNames = other.parentPropertyNames;
        childPropertyNames = other.childPropertyNames;
        parentProperties = other.parentProperties;
        childProperties = other.childProperties;
        parentClass = other.parentClass;
        childClass = other.childClass;
        joinProperty = other.joinProperty;
        caseSensitive = other.caseSensitive;
        parentIsAQuery = other.parentIsAQuery;
        reversed = other.reversed;
    }

    // note parent may be a POJO or a list of POJOs
    public JoinInfo(Join joinAnnotation, PropertyInfo joinProperty, Object parent, Class<?> parentClass) {
        this.joinProperty = joinProperty;
        parentPropertyNames = joinAnnotation.onProperties().split(",");
        childPropertyNames = joinAnnotation.toProperties().split(",");
        if (parentPropertyNames.length != childPropertyNames.length) {
            throw new PersismException("how would I match these?"); // todo add to Messages
        }
        Util.trimArray(parentPropertyNames);
        Util.trimArray(childPropertyNames);
        // todo test these properties exist and fail otherwise

        caseSensitive = joinAnnotation.caseSensitive();

        childClass = joinAnnotation.to();
        this.parentClass = parentClass;

        parentIsAQuery = Collection.class.isAssignableFrom(parent.getClass());

        // todo if we encapsulate PropertyInfo we MAY NOT need a defensive copy here or with childProperties
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

    public JoinInfo swapParentAndChild() {
        JoinInfo info = new JoinInfo(this);

        String[] tmpPropertyNames = info.parentPropertyNames;
        info.parentPropertyNames = info.childPropertyNames;
        info.childPropertyNames = tmpPropertyNames;

        List<PropertyInfo> tmpProperties = info.parentProperties;
        info.parentProperties = info.childProperties;
        info.childProperties = tmpProperties;

        Class<?> tmpClass = info.parentClass;
        info.parentClass = info.childClass;
        info.childClass = tmpClass;

        info.reversed = true;
        return info;
    }

    public String[] parentPropertyNames() {
        return parentPropertyNames;
    }

    public String[] childPropertyNames() {
        return childPropertyNames;
    }

    public List<PropertyInfo> parentProperties() {
        return parentProperties;
    }

    public List<PropertyInfo> childProperties() {
        return childProperties;
    }

    public Class<?> parentClass() {
        return parentClass;
    }

    public Class<?> childClass() {
        return childClass;
    }

    public boolean caseSensitive() {
        return caseSensitive;
    }

    public boolean parentIsAQuery() {
        return parentIsAQuery;
    }

    public PropertyInfo joinProperty() {
        return joinProperty;
    }

    public boolean reversed() {
        return reversed;
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
