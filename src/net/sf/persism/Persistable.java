package net.sf.persism;

/**
 * Indicates that Persism should keep track of changed columns in order to only include those columns in the SQL UPDATE statement.
 * You can use this interface in situations where you can't or don't want your data objects to inherit PersistableObject.
 *
 * @author Dan Howard
 * @see PersistableObject PersistableObject for implementation.
 * @since 10/8/11 9:51 AM
 */
public interface Persistable<T> extends Cloneable {


    /**
     * Saves the current state of the data object to later detect changes for SQL UPDATE statements.
     * Persism calls this method internally, you usually don't have to call this method yourself.
     *
     * @throws PersismException If an SQL or other exception occurs.
     * @see PersistableObject for example implementation
     */
    void saveReadState() throws PersismException;

    /**
     * Getter (but not a Getter) for the data object in it's original state. The state at the time it was read from the database.
     *
     * @return The data object in it's original state.
     * @see PersistableObject for example implementation
     */
    T readOriginalValue();
}
