package cz.cdcargo.javascaffold.shell.ui.webapp.page.favicon;

import java.util.Objects;

import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Handles requests for the conventional root favicon location.
 *
 * The endpoint permanently redirects clients to the favicon published with
 * the application's static frontend assets. It is stateless and can be reused
 * for multiple requests.
 */
public final class FaviconEndpoint {

    private static final String FAVICON_LOCATION = "/bundle/icons/favicon.ico";

    /**
     * Creates a favicon endpoint.
     */
    public FaviconEndpoint() {
    }

    /**
     * Sends a permanent redirect to the published favicon resource.
     *
     * The response has status 301, contains the favicon URI in its Location
     * header, and has no response body.
     *
     * @param request  incoming HTTP request
     * @param response HTTP response used to send the redirect
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        response.status(Status.MOVED_PERMANENTLY_301)
                .header(HeaderNames.LOCATION, FAVICON_LOCATION)
                .send();
    }
}
