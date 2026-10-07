package cz.cdcargo.javascaffold.core.platform.functional;

import java.util.Objects;

/**
 * Describes an unsuccessful operation.
 *
 * Each failure has a diagnostic message and can retain its originating
 * exception.
 *
 * @param message diagnostic message describing the failure
 * @param cause   underlying cause, or null when no cause is available
 */
public record Failure(String message, Throwable cause) {

    /**
     * Creates a failure with a message and an optional cause.
     *
     * @param message diagnostic message describing the failure
     * @param cause   underlying cause, or null when no cause is available
     * @throws NullPointerException if message is null
     */
    public Failure {
        Objects.requireNonNull(message, "Message must not be null");
    }

    /**
     * Creates a failure without an underlying cause.
     *
     * @param message diagnostic message describing the failure
     * @return a failure containing the supplied message
     * @throws NullPointerException if message is null
     */
    public static Failure of(String message) {
        return new Failure(message, null);
    }

    /**
     * Creates a failure with a message and cause.
     *
     * @param message diagnostic message describing the failure
     * @param cause   underlying cause, or null when no cause is available
     * @return a failure containing the supplied message and cause
     * @throws NullPointerException if message is null
     */
    public static Failure of(String message, Throwable cause) {
        return new Failure(message, cause);
    }

    /**
     * Creates a failure from an exception.
     *
     * The fully qualified exception class name is used when the exception has no
     * message.
     *
     * @param cause underlying cause
     * @return a failure containing the supplied cause and a derived message
     * @throws NullPointerException if cause is null
     */
    public static Failure from(Throwable cause) {
        var validCause = Objects.requireNonNull(cause, "Cause must not be null");
        var message = Objects.requireNonNullElse(validCause.getMessage(), validCause.getClass().getName());
        return new Failure(message, validCause);
    }
}
