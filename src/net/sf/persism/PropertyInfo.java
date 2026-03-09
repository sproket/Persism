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


//    todo IllegalArgumentException is a different type from the field type
//    todo IllegalAccessException or module related or setting on a final field in a record
//    todo InvocationTargetException the setter call where there's an exception in the setter method
//    todo for getter should we have a field get if there's no Getter?


    // Convenience getter with runtime exception for functional
    Object getValue(Object object) {
        try {
            if (getter != null) {
                return getter.invoke(object);
            } else {
                throw new PersismException(Message.MissingGetter.message(propertyName));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PersismException(e.getMessage() + "(" + this.propertyName + ")", e);
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
            // don't catch IllegalArgumentException - this is handled by reader to translate to human readable message
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
