package cz.cdcargo.javascaffold.core.platform.status;

import java.util.Objects;
import java.util.concurrent.CancellationException;

/**
 * Supports cooperative cancellation through a token registered for the current
 * thread.
 *
 * Child and worker threads do not inherit registrations. Clear a registration
 * when its operation finishes.
 */
public final class CancellationToken {

    private static final ThreadLocal<CancellationToken> CURRENT = new ThreadLocal<>();
    private volatile boolean cancelled;

    /**
     * Registers a token for the current thread, replacing an existing registration.
     *
     * @param token the token to associate with the current thread
     * @throws NullPointerException if token is null
     */
    public static void register(CancellationToken token) {
        CURRENT.set(Objects.requireNonNull(token, "Token must not be null"));
    }

    /**
     * Removes the token registered for the current thread.
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * Throws an exception when the token registered for the current thread is
     * cancelled.
     *
     * @throws CancellationException if the current thread's token is cancelled
     */
    public static void throwIfCancelled() throws CancellationException {
        var token = CURRENT.get();
        if (token != null && token.cancelled) {
            throw new CancellationException("Task has been cancelled");
        }
    }

    /**
     * Requests cancellation through this token.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Determines whether this token has been cancelled.
     *
     * @return true when cancellation has been requested, otherwise false
     */
    public boolean isCancelled() {
        return cancelled;
    }
}
