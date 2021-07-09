package net.sf.persism.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates this class represents a view. You may optionally indicate the view name if Persism cannot find the view name on it's own.
 */

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface View {
    /**
     * View Name
     * @return View Name
     */
    String value() default "";
}
