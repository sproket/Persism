package net.sf.persism;

/**
 * Result of an insert
 */
public final class Result<T> {
    private final int rows;
    private final T dataObject;

    /**
     *
     * @param rows rows affected
     * @param dataObject possibly changed object after an insert. Use for Records which are immutable.
     */
    public Result(int rows, T dataObject) {
        this.rows = rows;
        this.dataObject = dataObject;
    }

    /**
     * Return value from jdbc statement getUpdateCount after an insert.
     * @return row count changed
     */
    public int rows() {
        return rows;
    }

    /**
     * Instance of the possibly modified object after insert.
     * @return dataObject of type T
     */
    public T dataObject() {
        return dataObject;
    }
}
