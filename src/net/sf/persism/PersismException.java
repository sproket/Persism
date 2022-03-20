package net.sf.persism;

/**
 * General RuntimeException used by Persism. Note that Persism will rollback transactions if the exception is an SQLException.
 *
 * @author Dan Howard
 * @since 9/8/11 6:41 AM
 *
 */
public final class PersismException extends RuntimeException {

    private static final long serialVersionUID = 3629404706918664936L;

    private PersismException() {
    }

    /**
     * PersismException with a message
     * @param message message string
     */
    public PersismException(String message) {
        this(message, new Throwable(message));
    }

    /**
     * PersismException with a message and a cause
     * @param message message string
     * @param cause Throwable cause
     */
    public PersismException(String message, Throwable cause) {
        super(message, cause);
    }

}
