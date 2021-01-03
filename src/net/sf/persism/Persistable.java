package net.sf.persism;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Interface for PersistableObject class. You can use this interface in situations where you can't or don't want
 * your data objects to inherit PersistableObject.
 *
 * @author Dan Howard
 * @see PersistableObject PersistableObject for implementation.
 * @since 10/8/11 9:51 AM
 */
public interface Persistable {


    /**
     * Saves the current state of the data object to later detect changes for SQL UPDATE statements.
     * Persism calls this method internally, you usually don't have to call this method yourself.
     * @see PersistableObject for example implementation
     * @throws PersismException If an SQL or other exception occurs.
     */
    void saveReadState() throws PersismException;

    /**
     * Getter for the data object in it's original state. The state at the time it was read from the database.
     *
     * @see PersistableObject for example implementation
     * @return The data object in it's original state.
     */
    Persistable getOriginalValue();
}
