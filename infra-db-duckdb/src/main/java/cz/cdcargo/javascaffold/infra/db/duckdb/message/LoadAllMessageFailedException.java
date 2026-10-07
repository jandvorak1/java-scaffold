package cz.cdcargo.javascaffold.infra.db.duckdb.message;

import java.io.Serial;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;

/**
 * Indicates that scaffold messages could not be prepared or loaded from
 * DuckDB.
 *
 * The original failure is retained as the cause for diagnostic purposes.
 */
public final class LoadAllMessageFailedException extends AbstractBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Loading all messages failed";

    /**
     * Creates an exception for a failed scaffold message operation.
     *
     * @param cause failure that prevented the messages from being prepared or
     *              loaded
     */
    public LoadAllMessageFailedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
