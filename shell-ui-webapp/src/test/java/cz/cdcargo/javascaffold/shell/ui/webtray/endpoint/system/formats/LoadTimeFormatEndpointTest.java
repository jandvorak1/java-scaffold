package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.formats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class LoadTimeFormatEndpointTest {

    @TempDir
    Path tempDir;

    @Test
    void testConstructorRejectsNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new LoadTimeFormatEndpoint(null, null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWriter() {
        var exception = assertThrows(NullPointerException.class,
                () -> new LoadTimeFormatEndpoint(createPreferences(), null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var exception = assertThrows(NullPointerException.class,
                () -> createEndpoint(createPreferences()).execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteReturnsSupportedTimeFormats() throws Exception {
        var server = createServer(createEndpoint(createPreferences()));

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/formats"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var body = response.body();

            assertEquals(200, response.statusCode());
            assertEquals("application/json", response.headers().firstValue("Content-Type").orElseThrow());
            assertTrue(body.startsWith("{\"formats\":["));
            assertTrue(body.contains("\"title\":"));
            assertTrue(body.contains("\"description\":"));
            for (var format : SupportedTimeFormat.values()) {
                assertTrue(body.contains("\"id\":\"" + format.id() + "\""));
                assertTrue(body.contains("\"pattern\":\"" + format.pattern() + "\""));
            }
        } finally {
            server.stop();
        }
    }

    private LoadTimeFormatEndpoint createEndpoint(FilePreferencesStore preferences) {
        return new LoadTimeFormatEndpoint(preferences, new ResponseWriter(new WebSecurity("http://localhost")));
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }

    private WebServer createServer(LoadTimeFormatEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing.get("/formats", endpoint::execute))
                .build()
                .start();
    }
}
