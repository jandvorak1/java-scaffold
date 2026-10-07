package cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthenticationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.session.SessionCookie;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.AccessTokenService;
import io.helidon.webserver.http.ServerRequest;

/**
 * Authenticates HTTP requests using a session token stored in the configured
 * cookie.
 *
 * Missing or rejected session tokens result in an authentication exception
 * without exposing token details.
 */
public final class SessionCookieIdentity implements Identity {

    private final AccessTokenService service;
    private final SessionCookie cookie;

    /**
     * Creates an identity backed by the supplied token service and session cookie.
     *
     * @param service service used to authenticate session tokens
     * @param cookie  cookie containing the session token
     * @throws NullPointerException if service or cookie is null
     */
    public SessionCookieIdentity(AccessTokenService service, SessionCookie cookie) {
        this.service = Objects.requireNonNull(service, "Access token service must not be null");
        this.cookie = Objects.requireNonNull(cookie, "Session cookie must not be null");
    }

    @Override
    public Principal requirePrincipal(ServerRequest request) {
        var token = cookie.read(request);
        return service.authenticateSession(token).orElseThrow(AuthenticationException::new);
    }
}
