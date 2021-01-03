package net.sf.persism;

/**
 * General RuntimeException used by Persism. Note that Persism will rollback transactions if the exception is an SQLException.
 *
 * @author Dan Howard
 * @since 9/8/11 6:41 AM
 */
public class PersismException extends RuntimeException {

    private static final long serialVersionUID = 3629404706918664936L;

    public PersismException() {
    }

    public PersismException(String message) {
        super(message);
    }

    public PersismException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersismException(Throwable cause) {
        super(cause);
    }
}
