package net.sf.persism;

/**
 * Simple wrapper for SQL String. Mainly to allow for overloads to fetch methods. Stupid ... feature!
 */
public final class SQL {

    private final String sql;

    boolean whereOnly; // flags this as WHERE only - we add the SELECT part.
    boolean storedProc; // not sure I'll need this
    boolean knownSQL; // not sure I'll need this

    SQL(String sql) {
        this.sql = sql;
    }

    /**
     * Static initializer for a new SQL string
     * @param sql String
     * @return new SQL object
     */
    public static SQL sql(String sql) {
        return new SQL(sql);
    }

    /**
     * Static initializer for a new SQL stored procedure string
     * @param storedProc String
     * @return new SQL object
     */
    public static SQL proc(String storedProc) {
        SQL sql = new SQL(storedProc);
        sql.storedProc = true;
        return sql;
    }

    /**
     * Convenience method used to specify an SQL WHERE clause for an SQL Statement.
     * The SELECT ... parts would be provided by Persism.
     * @param where String
     * @return new SQL object
     */
    public static SQL where(String where) {
        SQL sql = new SQL(where);
        sql.whereOnly = true;
        return sql;
    }

//    public SQL orderBy(String orderBy) {
//        return new SQL(this.sql + " ORDER BY " + orderBy);
//    }

    @Override
    public String toString() {
        return sql;
    }
}
