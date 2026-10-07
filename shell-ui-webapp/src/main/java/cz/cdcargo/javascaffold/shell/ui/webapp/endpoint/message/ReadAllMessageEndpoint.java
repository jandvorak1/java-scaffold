package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.application.message.ReadAllMessageUseCase;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerTokenGenerator;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity.Identity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Permission;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

import io.helidon.http.Status;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Starts asynchronous retrieval of all messages.
 *
 * Requests must pass the state-changing request checks and have read
 * permission. Accepted requests register and start a background worker and
 * return its token and current state.
 */
public final class ReadAllMessageEndpoint {

    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_STATE = "state";

    private final Identity identity;
    private final WebSecurity security;
    private final ReadAllMessageUseCase useCase;
    private final ResponseWriter writer;
    private final BackgroundWorkerRegistry registry;

    /**
     * Creates an endpoint with the services required to authorize and start
     * asynchronous message retrieval.
     *
     * @param identity identity service used to verify the caller's permissions
     * @param security security policy used to verify state-changing requests
     * @param useCase  message retrieval operation executed by the background worker
     * @param writer   writer used to send the accepted response
     * @param registry registry receiving the created background worker
     * @throws NullPointerException if any argument is null
     */
    public ReadAllMessageEndpoint(Identity identity, WebSecurity security, ReadAllMessageUseCase useCase,
            ResponseWriter writer, BackgroundWorkerRegistry registry) {
        this.identity = Objects.requireNonNull(identity, "Identity must not be null");
        this.security = Objects.requireNonNull(security, "Web security must not be null");
        this.useCase = Objects.requireNonNull(useCase, "Use case must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
        this.registry = Objects.requireNonNull(registry, "Background worker registry must not be null");
    }

    /**
     * Authorizes the caller and starts message retrieval in a registered
     * background worker.
     *
     * The response contains the worker token and its state when the response is
     * created.
     *
     * @param request  incoming request containing application credentials and
     *                 request origin
     * @param response response receiving the worker token and state
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        security.requireStateChangingRequest(request);
        identity.requirePermission(request, Permission.READ);

        var token = BackgroundWorkerTokenGenerator.generate();
        var cancellationToken = new CancellationToken();
        var worker = new ReadAllMessageWorker(token, cancellationToken, useCase);

        registry.add(worker);
        worker.execute();
        var json = JsonValue.objectBuilder()
                .set(FIELD_TOKEN, worker.token())
                .set(FIELD_STATE, worker.state().name())
                .build();
        writer.sendJson(response, Status.ACCEPTED_202, json);
    }
}
