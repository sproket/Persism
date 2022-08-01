package net.sf.persism;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    boolean isJoin;

    Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>(4);

    Annotation getAnnotation(Class<? extends Annotation> annotationClass) {
        return annotations.get(annotationClass);
    }

    // for collections
    boolean isJoin() {
        return isJoin;
    }

    // Convenience getter with runtime exception for functional
    Object getValue(Object object) {
        try {
            return getter.invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    void setValue(Object object, Object value) {
        try {
            if (setter != null) {
                setter.invoke(object, value);
            } else {
                field.setAccessible(true);
                field.set(object, value);
                field.setAccessible(false);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "PropertyInfo{" +
                "propertyName='" + propertyName + '\'' +
                ", getter=" + getter +
                ", setter=" + setter +
                ", annotations=" + annotations +
                '}';
    }
}
