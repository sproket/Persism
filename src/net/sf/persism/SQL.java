package net.sf.persism;

/**
 * Simple wrapper for SQL String. Mainly to allow for overloads to fetch methods. Stupid ... feature!
 */
public final class SQL {

    private final String sql;

    SQL(String sql) {
        this.sql = sql;
    }

    public static SQL sql(String sql) {
        return new SQL(sql);
    }

    @Override
    public String toString() {
        return sql;
    }
}
