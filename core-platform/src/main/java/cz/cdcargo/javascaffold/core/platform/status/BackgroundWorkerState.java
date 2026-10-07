package cz.cdcargo.javascaffold.core.platform.status;

/**
 * Defines the possible lifecycle states of a background worker.
 */
public enum BackgroundWorkerState {

    /** The worker has been created but has not started. */
    READY,

    /** The worker is executing its operation. */
    RUNNING,

    /** The worker completed successfully. */
    DONE,

    /** The worker terminated because of a failure. */
    FAILED,

    /** The worker was cancelled before it completed successfully. */
    CANCELLED;

    /**
     * Determines whether this state ends the worker lifecycle.
     *
     * @return true for completed, failed, or cancelled states; otherwise false
     */
    public boolean isTerminal() {
        return this == DONE || this == FAILED || this == CANCELLED;
    }
}
