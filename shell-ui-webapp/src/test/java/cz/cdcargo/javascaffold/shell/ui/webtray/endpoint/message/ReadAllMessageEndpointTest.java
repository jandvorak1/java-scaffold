package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerState;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity.LocalIdentity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class ReadAllMessageEndpointTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\\"token\\\":\\\"([^\\\"]+)\\\"");

    @Test
    void testExecute() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var registry = new BackgroundWorkerRegistry();
        var security = new WebSecurity("http://localhost");
        var endpoint = new ReadAllMessageEndpoint(new LocalIdentity(), security, () -> {
            started.countDown();
            await(release);
            return List.of();
        }, new ResponseWriter(security), registry);
        var server = WebServer.builder()
                .port(0)
                .routing(routing -> routing.post("/api/message/read", endpoint::execute))
                .build()
                .start();

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/api/message/read"))
                    .header("Origin", "http://localhost")
                    .header("X-CSRF-Token", security.csrfToken())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var tokenMatcher = TOKEN_PATTERN.matcher(response.body());

            assertEquals(202, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElseThrow().startsWith("application/json"));
            assertTrue(tokenMatcher.find());
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertEquals(BackgroundWorkerState.RUNNING, registry.get(tokenMatcher.group(1)).state());
            assertTrue(response.body().contains("\"state\":\"RUNNING\""));
        } finally {
            release.countDown();
            server.stop();
        }
    }

    @Test
    void testExecuteRejectsMissingCsrfToken() throws Exception {
        var security = new WebSecurity("http://localhost");
        var endpoint = new ReadAllMessageEndpoint(new LocalIdentity(), security, List::of,
                new ResponseWriter(security), new BackgroundWorkerRegistry());
        var server = WebServer.builder()
                .port(0)
                .routing(routing -> routing
                        .post("/api/message/read", endpoint::execute)
                        .error(AuthorizationException.class,
                                (request, response, exception) -> response.status(403).send()))
                .build()
                .start();

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/api/message/read"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(403, response.statusCode());
        } finally {
            server.stop();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Worker was interrupted", e);
        }
    }
}
