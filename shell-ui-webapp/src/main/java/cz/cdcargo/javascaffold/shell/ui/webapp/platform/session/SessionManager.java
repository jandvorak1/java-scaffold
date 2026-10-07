package cz.cdcargo.javascaffold.shell.ui.webapp.platform.session;

import java.time.Duration;
import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.AccessTokenService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Coordinates session tokens with their HTTP cookies.
 *
 * Starting a session issues a token and sends it to the client. Ending a
 * session revokes a presented token and expires the client cookie.
 */
public final class SessionManager {

    private final AccessTokenService tokenService;
    private final SessionCookie cookie;
    private final Duration lifetime;

    /**
     * Creates a session manager with a positive cookie lifetime.
     *
     * @param tokenService service used to issue, authenticate, and revoke session
     *                     tokens
     * @param cookie       session cookie definition
     * @param lifetime     session cookie lifetime
     * @throws NullPointerException     if tokenService, cookie, or lifetime is null
     * @throws IllegalArgumentException if lifetime is not positive
     */
    public SessionManager(AccessTokenService tokenService, SessionCookie cookie, Duration lifetime) {
        this.tokenService = Objects.requireNonNull(tokenService, "Token service must not be null");
        this.cookie = Objects.requireNonNull(cookie, "Session cookie must not be null");
        this.lifetime = Objects.requireNonNull(lifetime, "Session lifetime must not be null");
        if (lifetime.isZero() || lifetime.isNegative()) {
            throw new IllegalArgumentException("Session lifetime must be positive");
        }
    }

    /**
     * Determines whether a request contains an authenticated session token.
     *
     * @param request HTTP request to inspect
     * @return true when the request contains a valid session
     * @throws NullPointerException if request is null
     */
    public boolean hasValidSession(ServerRequest request) {
        var token = cookie.read(request);
        return !token.isBlank() && tokenService.authenticateSession(token).isPresent();
    }

    /**
     * Starts a session and adds the issued token cookie to the response.
     *
     * @param response  HTTP response receiving the session cookie
     * @param principal principal associated with the new session
     * @throws NullPointerException if response or principal is null
     */
    public void startSession(ServerResponse response, Principal principal) {
        Objects.requireNonNull(response, "Server response must not be null");
        Objects.requireNonNull(principal, "Principal must not be null");
        var token = tokenService.issueSession(principal);
        response.headers().addCookie(cookie.create(token, lifetime));
    }

    /**
     * Ends a session and adds an expired session cookie to the response.
     *
     * @param request  HTTP request containing the session cookie
     * @param response HTTP response receiving the expired cookie
     * @throws NullPointerException if request or response is null
     */
    public void endSession(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        var token = cookie.read(request);
        if (!token.isBlank()) {
            tokenService.revoke(token);
        }
        response.headers().addCookie(cookie.clear());
    }
}
