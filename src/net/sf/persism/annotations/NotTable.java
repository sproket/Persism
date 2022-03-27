package net.sf.persism.annotations;

/*
 * Created by IntelliJ IDEA.
 * User: DHoward
 * Date: 9/14/11
 * Time: 7:38 AM
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the class represents the result of query instead of a table.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NotTable {

}
