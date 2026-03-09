package net.sf.persism;

import java.util.Arrays;
import java.util.Objects;

// wrapper class for when you join on multiple columns
final class KeyBox {

    final Object[] keyValues;

    KeyBox(boolean caseSensitive, Object... keyValues) {
        this.keyValues = keyValues;

        // If not case-sensitive then make String values upper case
        if (!caseSensitive) {
            for (int j = 0; j < keyValues.length; j++) {
                if (keyValues[j] instanceof String s) {
                    s = s.toUpperCase();
                    this.keyValues[j] = s;
                }
            }
        }
    }

    public boolean isAllNull() {
        return Arrays.stream(keyValues).allMatch(Objects::isNull);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KeyBox keyBox = (KeyBox) o;
        return Arrays.equals(keyValues, keyBox.keyValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyValues);
    }

    @Override
    public String toString() {
        return "KeyBox{" +
                "keyValues=" + Arrays.toString(keyValues) +
                '}';
    }
}
