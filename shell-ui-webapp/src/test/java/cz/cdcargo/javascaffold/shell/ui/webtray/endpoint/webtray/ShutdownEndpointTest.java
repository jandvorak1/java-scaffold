package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.webtray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class ShutdownEndpointTest {

    @Test
    void testConstructorRejectsNullSecurity() {
        var exception = assertThrows(NullPointerException.class,
                () -> new ShutdownEndpoint(null, null, () -> {
                }));

        assertEquals("Web security must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWriter() {
        var security = new WebSecurity("http://localhost");
        var exception = assertThrows(NullPointerException.class,
                () -> new ShutdownEndpoint(security, null, () -> {
                }));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullShutdownAction() {
        var security = new WebSecurity("http://localhost");
        var writer = new ResponseWriter(security);
        var exception = assertThrows(NullPointerException.class,
                () -> new ShutdownEndpoint(security, writer, null));

        assertEquals("Shutdown action must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var security = new WebSecurity("http://localhost");
        var writer = new ResponseWriter(security);
        var endpoint = new ShutdownEndpoint(security, writer, () -> {
        });
        var exception = assertThrows(NullPointerException.class, () -> endpoint.execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteReturnsNoContentAndSchedulesShutdown() throws Exception {
        var shutdown = new CountDownLatch(1);
        var security = new WebSecurity("http://localhost");
        var endpoint = new ShutdownEndpoint(security, new ResponseWriter(security), shutdown::countDown);
        var server = createServer(endpoint);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/shutdown"))
                    .header("Origin", "http://localhost")
                    .header("X-CSRF-Token", security.csrfToken())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(204, response.statusCode());
            assertTrue(shutdown.await(2, TimeUnit.SECONDS));
        } finally {
            server.stop();
        }
    }

    @Test
    void testExecuteRejectsMissingCsrfToken() throws Exception {
        var security = new WebSecurity("http://localhost");
        var endpoint = new ShutdownEndpoint(security, new ResponseWriter(security), () -> {
        });
        var server = createServer(endpoint);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/shutdown"))
                    .header("Origin", "http://localhost")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(403, response.statusCode());
        } finally {
            server.stop();
        }
    }

    private static WebServer createServer(ShutdownEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing
                        .post("/shutdown", endpoint::execute)
                        .error(AuthorizationException.class,
                                (request, response, exception) -> response.status(403).send()))
                .build()
                .start();
    }
}
