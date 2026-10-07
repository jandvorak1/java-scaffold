package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.application.message.GenerateGreetingMessageUseCase;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerTokenGenerator;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.JsonParameters;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity.Identity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Permission;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

import io.helidon.http.BadRequestException;
import io.helidon.http.Status;
import io.helidon.json.JsonException;
import io.helidon.json.JsonObject;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Starts asynchronous generation of a greeting message.
 *
 * Requests must pass the state-changing request checks and have read
 * permission. The JSON request body must contain a non-blank title string.
 * Accepted requests register and start a background worker and return its token
 * and current state.
 */
public final class GenerateGreetingMessageEndpoint {

    private static final String PARAM_TITLE = "title";

    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_STATE = "state";

    private final Identity identity;
    private final WebSecurity security;
    private final GenerateGreetingMessageUseCase useCase;
    private final ResponseWriter writer;
    private final BackgroundWorkerRegistry registry;

    /**
     * Creates an endpoint with the services used to authorize requests, start
     * greeting generation, register workers, and write JSON responses.
     *
     * @param identity caller identity and permission resolver
     * @param security state-changing request validator
     * @param useCase  greeting operation run by each worker
     * @param writer   JSON response writer
     * @param registry registry retaining workers for status requests
     * @throws NullPointerException if any dependency is null
     */
    public GenerateGreetingMessageEndpoint(Identity identity, WebSecurity security,
            GenerateGreetingMessageUseCase useCase, ResponseWriter writer, BackgroundWorkerRegistry registry) {
        this.identity = Objects.requireNonNull(identity, "Identity must not be null");
        this.security = Objects.requireNonNull(security, "Web security must not be null");
        this.useCase = Objects.requireNonNull(useCase, "Use case must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
        this.registry = Objects.requireNonNull(registry, "Background worker registry must not be null");
    }

    /**
     * Authorizes the caller, reads the title from the JSON request body, and
     * starts a registered greeting worker.
     *
     * Query parameters do not provide the title. The response contains the
     * worker token and its state when the response is created. Invalid JSON or
     * a missing, non-string, or blank title prevents worker creation.
     *
     * @param request  incoming request containing application credentials,
     *                 request origin, and a JSON object with the title
     * @param response response receiving the worker token and state
     * @throws BadRequestException  if the JSON request body is invalid
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        security.requireStateChangingRequest(request);
        identity.requirePermission(request, Permission.READ);

        try {
            var body = request.content().as(JsonObject.class);
            var parameters = JsonParameters.of(body);
            var title = parameters.requiredNonBlankString(PARAM_TITLE);

            var token = BackgroundWorkerTokenGenerator.generate();
            var cancellationToken = new CancellationToken();
            var worker = new GenerateGreetingMessageWorker(token, cancellationToken, useCase, title);
            registry.add(worker);
            worker.execute();
            var json = JsonValue.objectBuilder()
                    .set(FIELD_TOKEN, worker.token())
                    .set(FIELD_STATE, worker.state().name())
                    .build();
            writer.sendJson(response, Status.ACCEPTED_202, json);
        } catch (JsonException exception) {
            throw new BadRequestException("Invalid JSON request body", exception);
        }
    }
}
