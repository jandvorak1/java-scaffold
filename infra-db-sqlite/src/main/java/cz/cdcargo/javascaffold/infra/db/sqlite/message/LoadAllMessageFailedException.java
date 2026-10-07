package cz.cdcargo.javascaffold.infra.db.sqlite.message;

import java.io.Serial;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;

/**
 * Signals that the SQLite message-loading operation could not complete.
 *
 * The underlying failure is retained as the cause for diagnostics.
 */
public final class LoadAllMessageFailedException extends AbstractBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Loading all messages failed";

    /**
     * Creates an exception for a failed SQLite message-loading operation.
     *
     * @param cause failure that prevented the operation from completing
     */
    public LoadAllMessageFailedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
