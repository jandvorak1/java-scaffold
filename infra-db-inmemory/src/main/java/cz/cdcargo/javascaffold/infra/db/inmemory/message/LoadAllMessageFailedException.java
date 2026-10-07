package cz.cdcargo.javascaffold.infra.db.inmemory.message;

import java.io.Serial;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;

/**
 * Signals that the in-memory message-loading operation could not finish.
 *
 * The exception retains the underlying failure as its cause for diagnostics.
 */
public final class LoadAllMessageFailedException extends AbstractBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Loading all messages failed";

    /**
     * Creates an exception for a failed in-memory message-loading operation.
     *
     * @param cause failure that prevented the operation from completing
     */
    public LoadAllMessageFailedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
