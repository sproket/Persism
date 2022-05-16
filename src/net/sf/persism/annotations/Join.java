package net.sf.persism.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the property value is set by joining from another table (pojo class). You may use this on a
 * single object (one to one, many to one), or on a collection (one to many, many to many). If joining to a
 * collection, make sure that you instantiate it as a modifiable collection. Persism will not do that fo you.
 * @see <a href="https://sproket.github.io/Persism/join.html">How to use the new @Join Annotation</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Join {

    /**
     * Class of the POJO that this class joins to.
     * @return target POJO Class
     */
    Class<?> to();

    /**
     * Comma seperated property names whose values are used to join to the target table.
     * @return property names
     */
    String onProperties();

    /**
     * Comma seperated property names whose values are used to join to the parent table.
     * @return property names
     */
    String toProperties();

    /**
     * Indicates that you use String primary/foreign key values in your DB which are case-sensitive. Default false.
     * @return case-sensitive
     */
    boolean caseSensitive() default false;
}
