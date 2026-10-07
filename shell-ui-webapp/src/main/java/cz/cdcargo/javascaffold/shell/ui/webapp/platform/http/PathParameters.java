package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import java.util.Objects;

import io.helidon.common.parameters.Parameters;
import io.helidon.webserver.http.ServerRequest;

/**
 * Provides typed and validated access to HTTP path parameters.
 *
 * Values are read from a Helidon parameter collection and conversion failures
 * are reported as bad HTTP requests identifying the path parameter source.
 */
public final class PathParameters extends AbstractURIParameters {

    /**
     * Creates a path parameter accessor for the supplied parameter collection.
     *
     * @param parameters path parameter collection
     * @throws NullPointerException if parameters is null
     */
    public PathParameters(Parameters parameters) {
        super(parameters, "path");
    }

    /**
     * Creates a path parameter accessor for the supplied request.
     *
     * @param request HTTP server request containing the path parameters
     * @return accessor backed by the request path parameters
     * @throws NullPointerException if request is null
     */
    public static PathParameters of(ServerRequest request) {
        Objects.requireNonNull(request, "Server request must not be null");
        return new PathParameters(request.path().pathParameters());
    }

    /**
     * Creates a path parameter accessor for the supplied parameter collection.
     *
     * @param parameters path parameter collection
     * @return accessor backed by the supplied parameters
     * @throws NullPointerException if parameters is null
     */
    public static PathParameters of(Parameters parameters) {
        return new PathParameters(parameters);
    }
}
