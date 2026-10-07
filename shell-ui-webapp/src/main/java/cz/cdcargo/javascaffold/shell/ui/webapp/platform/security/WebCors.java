package cz.cdcargo.javascaffold.shell.ui.webapp.platform.security;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.helidon.http.HeaderNames;
import io.helidon.http.Method;
import io.helidon.webserver.cors.CorsFeature;

/**
 * Creates the optional cross-origin policy used by the web application.
 *
 * Configured origins are normalized and restricted to HTTP or HTTPS origins
 * without credentials, paths, queries, or fragments. An empty configuration
 * disables the CORS feature.
 */
public final class WebCors {

    private final Set<String> allowedOrigins;

    /**
     * Creates a CORS configuration for the supplied origins.
     *
     * @param allowedOrigins comma-separated origins allowed to access the
     *                       application
     * @throws NullPointerException     if allowedOrigins is null
     * @throws IllegalArgumentException if an origin is malformed or is not an
     *                                  HTTP or HTTPS origin
     */
    public WebCors(String allowedOrigins) {
        this.allowedOrigins = WebSecurity.parseOrigins(
                Objects.requireNonNull(allowedOrigins, "Allowed origins must not be null"));
    }

    /**
     * Creates the CORS feature when at least one origin is configured.
     *
     * @return configured CORS feature, or an empty optional when no origins are
     *         allowed
     */
    public Optional<CorsFeature> create() {
        if (allowedOrigins.isEmpty()) {
            return Optional.empty();
        }

        var builder = CorsFeature.builder()
                .addPath(path -> {
                    path.pathPattern("/*");
                    allowedOrigins.forEach(path::addAllowOrigin);

                    path.addAllowMethod(Method.GET);
                    path.addAllowMethod(Method.POST);
                    path.addAllowMethod(Method.DELETE);

                    path.addAllowHeader(HeaderNames.CONTENT_TYPE_NAME);
                    path.addAllowHeader(HeaderNames.create("X-CSRF-Token"));
                });
        return Optional.of(builder.build());
    }

}
