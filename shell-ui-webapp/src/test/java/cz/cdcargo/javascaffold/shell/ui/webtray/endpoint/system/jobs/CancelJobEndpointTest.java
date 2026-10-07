package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs;

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

import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorker;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerState;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class CancelJobEndpointTest {

    @Test
    void testConstructorRejectsNullSecurity() {
        var exception = assertThrows(NullPointerException.class, () -> new CancelJobEndpoint(null, null, null));

        assertEquals("Web security must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWriter() {
        var security = new WebSecurity("http://localhost");
        var exception = assertThrows(NullPointerException.class,
                () -> new CancelJobEndpoint(security, null, null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullRegistry() {
        var security = new WebSecurity("http://localhost");
        var exception = assertThrows(NullPointerException.class,
                () -> new CancelJobEndpoint(security, new ResponseWriter(security), null));

        assertEquals("Background worker registry must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var security = new WebSecurity("http://localhost");
        var endpoint = new CancelJobEndpoint(security, new ResponseWriter(security), new BackgroundWorkerRegistry());
        var exception = assertThrows(NullPointerException.class, () -> endpoint.execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteCancelsWorkerAndReturnsState() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var registry = new BackgroundWorkerRegistry();
        var worker = new BackgroundWorker<Void, Void>("worker-token") {
            @Override
            protected Void doInBackground() throws Exception {
                started.countDown();
                release.await();
                return null;
            }
        };
        registry.add(worker);
        worker.execute();
        assertTrue(started.await(5, TimeUnit.SECONDS));

        var security = new WebSecurity("http://localhost");
        var endpoint = new CancelJobEndpoint(security, new ResponseWriter(security), registry);
        var server = createServer(endpoint);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/jobs/worker-token/cancel"))
                    .header("Origin", "http://localhost")
                    .header("X-CSRF-Token", security.csrfToken())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals(BackgroundWorkerState.CANCELLED, worker.state());
            assertTrue(response.body().contains("\"token\":\"worker-token\""));
            assertTrue(response.body().contains("\"state\":\"CANCELLED\""));
        } finally {
            release.countDown();
            server.stop();
        }
    }

    @Test
    void testExecuteRejectsMissingCsrfToken() throws Exception {
        var security = new WebSecurity("http://localhost");
        var endpoint = new CancelJobEndpoint(security, new ResponseWriter(security),
                new BackgroundWorkerRegistry());
        var server = createServer(endpoint);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/jobs/worker-token/cancel"))
                    .header("Origin", "http://localhost")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(403, response.statusCode());
        } finally {
            server.stop();
        }
    }

    private static WebServer createServer(CancelJobEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing
                        .post("/jobs/{token}/cancel", endpoint::execute)
                        .error(AuthorizationException.class,
                                (request, response, exception) -> response.status(403).send()))
                .build()
                .start();
    }
}
