package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.auth;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class SessionEndpoint {

    private final ResponseWriter writer;

    public SessionEndpoint(ResponseWriter writer) {
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
    }

}
