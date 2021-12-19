package net.sf.persism;

import java.util.function.BiFunction;
import java.util.function.Function;

interface Convertable {
    void setConverter(Function<?, ?> func, String name);

    // could be used for enum? needs value, enum, return value
    default void setConverter(BiFunction<?, ?, ?> func) {
        // default does nothing...
    }
}
