package cz.cdcargo.javascaffold.core.platform.status;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes a single asynchronous operation and exposes its lifecycle, result,
 * failure, and progress.
 *
 * Completion, failure, and progress callbacks run on the task thread.
 * Cancellation callbacks run on the thread that requests cancellation.
 *
 * @param <T> the background task result type
 * @param <V> the progress message type
 */
public abstract class BackgroundWorker<T, V> {

    private static final Logger LOGGER = Logger.getLogger(BackgroundWorker.class.getName());
    private static final ExecutorService SHARED_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final Clock clock;
    private final String token;
    private final List<V> messages = new ArrayList<>();
    private final Instant createdAt;

    private volatile BackgroundWorkerState state = BackgroundWorkerState.READY;
    private volatile Instant finishedAt;
    private volatile Exception error;
    private volatile T result;

    private Future<T> future;

    /**
     * Creates a worker that records timestamps with the system UTC clock.
     *
     * @param token unique non-blank worker token
     * @throws NullPointerException     if token is null
     * @throws IllegalArgumentException if token is blank
     */
    protected BackgroundWorker(String token) {
        this(token, Clock.systemUTC());
    }

    /**
     * Creates a worker that records timestamps with the supplied clock.
     *
     * @param token unique non-blank worker token
     * @param clock clock used to create lifecycle timestamps
     * @throws NullPointerException     if token or clock is null
     * @throws IllegalArgumentException if token is blank
     */
    protected BackgroundWorker(String token, Clock clock) {
        this.token = requireToken(token);
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
        this.createdAt = clock.instant();
    }

    /**
     * Performs the operation after the worker has been started.
     *
     * @return the completed task result
     * @throws Exception if the background work fails
     */
    protected abstract T doInBackground() throws Exception;

    /**
     * Handles an immutable batch of progress messages published by the operation.
     *
     * @param chunks an immutable batch of progress messages
     */
    protected void process(List<V> chunks) {
    }

    /**
     * Handles the result after the operation completes successfully.
     *
     * @param result the completed task result
     */
    protected void done(T result) {
    }

    /**
     * Handles a failure after the worker enters the failed state.
     *
     * @param cause the task failure
     */
    protected void failed(Exception cause) {
    }

    /**
     * Handles notification after the worker has been cancelled.
     */
    protected void cancelled() {
    }

    /**
     * Handles notification immediately before the operation thread is interrupted.
     */
    protected void onCancelRequested() {
    }

    /**
     * Starts the operation on the shared executor.
     *
     * @throws IllegalStateException if this worker has already been started
     */
    public synchronized void execute() {
        if (state != BackgroundWorkerState.READY) {
            throw new IllegalStateException("Background worker can only be executed once");
        }
        state = BackgroundWorkerState.RUNNING;
        future = SHARED_EXECUTOR.submit(() -> {
            T backgroundResult;
            try {
                backgroundResult = doInBackground();
            } catch (Exception e) {
                synchronized (this) {
                    if (state == BackgroundWorkerState.CANCELLED) {
                        return null;
                    }
                    state = BackgroundWorkerState.FAILED;
                    finishedAt = clock.instant();
                    error = e;
                }
                LOGGER.log(Level.SEVERE, "Background worker failed", e);
                failed(e);
                return null;
            }
            synchronized (this) {
                if (state != BackgroundWorkerState.RUNNING) {
                    return backgroundResult;
                }
                result = backgroundResult;
                state = BackgroundWorkerState.DONE;
                finishedAt = clock.instant();
            }
            done(backgroundResult);
            return backgroundResult;
        });
    }

    /**
     * Requests cancellation of the running operation and invokes the cancellation
     * callbacks.
     */
    public void cancel() {
        Future<T> currentFuture;
        synchronized (this) {
            if (state != BackgroundWorkerState.RUNNING) {
                return;
            }
            state = BackgroundWorkerState.CANCELLED;
            finishedAt = clock.instant();
            currentFuture = future;
        }
        try {
            onCancelRequested();
        } finally {
            currentFuture.cancel(true);
            cancelled();
        }
    }

    /**
     * Returns the unique token assigned to this worker.
     *
     * @return the worker token
     */
    public final String token() {
        return token;
    }

    /**
     * Returns the current lifecycle state.
     *
     * @return the current worker state
     */
    public final BackgroundWorkerState state() {
        return state;
    }

    /**
     * Determines whether the operation is currently running.
     *
     * @return true when the worker is running
     */
    public final boolean running() {
        return state == BackgroundWorkerState.RUNNING;
    }

    /**
     * Determines whether the worker has reached a terminal state.
     *
     * @return true when the task completed, failed, or was cancelled
     */
    public final boolean finished() {
        return state.isTerminal();
    }

    /**
     * Publishes a batch of progress updates and forwards it to the progress
     * callback.
     *
     * @param values the progress updates to publish
     * @throws NullPointerException if values or any contained value is null
     */
    protected final void publish(List<V> values) {
        var batch = List.copyOf(values);
        synchronized (this) {
            if (state != BackgroundWorkerState.RUNNING) {
                return;
            }
            messages.addAll(batch);
        }
        process(batch);
    }

    /**
     * Publishes one progress update to the worker.
     *
     * @param value the progress update to publish
     * @throws NullPointerException if value is null
     */
    protected final void publish(V value) {
        publish(List.of(value));
    }

    /**
     * Creates a snapshot containing the current state and messages starting at an
     * offset.
     *
     * @param offset the zero-based message index from which to include updates
     * @return a snapshot of the worker at the time of invocation
     * @throws IllegalArgumentException if offset is negative
     */
    public final synchronized BackgroundWorkerSnapshot<T, V> snapshot(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }
        var messageList = offset >= messages.size()
                ? List.<V>of()
                : List.copyOf(messages.subList(offset, messages.size()));
        return new BackgroundWorkerSnapshot<>(token, state, createdAt, finishedAt, messages.size(), messageList, error,
                result);
    }

    private static String requireToken(String token) {
        Objects.requireNonNull(token, "Token must not be null");
        if (token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }
        return token;
    }
}
