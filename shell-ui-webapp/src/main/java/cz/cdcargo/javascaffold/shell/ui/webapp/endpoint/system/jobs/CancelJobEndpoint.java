package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.PathParameters;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

import io.helidon.http.Status;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Cancels a background worker identified by its path token.
 *
 * State-changing request checks protect the operation. The successful JSON
 * response contains the worker token and its state after cancellation is
 * requested.
 */
public final class CancelJobEndpoint {

    private static final String PATH_PARAM_TOKEN = "token";

    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_STATE = "state";

    private final WebSecurity security;
    private final ResponseWriter writer;
    private final BackgroundWorkerRegistry registry;

    /**
     * Creates an endpoint from request security, a response writer, and a
     * background worker registry.
     *
     * @param security validates state-changing requests
     * @param writer   sends the worker state response
     * @param registry finds the worker to cancel
     * @throws NullPointerException if any dependency is null
     */
    public CancelJobEndpoint(WebSecurity security, ResponseWriter writer, BackgroundWorkerRegistry registry) {
        this.security = Objects.requireNonNull(security, "Web security must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
        this.registry = Objects.requireNonNull(registry, "Background worker registry must not be null");
    }

    /**
     * Validates the request, cancels the requested worker, and sends its state.
     *
     * @param request  supplies security data and the worker path token
     * @param response receives the worker token and state
     * @throws NullPointerException                if request or response is null
     * @throws io.helidon.http.BadRequestException if the worker token is missing
     *                                             or blank
     * @throws java.util.NoSuchElementException    if no worker exists for the token
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        security.requireStateChangingRequest(request);
        var token = PathParameters.of(request).requiredNonBlankString(PATH_PARAM_TOKEN);
        var worker = registry.get(token);
        worker.cancel();
        var snapshot = worker.snapshot(0);
        var json = JsonValue.objectBuilder()
                .set(FIELD_TOKEN, snapshot.token())
                .set(FIELD_STATE, snapshot.state().name())
                .build();
        writer.sendJson(response, Status.OK_200, json);
    }
}
