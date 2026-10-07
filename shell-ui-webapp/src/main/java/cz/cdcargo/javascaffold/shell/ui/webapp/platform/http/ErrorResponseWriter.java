package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import java.util.List;
import java.util.Objects;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.HttpMediaType;
import io.helidon.http.Status;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Negotiates and writes an error representation supported by both the request
 * and the application.
 *
 * HTML, plain text, and JSON are supported. A request without an Accept header
 * or without a compatible media type receives an empty response with the
 * supplied status.
 */
public final class ErrorResponseWriter {

    enum ResponseType {
        HTML,
        TEXT,
        JSON,
        EMPTY
    }

    private final ResponseWriter writer;

    /**
     * Creates an error response writer.
     *
     * @param writer response writer used for the selected representation
     * @throws NullPointerException if writer is null
     */
    public ErrorResponseWriter(ResponseWriter writer) {
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    /**
     * Writes an error response in a representation accepted by the request.
     *
     * @param request  HTTP request containing the Accept header
     * @param response HTTP response to write
     * @param status   HTTP status for the response
     * @param message  error message
     * @throws NullPointerException if request, response, status, or message is null
     */
    public void send(ServerRequest request, ServerResponse response, Status status, String message) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        Objects.requireNonNull(status, "HTTP status must not be null");
        Objects.requireNonNull(message, "Error message must not be null");
        var responseType = request.headers().contains(HeaderNames.ACCEPT)
                ? selectResponseType(request.headers().acceptedTypes())
                : ResponseType.EMPTY;
        switch (responseType) {
            case HTML -> sendHtml(response, status, message);
            case TEXT -> writer.sendText(response, status, message);
            case JSON -> sendJson(response, status, message);
            case EMPTY -> writer.sendEmpty(response, status);
        }
    }

    static ResponseType selectResponseType(List<HttpMediaType> acceptedTypes) {
        Objects.requireNonNull(acceptedTypes, "Accepted media types must not be null");
        for (var acceptedType : acceptedTypes) {
            if (acceptedType.qualityFactor() == 0) {
                continue;
            }
            if (acceptedType.test(MediaTypes.TEXT_HTML)) {
                return ResponseType.HTML;
            }
            if (acceptedType.test(MediaTypes.TEXT_PLAIN)) {
                return ResponseType.TEXT;
            }
            if (acceptedType.test(MediaTypes.APPLICATION_JSON)) {
                return ResponseType.JSON;
            }
        }
        return ResponseType.EMPTY;
    }

    private void sendJson(ServerResponse response, Status status, String message) {
        var json = JsonValue.objectBuilder()
                .set("error", JsonValue.objectBuilder()
                        .set("message", message)
                        .build())
                .build();
        writer.sendJson(response, status, json);
    }

    private void sendHtml(ServerResponse response, Status status, String message) {
        var html = """
                <!doctype html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>%s</title>
                </head>
                <body>
                    <h1>%s</h1>
                    <p>%s</p>
                </body>
                </html>
                """.formatted(status.codeText(), status.codeText(), escapeHtml(message));
        writer.sendHtml(response, status, html);
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
