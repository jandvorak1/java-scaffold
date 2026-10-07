package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.PathParameters;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Removes a background worker from the registry.
 *
 * State-changing request checks protect the operation. Removing a worker only
 * ends its registry visibility; it does not cancel a running operation.
 */
public final class DeleteJobEndpoint {

    private static final String PATH_PARAM_TOKEN = "token";

    private final WebSecurity security;
    private final ResponseWriter writer;
    private final BackgroundWorkerRegistry registry;

    /**
     * Creates an endpoint from request security, a response writer, and a
     * background worker registry.
     *
     * @param security validates state-changing requests
     * @param writer   sends the no-content acknowledgement
     * @param registry removes the worker from its entries
     * @throws NullPointerException if any dependency is null
     */
    public DeleteJobEndpoint(WebSecurity security, ResponseWriter writer, BackgroundWorkerRegistry registry) {
        this.security = Objects.requireNonNull(security, "Web security must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
        this.registry = Objects.requireNonNull(registry, "Background worker registry must not be null");
    }

    /**
     * Validates the request, removes the requested worker, and acknowledges it.
     *
     * @param request  supplies security data and the worker path token
     * @param response receives the no-content acknowledgement
     * @throws NullPointerException                if request or response is null
     * @throws io.helidon.http.BadRequestException if the worker token is missing
     *                                             or blank
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        security.requireStateChangingRequest(request);
        var token = PathParameters.of(request).requiredNonBlankString(PATH_PARAM_TOKEN);
        registry.remove(token);
        writer.sendEmpty(response, Status.NO_CONTENT_204);
    }
}
