package net.sf.persism;

import net.sf.persism.annotations.NotColumn;

/**
 * Persism uses information from this class to detect which properties are modified in your data objects and only
 * includes those columns in the SQL UPDATE statements. Data objects can optionally inherit from this class.
 *
 * @author Dan Howard
 * @since 9/15/11 7:14 AM
 */
public abstract class PersistableObject<T> implements Persistable<T> {

    @NotColumn
    private T persismOriginalValue = null;

    @Override
    public final void saveReadState() throws PersismException {
        persismOriginalValue = clone();
    }

    @Override
    public final T readOriginalValue() {
        return persismOriginalValue;
    }

    /**
     * Used by saveReadState for persismOriginalValue
     *
     * @return Clone of T
     */
    @Override
    public final T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }
}
