package cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception;

import java.io.Serial;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;

/**
 * Indicates that an authenticated caller is not permitted to perform an
 * operation or access a resource.
 *
 * The exception always uses a generic message to prevent authorization details
 * from being disclosed to callers. An underlying cause may be retained for
 * diagnostics without changing the public message.
 */
public final class AuthorizationException extends AbstractBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Authorization failed";

    /**
     * Creates an authorization exception without an underlying cause.
     */
    public AuthorizationException() {
        super(MESSAGE);
    }

    /**
     * Creates an authorization exception with an underlying cause.
     *
     * @param cause underlying cause of the authorization failure, or null if the
     *              cause is unavailable
     */
    public AuthorizationException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
