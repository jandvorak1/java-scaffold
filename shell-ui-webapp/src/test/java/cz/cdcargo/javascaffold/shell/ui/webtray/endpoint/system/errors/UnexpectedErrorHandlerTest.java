package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class UnexpectedErrorHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testUnexpectedErrorHandlerRejectsNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new UnexpectedErrorHandler(null, null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testUnexpectedErrorHandlerRejectsNullWriter() {
        var exception = assertThrows(NullPointerException.class,
                () -> new UnexpectedErrorHandler(createPreferences(), null));

        assertEquals("Error response writer must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var exception = assertThrows(NullPointerException.class,
                () -> createHandler(createPreferences()).execute(null, null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteSendsLocalizedInternalServerErrorResponse() throws Exception {
        var preferences = createPreferences();
        preferences.put("settings.locale", "cs-CZ");
        var server = createServer(createHandler(preferences));

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/error"))
                    .header("Accept", "application/json")
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(500, response.statusCode());
            assertEquals("application/json", response.headers().firstValue("Content-Type").orElseThrow());
            assertEquals(
                    "{\"error\":{\"message\":\"Došlo k neočekávané chybě! Kontaktujte prosím administrátora systému.\"}}",
                    response.body());
        } finally {
            server.stop();
        }
    }

    private UnexpectedErrorHandler createHandler(FilePreferencesStore preferences) {
        var writer = new ErrorResponseWriter(new ResponseWriter(new WebSecurity("http://localhost")));
        return new UnexpectedErrorHandler(preferences, writer);
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }

    private WebServer createServer(UnexpectedErrorHandler handler) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing
                        .get("/error", (request, response) -> {
                            throw new IllegalStateException("Unexpected failure");
                        })
                        .error(RuntimeException.class, handler::execute))
                .build()
                .start();
    }
}
