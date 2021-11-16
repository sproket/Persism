package net.sf.persism;

import net.sf.persism.annotations.Join;

import java.util.Collection;

final class JoinInfo {

    String[] parentPropertyNames;
    String[] childPropertyNames;
    Collection<PropertyInfo> parentProperties;
    Collection<PropertyInfo> childProperties;
    Class<?> parentClass;
    Class<?> childClass;
    boolean caseSensitive;
    boolean parentIsAQuery;

    // note parent maybe a POJO or a list of POJOs
    public JoinInfo(Object parent, Class<?> parentClass, Join joinAnnotation) {
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

        parentProperties = MetaData.getPropertyInfo(parentClass);
        childProperties = MetaData.getPropertyInfo(childClass);

    }
}
