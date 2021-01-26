package net.sf.persism;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Persism uses information from this class to detect which properties are modified in your data objects and only
 * includes those columns in the SQL UPDATE statements. Data objects can optionally inherit from this class.
 *
 * @author Dan Howard
 * @since 9/15/11 7:14 AM
 */
public abstract class PersistableObject<T> implements Persistable<T> {

    private T originalValue = null;

    public final void saveReadState() throws PersismException {
        originalValue = clone();
    }

    public final T getOriginalValue() {
        return originalValue;
    }

    /**
     * Used for getting originalValue
     * @return Clone of T
     */
    @Override
    public final T clone()  {
        try {
            return (T)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }
}
