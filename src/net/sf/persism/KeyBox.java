package net.sf.persism;

import java.util.Arrays;

// wrapper class for when you join on multiple columns
public final class KeyBox {

    Object[] keyValues;

    public KeyBox(Object[] keyValues, boolean caseSensitive) {
        this.keyValues = keyValues;

        // If not case-sensitive then make String values upper case
        if (!caseSensitive) {
            for (int j = 0; j < keyValues.length; j++) {
                if (keyValues[j] instanceof String s) {
                    s = s.toUpperCase();
                    keyValues[j] = s;
                }
            }
        }
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
}
