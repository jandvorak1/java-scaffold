package cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception;

import java.io.Serial;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;

/**
 * Indicates that a caller could not be authenticated.
 *
 * The exception always uses a generic message to prevent authentication details
 * from being disclosed to callers. An underlying cause may be retained for
 * diagnostics without changing the public message.
 */
public final class AuthenticationException extends AbstractBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Authentication failed";

    /**
     * Creates an authentication exception without an underlying cause.
     */
    public AuthenticationException() {
        super(MESSAGE);
    }

    /**
     * Creates an authentication exception with an underlying cause.
     *
     * @param cause underlying cause of the authentication failure, or null if the
     *              cause is unavailable
     */
    public AuthenticationException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
