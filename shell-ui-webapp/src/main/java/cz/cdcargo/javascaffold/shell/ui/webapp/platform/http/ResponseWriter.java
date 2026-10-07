package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HttpMediaType;
import io.helidon.http.Status;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerResponse;

/**
 * Writes HTTP responses with a status, the appropriate content type, and the
 * application's security headers.
 *
 * Each operation prepares and sends the response immediately. Textual bodies
 * use UTF-8, while empty responses do not set a content type.
 */
public final class ResponseWriter {

    private final WebSecurity security;

    /**
     * Creates a response writer using the supplied security configuration.
     *
     * @param security security header provider
     * @throws NullPointerException if security is null
     */
    public ResponseWriter(WebSecurity security) {
        this.security = Objects.requireNonNull(security, "Web security must not be null");
    }

    /**
     * Sends a JSON response.
     *
     * @param response HTTP response to write
     * @param status   HTTP status
     * @param json     JSON response body
     * @throws NullPointerException if response, status, or json is null
     */
    public void sendJson(ServerResponse response, Status status, JsonValue json) {
        Objects.requireNonNull(json, "JSON body must not be null");
        prepare(response, status);
        response.headers().contentType(MediaTypes.APPLICATION_JSON);
        response.send(json);
    }

    /**
     * Sends an HTML response encoded as UTF-8.
     *
     * @param response HTTP response to write
     * @param status   HTTP status
     * @param html     HTML response body
     * @throws NullPointerException if response, status, or html is null
     */
    public void sendHtml(ServerResponse response, Status status, String html) {
        Objects.requireNonNull(html, "HTML body must not be null");
        prepare(response, status);
        response.headers().contentType(HttpMediaType.create(MediaTypes.TEXT_HTML).withCharset(StandardCharsets.UTF_8));
        response.send(html);
    }

    /**
     * Sends a plain-text response encoded as UTF-8.
     *
     * @param response HTTP response to write
     * @param status   HTTP status
     * @param text     plain-text response body
     * @throws NullPointerException if response, status, or text is null
     */
    public void sendText(ServerResponse response, Status status, String text) {
        Objects.requireNonNull(text, "Text body must not be null");
        prepare(response, status);
        response.headers().contentType(HttpMediaType.create(MediaTypes.TEXT_PLAIN).withCharset(StandardCharsets.UTF_8));
        response.send(text);
    }

    /**
     * Sends a response without a body.
     *
     * @param response HTTP response to write
     * @param status   HTTP status
     * @throws NullPointerException if response or status is null
     */
    public void sendEmpty(ServerResponse response, Status status) {
        prepare(response, status);
        response.send();
    }

    private void prepare(ServerResponse response, Status status) {
        Objects.requireNonNull(response, "Server response must not be null");
        Objects.requireNonNull(status, "HTTP status must not be null");
        security.applySecurityHeaders(response);
        response.status(status.code());
    }
}
