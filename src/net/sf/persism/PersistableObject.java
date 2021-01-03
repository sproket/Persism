package net.sf.persism;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Data objects can optionally inherit from this class. Persism uses information from this class to detect which
 * properties are modified in your data objects and only includes those columns in the SQL UPDATE statements.
 *
 * @author Dan Howard
 * @since 9/15/11 7:14 AM
 */
public abstract class PersistableObject implements Persistable {

    Persistable originalValue = null;

    public final void saveReadState() throws PersismException {
        try {
            Collection<PropertyInfo> properties = MetaData.getPropertyInfo(getClass());
            originalValue = getClass().newInstance();
            for (PropertyInfo propertyInfo : properties) {

                // It's possible to have a read-only property in a class. We just ignore those
                if (propertyInfo.setter != null) {
                    propertyInfo.setter.invoke(originalValue, propertyInfo.getter.invoke(this));
                }
            }
        } catch (Exception e) {
            throw new PersismException(e);
        }
    }

    public final Persistable getOriginalValue() {
        return originalValue;
    }
}
