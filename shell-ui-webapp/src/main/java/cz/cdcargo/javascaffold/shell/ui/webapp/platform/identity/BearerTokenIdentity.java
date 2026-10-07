package cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthenticationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.AccessTokenService;
import io.helidon.http.HeaderNames;
import io.helidon.webserver.http.ServerRequest;

/**
 * Authenticates HTTP requests using bearer credentials.
 *
 * The authentication scheme is matched without regard to letter case. Missing,
 * malformed, blank, or rejected credentials result in an authentication
 * exception without exposing token details.
 */
public final class BearerTokenIdentity implements Identity {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AccessTokenService service;

    /**
     * Creates an identity backed by the supplied access token service.
     *
     * @param service service used to authenticate bearer tokens
     * @throws NullPointerException if service is null
     */
    public BearerTokenIdentity(AccessTokenService service) {
        this.service = Objects.requireNonNull(service, "Access token service must not be null");
    }

    @Override
    public Principal requirePrincipal(ServerRequest request) {
        var token = readBearerToken(request);
        return service.authenticateBearer(token).orElseThrow(AuthenticationException::new);
    }

    private static String readBearerToken(ServerRequest request) {
        Objects.requireNonNull(request, "Server request must not be null");
        var authorization = request.headers().first(HeaderNames.AUTHORIZATION).orElse("");
        return readBearerToken(authorization);
    }

    static String readBearerToken(String authorization) {
        Objects.requireNonNull(authorization, "Authorization value must not be null");
        if (authorization.length() <= BEARER_PREFIX.length()
                || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return "";
        }
        var token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isBlank() ? "" : token;
    }
}
