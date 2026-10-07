package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.webtray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class StatusEndpointTest {

    @Test
    void testConstructorRejectsNullWriter() {
        var exception = assertThrows(NullPointerException.class, () -> new StatusEndpoint(null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var endpoint = createEndpoint();
        var exception = assertThrows(NullPointerException.class, () -> endpoint.execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteReturnsOkStatus() throws Exception {
        var server = createServer(createEndpoint());

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/status"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals("application/json", response.headers().firstValue("Content-Type").orElseThrow());
            assertEquals("{\"status\":\"ok\"}", response.body());
        } finally {
            server.stop();
        }
    }

    private StatusEndpoint createEndpoint() {
        var security = new WebSecurity("http://localhost");
        return new StatusEndpoint(new ResponseWriter(security));
    }

    private WebServer createServer(StatusEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing.get("/status", endpoint::execute))
                .build()
                .start();
    }
}
