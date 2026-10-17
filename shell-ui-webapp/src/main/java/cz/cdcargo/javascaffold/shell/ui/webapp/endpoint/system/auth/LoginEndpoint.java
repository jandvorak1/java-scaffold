package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.auth;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthenticationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.password.PasswordAuthenticator;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.session.SessionManager;
import io.helidon.http.Status;
import io.helidon.json.JsonObject;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class LoginEndpoint {

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PASSWORD = "password";

    private final WebSecurity security;
    private final ResponseWriter writer;
    private final PasswordAuthenticator authenticator;
    private final SessionManager session;

    public LoginEndpoint(WebSecurity security, ResponseWriter writer, PasswordAuthenticator authenticator,
            SessionManager session) {
        this.security = Objects.requireNonNull(security, "Web security must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
        this.authenticator = Objects.requireNonNull(authenticator, "Password authenticator must not be null");
        this.session = Objects.requireNonNull(session, "Session manager must not be null");
    }

    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        security.requireStateChangingRequest(request);

        var json = request.content().as(JsonObject.class);
        var email = json.stringValue(FIELD_EMAIL).orElseThrow(AuthenticationException::new);
        var password = json.stringValue(FIELD_PASSWORD).orElseThrow(AuthenticationException::new);

        var account = authenticator.authenticate(email, password);
        session.startSession(response, account.principal());
        writer.sendEmpty(response, Status.NO_CONTENT_204);
    }
}
