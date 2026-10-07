package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.preferences;

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
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.http.HttpException;
import io.helidon.webserver.WebServer;

class SavePreferencesEndpointTest {

    @TempDir
    Path tempDir;

    @Test
    void testConstructorRejectsNullSecurity() {
        var exception = assertThrows(NullPointerException.class,
                () -> new SavePreferencesEndpoint(null, null, null));

        assertEquals("Web security must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullPreferences() {
        var security = new WebSecurity("http://localhost");
        var exception = assertThrows(NullPointerException.class,
                () -> new SavePreferencesEndpoint(security, null, null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWriter() {
        var security = createSecurity();
        var exception = assertThrows(NullPointerException.class,
                () -> new SavePreferencesEndpoint(security, createPreferences(), null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsMissingCsrfToken() throws Exception {
        var security = createSecurity();
        var endpoint = createEndpoint(security, createPreferences());
        var server = createServer(endpoint);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/preferences"))
                    .header("Origin", "http://localhost")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(403, response.statusCode());
        } finally {
            server.stop();
        }
    }

    @Test
    void testExecuteSavesValidatedPreferences() throws Exception {
        var security = createSecurity();
        var preferences = createPreferences();
        var server = createServer(createEndpoint(security, preferences));

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/preferences"))
                    .header("Origin", "http://localhost")
                    .header("X-CSRF-Token", security.csrfToken())
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {"locale":"en-US","decimalFormat":"comma_dot","dateFormat":"yyyy_mm_dd_dash",
                            "timeFormat":"hh_mm_ss_12","dateTimeFormat":"mm_dd_yyyy_slash_hh_mm_ss_a",
                            "timeZone":"Europe/Prague","theme":"sap_horizon","touch":true}
                            """))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(204, response.statusCode());
            assertEquals("en-US", preferences.get("settings.locale", null));
            assertEquals("comma_dot", preferences.get("settings.format.decimal", null));
            assertEquals("yyyy_mm_dd_dash", preferences.get("settings.format.date", null));
            assertEquals("hh_mm_ss_12", preferences.get("settings.format.time", null));
            assertEquals("mm_dd_yyyy_slash_hh_mm_ss_a", preferences.get("settings.format.datetime", null));
            assertEquals("Europe/Prague", preferences.get("settings.timezone", null));
            assertEquals("sap_horizon", preferences.get("settings.theme", null));
            assertEquals("true", preferences.get("settings.touch", null));
        } finally {
            server.stop();
        }
    }

    @Test
    void testExecuteRejectsInvalidFormatWithoutChangingPreferences() throws Exception {
        var security = createSecurity();
        var preferences = createPreferences();
        preferences.put("settings.theme", "initial-theme");
        var server = createServer(createEndpoint(security, preferences));

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/preferences"))
                    .header("Origin", "http://localhost")
                    .header("X-CSRF-Token", security.csrfToken())
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"decimalFormat\":\"invalid\",\"theme\":\"changed-theme\"}"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(400, response.statusCode());
            assertEquals("initial-theme", preferences.get("settings.theme", null));
            assertEquals(null, preferences.get("settings.locale", null));
        } finally {
            server.stop();
        }
    }

    private SavePreferencesEndpoint createEndpoint(WebSecurity security, FilePreferencesStore preferences) {
        return new SavePreferencesEndpoint(security, preferences, new ResponseWriter(security));
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }

    private WebSecurity createSecurity() {
        return new WebSecurity("http://localhost");
    }

    private WebServer createServer(SavePreferencesEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing
                        .post("/preferences", endpoint::execute)
                        .error(AuthorizationException.class,
                                (request, response, exception) -> response.status(403).send())
                        .error(HttpException.class,
                                (request, response, exception) -> response.status(exception.status().code()).send()))
                .build()
                .start();
    }
}
