package net.sf.persism.annotations;

/*
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
 * Optional annotation defining a column mapping for a property on the class. This annotation can appear on the class field, getter or setter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Column {

    /**
     * Name of the column mapped to the property. Used when the property cannot be auto-mapped to a table column.
     * <p>
     * It's only required if Persism cannot discover the column/property mapping on its own.
     *
     * @return Name of the column mapped to the property.
     */
    String name() default "";

    /**
     * Indicates if the column is an auto increment field.
     * This will tell Persism to exclude this column in insert statements and to
     * update the object with this column value after the insert.
     * <p>
     * It's only required if Persism cannot detect this column attribute on its own.
     * The only case for this seems to be Oracle before version 12. To maybe be deprecated later on.
     *
     * @return true if this column is autoIncrement.
     */
    @Deprecated
    boolean autoIncrement() default false;

    /**
     * Indicates that this column is a primary key.
     * <p>
     * It's only required if Persism cannot detect this column attribute on its own.
     *
     * @return true if this column is a primary key.
     */
    boolean primary() default false;

    /**
     * Indicates that this column has a default value in the database. This tells Persism that if the data object
     * did not specify a value then Persism will update the data object with the default after an insert.
     * <p>
     * It's only required if Persism cannot detect this column attribute on its own.
     *
     * @return true if this column has a default value in the database.
     */
    boolean hasDefault() default false;

}
