package cz.cdcargo.javascaffold.core.platform.status;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Delivers text status messages to a consumer registered for the current
 * thread.
 *
 * Other threads do not inherit the registration. Messages sent without a
 * registered consumer are ignored.
 */
public final class TextStatusChannel {

    private static final ThreadLocal<Consumer<String>> HANDLER = new ThreadLocal<>();

    private TextStatusChannel() {
    }

    /**
     * Registers a message consumer for the current thread, replacing an existing
     * registration.
     *
     * @param handler the handler that receives messages sent by the current thread
     * @throws NullPointerException if handler is null
     */
    public static void register(Consumer<String> handler) {
        HANDLER.set(Objects.requireNonNull(handler, "Handler must not be null"));
    }

    /**
     * Removes the consumer registered for the current thread.
     */
    public static void clear() {
        HANDLER.remove();
    }

    /**
     * Delivers a message to the consumer registered for the current thread.
     *
     * @param message text message to deliver
     * @throws NullPointerException if message is null
     */
    public static void send(String message) {
        Objects.requireNonNull(message, "Message must not be null");
        var handler = HANDLER.get();
        if (handler != null) {
            handler.accept(message);
        }
    }
}
