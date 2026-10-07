package cz.cdcargo.javascaffold.core.platform.exception;

import java.io.Serial;

/**
 * Provides the common base type for unchecked exceptions caused by a business
 * rule violation.
 *
 * Subclasses identify individual business failures while retaining the standard
 * exception message and cause semantics defined by RuntimeException.
 */
public abstract class AbstractBusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with no detail message or cause.
     */
    protected AbstractBusinessException() {
        super();
    }

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message detail message describing the business rule violation, or
     *                null if no detail message is available
     */
    protected AbstractBusinessException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified cause. Its detail message is the
     * string representation of the cause, or null when the cause is null.
     *
     * @param cause underlying cause of the business rule violation, or null if
     *              the cause is unknown or unavailable
     */
    protected AbstractBusinessException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an exception with the specified detail message and cause.
     *
     * @param message detail message describing the business rule violation, or
     *                null if no detail message is available
     * @param cause   underlying cause of the business rule violation, or null if
     *                the cause is unknown or unavailable
     */
    protected AbstractBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
