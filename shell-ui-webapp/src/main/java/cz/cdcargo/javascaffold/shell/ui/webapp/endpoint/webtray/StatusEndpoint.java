package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.webtray;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import io.helidon.http.Status;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Reports whether the local application's HTTP server is available.
 *
 * A successful invocation returns a compact JSON document with the fixed
 * status value ok. The endpoint itself does not perform authentication or
 * authorization.
 */
public final class StatusEndpoint {

    private static final String STATUS_FIELD = "status";
    private static final String STATUS_OK = "ok";
    private static final JsonValue STATUS_RESPONSE = JsonValue.objectBuilder()
            .set(STATUS_FIELD, STATUS_OK)
            .build();

    private final ResponseWriter writer;

    /**
     * Creates an endpoint that writes availability responses.
     *
     * @param writer writes JSON responses
     * @throws NullPointerException if writer is null
     */
    public StatusEndpoint(ResponseWriter writer) {
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    /**
     * Sends the available status as a successful JSON response.
     *
     * @param request  received HTTP request
     * @param response response that receives the status document
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        writer.sendJson(response, Status.OK_200, STATUS_RESPONSE);
    }
}
