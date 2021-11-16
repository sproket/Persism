package net.sf.persism.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Join {

    Class<?> to();

    String onProperties();

    String toProperties();

    // innerorouter? or CROSS JOIN?

    // alias to use if you want to reference the child class properties in a WHERE clause. OOF FORGET IT
    //String alias() default "";

    boolean caseSensitive() default false;
}
