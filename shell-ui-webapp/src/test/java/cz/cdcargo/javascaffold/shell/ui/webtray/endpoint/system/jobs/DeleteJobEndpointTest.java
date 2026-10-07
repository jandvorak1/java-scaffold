package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorker;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class DeleteJobEndpointTest {

    @Test
    void testConstructorRejectsNullSecurity() {
        var exception = assertThrows(NullPointerException.class, () -> new DeleteJobEndpoint(null, null, null));

        assertEquals("Web security must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWriter() {
        var security = new WebSecurity("http://localhost");
        var exception = assertThrows(NullPointerException.class,
                () -> new DeleteJobEndpoint(security, null, null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullRegistry() {
        var security = new WebSecurity("http://localhost");
        var exception = assertThrows(NullPointerException.class,
                () -> new DeleteJobEndpoint(security, new ResponseWriter(security), null));

        assertEquals("Background worker registry must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var security = new WebSecurity("http://localhost");
        var endpoint = new DeleteJobEndpoint(security, new ResponseWriter(security), new BackgroundWorkerRegistry());
        var exception = assertThrows(NullPointerException.class, () -> endpoint.execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRemovesWorker() throws Exception {
        var security = new WebSecurity("http://localhost");
        var registry = new BackgroundWorkerRegistry();
        var worker = new BackgroundWorker<Void, Void>("worker-token") {
            @Override
            protected Void doInBackground() {
                return null;
            }
        };
        registry.add(worker);
        var endpoint = new DeleteJobEndpoint(security, new ResponseWriter(security), registry);
        var server = createServer(endpoint);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/jobs/worker-token"))
                    .header("Origin", "http://localhost")
                    .header("X-CSRF-Token", security.csrfToken())
                    .DELETE()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(204, response.statusCode());
            assertThrows(NoSuchElementException.class, () -> registry.get("worker-token"));
        } finally {
            server.stop();
        }
    }

    @Test
    void testExecuteRejectsMissingCsrfToken() throws Exception {
        var security = new WebSecurity("http://localhost");
        var endpoint = new DeleteJobEndpoint(security, new ResponseWriter(security),
                new BackgroundWorkerRegistry());
        var server = createServer(endpoint);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/jobs/worker-token"))
                    .header("Origin", "http://localhost")
                    .DELETE()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(403, response.statusCode());
        } finally {
            server.stop();
        }
    }

    private static WebServer createServer(DeleteJobEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing
                        .delete("/jobs/{token}", endpoint::execute)
                        .error(AuthorizationException.class,
                                (request, response, exception) -> response.status(403).send()))
                .build()
                .start();
    }
}
