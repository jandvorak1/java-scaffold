package cz.cdcargo.javascaffold.shell.ui.webapp.page.favicon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import io.helidon.webserver.WebServer;

class FaviconEndpointTest {

    @Test
    void testExecuteReturnsFaviconRedirect() throws Exception {
        var endpoint = new FaviconEndpoint();
        var server = WebServer.builder()
                .port(0)
                .routing(routing -> routing.get("/favicon.ico", endpoint::execute))
                .build()
                .start();

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/favicon.ico"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(301, response.statusCode());
            assertEquals("/bundle/icons/favicon.ico", response.headers().firstValue("Location").orElseThrow());
        } finally {
            server.stop();
        }
    }
}
