package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.timezones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class LoadTimeZoneEndpointTest {

    @Test
    void testConstructorRejectsNullWriter() {
        var exception = assertThrows(NullPointerException.class, () -> new LoadTimeZoneEndpoint(null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var endpoint = createEndpoint();
        var exception = assertThrows(NullPointerException.class, () -> endpoint.execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteReturnsSupportedTimeZones() throws Exception {
        var server = createServer(createEndpoint());

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/timezones"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals("application/json", response.headers().firstValue("Content-Type").orElseThrow());
            assertTrue(response.body().startsWith("{\"timezones\":["));
            assertTrue(response.body().contains("\"id\":\"Europe/Prague\""));
        } finally {
            server.stop();
        }
    }

    private LoadTimeZoneEndpoint createEndpoint() {
        return new LoadTimeZoneEndpoint(new ResponseWriter(new WebSecurity("http://localhost")));
    }

    private WebServer createServer(LoadTimeZoneEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing.get("/timezones", endpoint::execute))
                .build()
                .start();
    }

}
