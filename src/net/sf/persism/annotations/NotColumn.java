package net.sf.persism.annotations;

import java.lang.annotation.*;

/**
 * Indicates that the property is not mapped to a column. This annotation can appear on the class field, getter or setter.
 * It works the same if you use the transient keyword on a field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD})
public @interface NotColumn {

}
