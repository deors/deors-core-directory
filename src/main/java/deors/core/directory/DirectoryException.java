package deors.core.directory;

/**
 * Generic exception for the Directory connection manager.
 *
 * @author deors
 * @version 1.0
 */
public class DirectoryException
    extends Exception {

    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = -7154775367885357033L;

    /**
     * Exception constructor.
     *
     * @param message the error message
     */
    public DirectoryException(String message) {
        super(message);
    }

    /**
     * Exception constructor.
     *
     * @param message the error message
     * @param rootCause the root cause
     */
    public DirectoryException(String message, Exception rootCause) {
        super(message, rootCause);
    }
}
