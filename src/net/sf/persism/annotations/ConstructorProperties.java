package net.sf.persism.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Copy of the ConstructorProperties from java.beans package.
 * Replicated inside Persism to remove java.desktop from the module.
 */
@Documented
@Target(CONSTRUCTOR)
@Retention(RUNTIME)
public @interface ConstructorProperties {

    String[] value();
}
