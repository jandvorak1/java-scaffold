package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.webtray;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Initiates a controlled shutdown of the local application.
 *
 * A request must pass the state-changing request checks before it is
 * acknowledged. The shutdown action runs asynchronously after a short delay,
 * allowing the no-content response to be delivered first.
 */
public final class ShutdownEndpoint {

    private static final long SHUTDOWN_DELAY_MILLIS = 300;
    private static final String SHUTDOWN_THREAD_NAME = "application-shutdown";

    private final WebSecurity security;
    private final ResponseWriter writer;
    private final Runnable shutdown;

    /**
     * Creates an endpoint from the request security policy, response writer,
     * and shutdown action.
     *
     * @param security validates state-changing requests
     * @param writer   sends the no-content acknowledgement
     * @param shutdown stops the application after the response is sent
     * @throws NullPointerException if any dependency is null
     */
    public ShutdownEndpoint(WebSecurity security, ResponseWriter writer, Runnable shutdown) {
        this.security = Objects.requireNonNull(security, "Web security must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
        this.shutdown = Objects.requireNonNull(shutdown, "Shutdown action must not be null");
    }

    /**
     * Validates the request, sends a no-content response, and schedules the
     * shutdown action.
     *
     * @param request  request to validate
     * @param response response receiving the acknowledgement
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        security.requireStateChangingRequest(request);
        writer.sendEmpty(response, Status.NO_CONTENT_204);
        Thread.ofVirtual()
                .name(SHUTDOWN_THREAD_NAME)
                .start(() -> {
                    sleep(SHUTDOWN_DELAY_MILLIS);
                    shutdown.run();
                });
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
