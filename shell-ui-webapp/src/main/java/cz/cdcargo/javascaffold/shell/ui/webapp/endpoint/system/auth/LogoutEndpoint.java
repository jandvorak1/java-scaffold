package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.auth;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class LogoutEndpoint {

    private final WebSecurity security;
    private final ResponseWriter writer;

    public LogoutEndpoint(WebSecurity security, ResponseWriter writer) {
        this.security = Objects.requireNonNull(security, "Web security must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        security.requireStateChangingRequest(request);
    }

}
