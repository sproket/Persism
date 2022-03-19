package net.sf.persism.annotations;

import java.lang.annotation.*;

/**
 * Indicates that the property is not mapped to a column. This annotation can appear on the class field, getter or setter.
 * Persism ignores read-only properties so this annotation is only required where you have a read/write property which does not map to any column.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD})
public @interface NotColumn {

}
