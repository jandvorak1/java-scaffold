package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import java.util.Objects;

import io.helidon.common.parameters.Parameters;
import io.helidon.webserver.http.ServerRequest;

/**
 * Provides typed and validated access to HTTP query parameters.
 *
 * Values are read from a Helidon parameter collection and conversion failures
 * are reported as bad HTTP requests identifying the query parameter source.
 */
public final class QueryParameters extends AbstractURIParameters {

    private QueryParameters(Parameters parameters) {
        super(parameters, "query");
    }

    /**
     * Creates a query parameter accessor for the supplied request.
     *
     * @param request HTTP server request containing the query parameters
     * @return accessor backed by the request query parameters
     * @throws NullPointerException if request is null
     */
    public static QueryParameters of(ServerRequest request) {
        Objects.requireNonNull(request, "Server request must not be null");
        return new QueryParameters(request.query());
    }

    /**
     * Creates a query parameter accessor for the supplied parameter collection.
     *
     * @param parameters query parameter collection
     * @return accessor backed by the supplied parameters
     * @throws NullPointerException if parameters is null
     */
    public static QueryParameters of(Parameters parameters) {
        return new QueryParameters(parameters);
    }
}
