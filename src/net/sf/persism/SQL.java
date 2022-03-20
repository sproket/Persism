package net.sf.persism;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Simple wrapper for SQL String. Mainly to allow for overloads to fetch/query methods.
 *
 * @see <a href="https://sproket.github.io/Persism/manual2.html">Using the new Query/Fetch methods</a>
 */
public final class SQL {

    final String sql;

    boolean whereOnly; // flags this as WHERE only - we add the SELECT part.
    boolean storedProc; // indicates this is a stored proc rather than an SQL statement

    String processedSQL = null;

    SQL(String sql) {
        sql = sql.trim();

        if (sql.startsWith("--") || sql.startsWith("/*")) {
            // trim comments

            // line comments
            StringBuilder sb = new StringBuilder();
            try (Scanner scanner = new Scanner(sql)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.startsWith("--")) {
                        sb.append(line).append("\n");
                    }
                }
            }
            sql = sb.toString();

            // /* */ comments
            Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
            sql = commentPattern.matcher(sql).replaceAll("").trim();
        }

        this.sql = sql;
    }

    /**
     * Method to instantiate a regular SQL string.
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
     * Method used to specify an SQL WHERE clause for an SQL Statement.
     * The SELECT ... parts would be provided by Persism.
     * Only here do we allow property names in the query
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
        if (processedSQL != null) {
            return processedSQL;
        }
        return sql;
    }
}
