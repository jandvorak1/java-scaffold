package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.preferences;

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
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDecimalFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class LoadPreferencesEndpointTest {

    @TempDir
    Path tempDir;

    @Test
    void testConstructorRejectsNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new LoadPreferencesEndpoint(null, null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWriter() {
        var exception = assertThrows(NullPointerException.class,
                () -> new LoadPreferencesEndpoint(createPreferences(), null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var exception = assertThrows(NullPointerException.class,
                () -> createEndpoint(createPreferences()).execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteReturnsDefaultsForUnsupportedPreferences() throws Exception {
        var preferences = createPreferences();
        preferences.put("settings.locale", "invalid");
        preferences.put("settings.format.decimal", "invalid");
        preferences.put("settings.format.date", "invalid");
        preferences.put("settings.format.time", "invalid");
        preferences.put("settings.format.datetime", "invalid");
        preferences.put("settings.timezone", "Invalid/Timezone");
        preferences.put("settings.touch", "invalid");
        var server = createServer(createEndpoint(preferences));

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/preferences"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var body = response.body();

            assertEquals(200, response.statusCode());
            assertEquals("application/json", response.headers().firstValue("Content-Type").orElseThrow());
            assertTrue(body.contains("\"locale\":\"cs-CZ\""));
            assertTrue(body.contains("\"decimalFormat\":\"" + SupportedDecimalFormat.SPACE_COMMA.id()));
            assertTrue(body.contains("\"dateFormat\":\"" + SupportedDateFormat.DD_MM_YYYY_DOT.id()));
            assertTrue(body.contains("\"timeFormat\":\"" + SupportedTimeFormat.HH_MM_SS_24.id()));
            assertTrue(body.contains("\"dateTimeFormat\":\"" + SupportedDateTimeFormat.DD_MM_YYYY_DOT_HH_MM_SS.id()));
            assertTrue(body.contains("\"timeZone\":\"Europe/Prague\""));
            assertTrue(body.contains("\"theme\":\"sap_fiori_3\""));
            assertTrue(body.contains("\"touch\":false"));
        } finally {
            server.stop();
        }
    }

    private LoadPreferencesEndpoint createEndpoint(FilePreferencesStore preferences) {
        return new LoadPreferencesEndpoint(preferences, new ResponseWriter(new WebSecurity("http://localhost")));
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }

    private WebServer createServer(LoadPreferencesEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing.get("/preferences", endpoint::execute))
                .build()
                .start();
    }
}
