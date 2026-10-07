package cz.cdcargo.javascaffold.shell.ui.webapp.platform.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Enforces request protections and applies security headers for the web
 * application.
 *
 * A policy instance owns a cryptographically random CSRF token. State-changing
 * requests must present that token and originate from a configured HTTP or
 * HTTPS origin. Configured origins are normalized when the policy is created.
 */
public final class WebSecurity {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final String csrfToken;
    private final Set<String> sameOrigins;

    /**
     * Creates a web security policy for the supplied same-origin values.
     *
     * @param sameOrigins comma-separated origins trusted for state-changing
     *                    requests
     * @throws NullPointerException     if sameOrigins is null
     * @throws IllegalArgumentException if an origin is malformed or is not an
     *                                  HTTP or HTTPS origin
     */
    public WebSecurity(String sameOrigins) {
        this.csrfToken = createCsrfToken();
        this.sameOrigins = parseOrigins(Objects.requireNonNull(sameOrigins, "Same origins must not be null"));
    }

    /**
     * Returns the CSRF token generated for this security policy.
     *
     * @return CSRF token
     */
    public String csrfToken() {
        return csrfToken;
    }

    /**
     * Validates the CSRF token and origin of a state-changing request.
     *
     * @param request request to validate
     * @throws AuthorizationException if the CSRF token or origin is invalid
     * @throws NullPointerException   if request is null
     */
    public void requireStateChangingRequest(ServerRequest request) {
        Objects.requireNonNull(request, "Server request must not be null");
        requireCsrfToken(request);
        requireSameOrigin(request);
    }

    /**
     * Adds the security headers required by the web application.
     *
     * @param response response to which the headers are added
     * @throws NullPointerException if response is null
     */
    public void applySecurityHeaders(ServerResponse response) {
        Objects.requireNonNull(response, "Server response must not be null");
        response.header(HeaderValues.X_CONTENT_TYPE_OPTIONS_NOSNIFF);
        response.header(HeaderValues.create("Referrer-Policy", "no-referrer"));
        response.header(HeaderValues.create("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'none'; form-action 'self'; frame-ancestors 'none'"));
    }

    private void requireCsrfToken(ServerRequest request) {
        var actual = request.headers().first(HeaderNames.create("X-CSRF-Token")).orElse("");
        if (actual.isBlank() || !MessageDigest.isEqual(csrfToken.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8))) {
            throw new AuthorizationException();
        }
    }

    private void requireSameOrigin(ServerRequest request) {
        var origin = request.headers().first(HeaderNames.ORIGIN).orElse("");
        if (!origin.isBlank()) {
            try {
                if (sameOrigins.contains(normalizeOrigin(origin))) {
                    return;
                }
            } catch (IllegalArgumentException exception) {
                // Invalid request origins are treated as unauthorized input.
            }
            throw new AuthorizationException();
        }
        var referer = request.headers().first(HeaderNames.REFERER).orElse("");
        if (!referer.isBlank()) {
            if (sameOrigins.stream().anyMatch(ref -> referer.equals(ref) || referer.startsWith(ref + "/")
                    || referer.startsWith(ref + "?") || referer.startsWith(ref + "#"))) {
                return;
            }
        }
        throw new AuthorizationException();
    }

    private static String createCsrfToken() {
        var bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static Set<String> parseOrigins(String value) {
        Objects.requireNonNull(value, "Origins must not be null");
        if (value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .map(WebSecurity::normalizeOrigin)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalizeOrigin(String origin) {
        try {
            var uri = new URI(origin);
            var scheme = uri.getScheme();
            var path = uri.getRawPath();
            if (scheme == null || uri.getHost() == null || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || !(path == null || path.isEmpty() || path.equals("/"))
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Invalid origin: " + origin);
            }
            var normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            var port = uri.getPort();
            if ((normalizedScheme.equals("http") && port == 80)
                    || (normalizedScheme.equals("https") && port == 443)) {
                port = -1;
            }
            return new URI(normalizedScheme, null, uri.getHost().toLowerCase(Locale.ROOT), port, null, null, null)
                    .toString();
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid origin: " + origin, exception);
        }
    }
}
