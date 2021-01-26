package net.sf.persism.annotations;

/*
 * $RCSfile: $
 * $Revision: $
 * $Date: $
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/8/11
 * Time: 6:18 AM
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the property is not mapped to a column. This annotation can appear on the class field, getter or setter.
 * Persism ignores read-only properties so this annotation is only required where you have a read/write property which does not map to any column.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD})
public @interface NotColumn {

}
