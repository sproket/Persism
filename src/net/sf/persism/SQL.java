package net.sf.persism;

/**
 * Simple wrapper for SQL String. Mainly to allow for overloads to fetch/query methods.
 * @see <a href="https://sproket.github.io/Persism/manual.html">Using the new Query/Fetch methods</a>
 * todo wrong link for now....
 */
public final class SQL {

    private final String sql;

    boolean whereOnly; // flags this as WHERE only - we add the SELECT part.
    boolean storedProc; // indicates this is a stored proc rather than an SQL statement

    SQL(String sql) {
        this.sql = sql;
    }

    /**
     * Static initializer for a new SQL string.
     * <pre>{@code
     *      Contact> contact;
     *      contact = session.fetch(Contact.class,
     *                sql("SELECT * FROM CONTACTS WHERE LAST_NAME = ? AND FIRST_NAME = ?"),
     *                params("Fred", "Flintstone");
     * }</pre>
     *
     * @param sql String
     * @return new SQL object
     */
    public static SQL sql(String sql) {
        return new SQL(sql);
    }

    /**
     * Convenience method used to specify an SQL WHERE clause for an SQL Statement.
     * The SELECT ... parts would be provided by Persism.
     * <pre>{@code
     *      List<Contact> contacts;
     *      contacts = session.query(Contact.class,
     *                 where("(:firstname = @name OR :company = @name) and :lastname = @last and :city = @city and :amountOwed > @owe ORDER BY :dateAdded"),
     *                 named(Map.of("name", "Fred", "last", "Flintstone", "owe", 10, "city", "Somewhere")));
     * }</pre>
     *
     * @param where String
     * @return new SQL object
     */
    public static SQL where(String where) {
        SQL sql = new SQL(" WHERE " + where);
        sql.whereOnly = true;
        return sql;
    }

    /**
     * Static initializer for a new SQL stored procedure string.
     * <pre>{@code
     *      List<CustomerOrder> list;
     *      list = session.query(CustomerOrder.class, proc("[spCustomerOrders](?)"), params("123"));
     * }</pre>
     *
     * @param storedProc String
     * @return new SQL object
     */
    public static SQL proc(String storedProc) {
        SQL sql = new SQL(storedProc);
        sql.storedProc = true;
        return sql;
    }

    /**
     * @hidden
     */
    @Override
    public String toString() {
        return sql;
    }
}
