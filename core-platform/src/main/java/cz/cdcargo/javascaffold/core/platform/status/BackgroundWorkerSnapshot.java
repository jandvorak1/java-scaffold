package cz.cdcargo.javascaffold.core.platform.status;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents an immutable, internally consistent view of a background worker.
 *
 * Progress messages are copied when the snapshot is created.
 *
 * @param <T>           the background worker result type
 * @param <V>           the progress message type
 * @param token         the worker token
 * @param state         the worker state when the snapshot was created
 * @param createdAt     the worker creation timestamp
 * @param finishedAt    the completion timestamp, or null while unfinished
 * @param messageOffset the total number of published messages, which cannot be
 *                      smaller than the number of included messages
 * @param messages      the progress messages included in the snapshot
 * @param error         the task failure, or null when no failure occurred
 * @param result        the task result, or null when no result is available
 */
public record BackgroundWorkerSnapshot<T, V>(String token, BackgroundWorkerState state, Instant createdAt,
        Instant finishedAt, int messageOffset, List<V> messages, Exception error, T result) {

    /**
     * Validates lifecycle data and copies the progress messages.
     *
     * @throws NullPointerException     if token, state, createdAt, messages, or a
     *                                  contained message is null
     * @throws IllegalArgumentException if the token is blank, message metadata
     *                                  is invalid, or lifecycle data does not match
     *                                  the state
     */
    public BackgroundWorkerSnapshot {
        Objects.requireNonNull(token, "Token must not be null");
        if (token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }
        Objects.requireNonNull(state, "State must not be null");
        Objects.requireNonNull(createdAt, "Created at must not be null");
        Objects.requireNonNull(messages, "Messages must not be null");
        messages = List.copyOf(messages);
        if (messageOffset < 0) {
            throw new IllegalArgumentException("Message offset must not be negative");
        }
        if (messageOffset < messages.size()) {
            throw new IllegalArgumentException("Message offset must not be smaller than messages size");
        }
        if (state.isTerminal() != (finishedAt != null)) {
            throw new IllegalArgumentException("Finished at must match the worker terminal state");
        }
        if (finishedAt != null && finishedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Finished at must not precede created at");
        }
        if ((state == BackgroundWorkerState.FAILED) != (error != null)) {
            throw new IllegalArgumentException("Error must be present only for failed workers");
        }
        if (state != BackgroundWorkerState.DONE && result != null) {
            throw new IllegalArgumentException("Result must be present only for completed workers");
        }
    }
}
