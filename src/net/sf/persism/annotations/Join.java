package net.sf.persism.annotations;

import net.sf.persism.Parameters;

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

    //TODO Child where clause to apply
    String where() default "";

    String alias() default "";

    boolean caseSensitive() default false;
}
