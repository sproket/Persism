package net.sf.persism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper Class to represent parameters to a query.
 */
public final class Parameters {
    List<Object> parameters;

    private static final Parameters none = new Parameters();

    // Default constructor for empty parameters or for constructing a parameter list. For internal use.
    Parameters() {
        this.parameters = new ArrayList<>();
    }

    // this version is internal. The static method is the better way to call this.
    Parameters(Object... parameters) {
        this.parameters = new ArrayList<>(Arrays.asList(parameters));
    }

    /**
     * Static initializer for a new set of Parameters
     * @param values varargs list of arbitrary parameters for a query.
     * @return new Parameters object
     */
    public static Parameters params(Object... values) {
        return new Parameters(values);
    }

    /**
     * Represents no Parameters
     * @return empty array instance of Parameters
     */
    public static Parameters none() {
        return none;
    }

    int size() {
        return parameters.size();
    }

    void add(Object parameter) {
        parameters.add(parameter);
    }

    Object get(int index) {
        return parameters.get(index);
    }

    void set(int index, Object parameter) {
        parameters.set(index, parameter);
    }

    /**
     * Format of Parameters suitable for passing to a JDBC statement.
     * @return Object array of parameter values.
     */
    Object[] toArray() {
        return parameters.toArray();
    }

    @Override
    public String toString() {
        return "" + parameters;
    }
}
