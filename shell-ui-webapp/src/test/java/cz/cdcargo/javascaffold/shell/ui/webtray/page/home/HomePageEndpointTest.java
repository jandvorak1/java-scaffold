package cz.cdcargo.javascaffold.shell.ui.webapp.page.home;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity.LocalIdentity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.helidon.webserver.WebServer;

class HomePageEndpointTest {

    @TempDir
    Path tempDir;

    @Test
    void testConstructorRejectsNullIdentity() {
        var exception = assertThrows(NullPointerException.class,
                () -> new HomePageEndpoint(null, null, null, null, null));

        assertEquals("Identity must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullPreferences() {
        var exception = assertThrows(NullPointerException.class,
                () -> new HomePageEndpoint(new LocalIdentity(), null, null, null, null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullTemplateEngine() {
        var exception = assertThrows(NullPointerException.class,
                () -> new HomePageEndpoint(new LocalIdentity(), createPreferences(), null, null, null));

        assertEquals("Template engine must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullResponseWriter() {
        var security = createSecurity();
        var exception = assertThrows(NullPointerException.class,
                () -> new HomePageEndpoint(new LocalIdentity(), createPreferences(), createTemplateEngine(), null,
                        security));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWebSecurity() {
        var security = createSecurity();
        var exception = assertThrows(NullPointerException.class,
                () -> new HomePageEndpoint(new LocalIdentity(), createPreferences(), createTemplateEngine(),
                        new ResponseWriter(security), null));

        assertEquals("Web security must not be null", exception.getMessage());
    }

    @Test
    void testExecuteReturnsHtmlWithDefaultConfigurationForInvalidPreferences() throws Exception {
        var preferences = createPreferences();
        preferences.put("settings.theme", "</script><p>unsafe</p>");
        preferences.put("settings.timezone", "Invalid/Timezone");
        preferences.put("settings.locale", "invalid");
        preferences.put("settings.touch", "invalid");
        preferences.put("settings.format.decimal", "invalid");
        preferences.put("settings.format.date", "invalid");
        preferences.put("settings.format.time", "invalid");
        preferences.put("settings.format.datetime", "invalid");

        var endpoint = createEndpoint(preferences);
        var server = WebServer.builder()
                .port(0)
                .routing(routing -> routing.get("/", endpoint::execute))
                .build()
                .start();

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var body = response.body();

            var decimalFormat = SupportedDecimalFormat.SPACE_COMMA.pattern();
            var dateFormat = SupportedDateFormat.DD_MM_YYYY_DOT.pattern();
            var timeFormat = SupportedTimeFormat.HH_MM_SS_24.pattern();
            var dateTimeFormat = SupportedDateTimeFormat.DD_MM_YYYY_DOT_HH_MM_SS.pattern();

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElseThrow().startsWith("text/html"));
            assertTrue(body.contains("\"language\":\"cs\""));
            assertTrue(body.contains("\"locale\":\"cs-CZ\""));
            assertTrue(body.contains("\"timeZone\":\"Europe/Prague\""));
            assertTrue(body.contains("\"decimalFormat\":\"" + decimalFormat));
            assertTrue(body.contains("\"dateFormat\":\"" + dateFormat));
            assertTrue(body.contains("\"timeFormat\":\"" + timeFormat));
            assertTrue(body.contains("\"dateTimeFormat\":\"" + dateTimeFormat));
            assertTrue(body.contains("\"admin\""));
            assertTrue(body.contains("\"read\""));
            assertTrue(body.contains("\\u003c/script\\u003e"));
            assertFalse(body.contains("</script><p>unsafe</p>"));
        } finally {
            server.stop();
        }
    }

    private HomePageEndpoint createEndpoint(FilePreferencesStore preferences) {
        var security = createSecurity();
        return new HomePageEndpoint(new LocalIdentity(), preferences, createTemplateEngine(),
                new ResponseWriter(security),
                security);
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }

    private WebSecurity createSecurity() {
        return new WebSecurity("http://localhost");
    }

    private TemplateEngine createTemplateEngine() {
        return TemplateEngine.createPrecompiled(ContentType.Html);
    }
}
