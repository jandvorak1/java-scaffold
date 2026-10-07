package cz.cdcargo.javascaffold.core.platform.status;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores background workers by token and manages the retention of terminal
 * workers.
 *
 * The registry is thread-safe. Terminal workers remain registered for the
 * configured duration and are removed during registry operations or when
 * cleanup is requested explicitly.
 */
public final class BackgroundWorkerRegistry {

    private final Map<String, BackgroundWorker<?, ?>> workers = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration finishedWorkerRetention;

    /**
     * Creates a registry with the system UTC clock and a fifteen-minute retention
     * period.
     */
    public BackgroundWorkerRegistry() {
        this(Clock.systemUTC(), Duration.ofMinutes(15));
    }

    /**
     * Creates a registry with the supplied clock and terminal-worker retention
     * period.
     *
     * @param clock                   the clock used to evaluate worker expiration
     * @param finishedWorkerRetention the time completed workers remain registered
     * @throws NullPointerException     if clock or finishedWorkerRetention is null
     * @throws IllegalArgumentException if finishedWorkerRetention is negative
     */
    public BackgroundWorkerRegistry(Clock clock, Duration finishedWorkerRetention) {
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
        this.finishedWorkerRetention = Objects.requireNonNull(finishedWorkerRetention,
                "Finished worker retention must not be null");
        if (finishedWorkerRetention.isNegative()) {
            throw new IllegalArgumentException("Finished worker retention must not be negative");
        }
    }

    /**
     * Registers a worker by its token.
     *
     * @param worker the worker to register
     * @throws NullPointerException  if worker is null
     * @throws IllegalStateException if a worker with the same token is already
     *                               registered
     */
    public void add(BackgroundWorker<?, ?> worker) {
        Objects.requireNonNull(worker, "Worker must not be null");
        removeExpired();
        var previous = workers.putIfAbsent(worker.token(), worker);
        if (previous != null) {
            throw new IllegalStateException("Background worker already exists for current token");
        }
    }

    /**
     * Returns the worker registered for a token.
     *
     * @param token the worker token
     * @return the registered worker
     * @throws NullPointerException   if token is null
     * @throws NoSuchElementException if no worker is registered for token
     */
    public BackgroundWorker<?, ?> get(String token) {
        var validToken = Objects.requireNonNull(token, "Token must not be null");
        removeExpired();
        var worker = workers.get(validToken);
        if (worker == null) {
            throw new NoSuchElementException("Background worker not found for current token");
        }
        return worker;
    }

    /**
     * Requests cancellation of the worker registered for a token.
     *
     * @param token the worker token
     * @throws NullPointerException   if token is null
     * @throws NoSuchElementException if no worker is registered for token
     */
    public void cancel(String token) {
        get(token).cancel();
    }

    /**
     * Removes the worker registered for a token without cancelling it.
     *
     * @param token the worker token
     * @throws NullPointerException if token is null
     */
    public void remove(String token) {
        workers.remove(Objects.requireNonNull(token, "Token must not be null"));
    }

    /**
     * Removes terminal workers whose retention period has elapsed.
     */
    public void removeExpired() {
        var now = clock.instant();
        workers.entrySet().removeIf(entry -> {
            var snapshot = entry.getValue().snapshot(0);
            if (snapshot.finishedAt() == null) {
                return false;
            }
            if (now.isBefore(snapshot.finishedAt())) {
                return false;
            }
            return Duration.between(snapshot.finishedAt(), now).compareTo(finishedWorkerRetention) >= 0;
        });
    }
}
