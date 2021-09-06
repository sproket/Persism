package net.sf.persism;

import java.util.*;

/**
 * Helper Class to represent parameters to a query.
 */
public final class Parameters {

    private static final Log log = Log.getLogger(Parameters.class);

    List<Object> parameters;
    Map<String, Object> namedParameters;
    Map<String, List<Integer>> parameterMap;

    boolean okToVerify = false; // todo ? what for?
    boolean areKeys = false;
    boolean areNamed = false;

    private static final Parameters none = new Parameters();

    // Default constructor for empty parameters or for constructing a parameter list. For internal use.
    Parameters() {
        this.parameters = new ArrayList<>();
    }

    // this version is internal. The static method is the better way to call this.
    Parameters(Object... parameters) {
        this.parameters = new ArrayList<>(Arrays.asList(parameters));
    }

    Parameters(Map<String, Object> params) {
        this();
        areNamed = true;
        namedParameters = params;
    }

    /**
     * Static initializer for a new set of Parameters.
     * <pre>{@code
     *      Employee employee = session.fetch(Employee.class, sql("SELECT * FROM Employees WHERE LastName=? and FirstName=?"), params("Leverling", "Janet"));
     * }</pre>
     *
     * @param values varargs list of arbitrary parameters for a query.
     * @return new Parameters object
     */
    public static Parameters params(Object... values) {
        return new Parameters(values);
    }

    /**
     * Static initializer for named parameters.
     * <pre>{@code
     *      List<Contact> contacts;
     *      contacts = session.query(Contact.class,
     *                 where("(:firstname = @name OR :company = @name) and :lastname = @last and :city = @city and :amountOwed > @owe ORDER BY :dateAdded"),
     *                 named(Map.of("name", "Fred", "last", "Flintstone", "owe", 10, "city", "Somewhere")));
     * }</pre>
     *
     * @param nameValuePair - use Map.of("key1", value, "key2", value) etc.
     * @return new Parameters object
     */
    public static Parameters named(Map<String, Object> nameValuePair) {
        return new Parameters(nameValuePair);
    }

    public static Parameters params(Map<String, Object> nameValuePair) {
        return new Parameters(nameValuePair);
    }

    /**
     * Represents no Parameters.
     *
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

    void setParameterMap(Map<String, List<Integer>> parameterMap) {
        this.parameterMap = parameterMap;

        assert areNamed;

        // find largest index
        int max = 0;
        for (List<Integer> integers : parameterMap.values()) {
            Optional<Integer> x = integers.stream().max(Integer::compare);
            if (x.isPresent() && x.get() > max) {
                max = x.get();
            }
        }

        log.debug("MAX? " + max);
        Object[] arr = new Object[max];
        Set<String> paramsNotFound = new TreeSet<>();

        parameterMap.keySet().forEach((key) -> {
            if (namedParameters.containsKey(key)) {
                Object value = namedParameters.get(key);

                List<Integer> list = parameterMap.get(key);
                for (Integer integer : list) {
                    arr[integer - 1] = value;
                }
            } else {
                paramsNotFound.add(key);
            }
        });

        Set<String> mistypeSet = new TreeSet<>(namedParameters.keySet());
        mistypeSet.removeAll(parameterMap.keySet());

        if (paramsNotFound.size() > 0) {
            throw new PersismException(Messages.QueryParameterNamesMissingOrNotFound.message(paramsNotFound, mistypeSet));
        }
        parameters.clear();
        parameters.addAll(Arrays.asList(arr));
    }

    Object[] toArray() {
        return parameters.toArray();
    }

    /**
     * @hidden
     */
    @Override
    public String toString() {
        return "" + parameters;
    }
}
