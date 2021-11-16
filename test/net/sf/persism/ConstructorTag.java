package net.sf.persism;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * used for debugging and testing
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstructorTag {

    String value();
}
