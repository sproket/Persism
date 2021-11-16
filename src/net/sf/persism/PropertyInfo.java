package net.sf.persism;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/8/11
 * Time: 8:09 AM
 */
final class PropertyInfo {

    String propertyName;
    Method getter;
    Method setter;
    Field field;
    boolean readOnly;
    boolean isJoin;

    Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>(4);

    Annotation getAnnotation(Class<? extends Annotation> annotationClass) {
        return annotations.get(annotationClass);
    }

    // for collections
    String propertyName() {
        return propertyName;
    }

    Method getter() {
        return getter;
    }

    Method setter() {
        return setter;
    }

    Field field() {
        return field;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    boolean isJoin() {
        return isJoin;
    }

    Map<Class<? extends Annotation>, Annotation> annotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return "PropertyInfo{" +
                "propertyName='" + propertyName + '\'' +
                ", getter=" + getter +
                ", setter=" + setter +
                ", annotations=" + annotations +
                ", readOnly=" + readOnly +
                '}';
    }
}
