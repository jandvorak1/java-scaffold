package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageID;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitle;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerState;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity.LocalIdentity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.webserver.WebServer;

class GenerateGreetingMessageEndpointTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\\"token\\\":\\\"([^\\\"]+)\\\"");

    @Test
    void testExecute() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var receivedTitle = new AtomicReference<String>();
        var registry = new BackgroundWorkerRegistry();
        var security = new WebSecurity("http://localhost");
        var endpoint = new GenerateGreetingMessageEndpoint(new LocalIdentity(), security, title -> {
            receivedTitle.set(title);
            started.countDown();
            await(release);
            return new Message(new MessageID(UUID.fromString("f75dfc2e-f04b-4cc2-89cc-b21e391b4582")),
                    new MessageTitle(title));
        }, new ResponseWriter(security), registry);
        var server = WebServer.builder()
                .port(0)
                .routing(routing -> routing.post("/api/message/greeting", endpoint::execute))
                .build()
                .start();

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/api/message/greeting?title=Ignored"))
                    .header("Origin", "http://localhost")
                    .header("X-CSRF-Token", security.csrfToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Jane Doe\"}"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var tokenMatcher = TOKEN_PATTERN.matcher(response.body());

            assertEquals(202, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElseThrow().startsWith("application/json"));
            assertTrue(tokenMatcher.find());
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertEquals("Jane Doe", receivedTitle.get());
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
        var endpoint = new GenerateGreetingMessageEndpoint(new LocalIdentity(), security,
                title -> new Message(
                        new MessageID(UUID.fromString("f75dfc2e-f04b-4cc2-89cc-b21e391b4582")),
                        new MessageTitle(title)),
                new ResponseWriter(security), new BackgroundWorkerRegistry());
        var server = WebServer.builder()
                .port(0)
                .routing(routing -> routing
                        .post("/api/message/greeting", endpoint::execute)
                        .error(AuthorizationException.class,
                                (request, response, exception) -> response.status(403).send()))
                .build()
                .start();

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/api/message/greeting"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Jane Doe\"}"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(403, response.statusCode());
        } finally {
            server.stop();
        }
    }

    @Test
    void testExecuteRejectsMissingTitle() throws Exception {
        assertInvalidBody("{}");
    }

    @Test
    void testExecuteRejectsBlankTitle() throws Exception {
        assertInvalidBody("{\"title\":\"   \"}");
    }

    @Test
    void testExecuteRejectsNonStringTitle() throws Exception {
        assertInvalidBody("{\"title\":42}");
    }

    @Test
    void testExecuteRejectsMalformedJson() throws Exception {
        assertInvalidBody("{");
    }

    private static void assertInvalidBody(String body) throws Exception {
        var invoked = new AtomicBoolean();
        var security = new WebSecurity("http://localhost");
        var endpoint = new GenerateGreetingMessageEndpoint(new LocalIdentity(), security, title -> {
            invoked.set(true);
            return new Message(new MessageID(UUID.randomUUID()), new MessageTitle(title));
        }, new ResponseWriter(security), new BackgroundWorkerRegistry());
        var server = WebServer.builder()
                .host("localhost")
                .port(0)
                .routing(routing -> routing.post("/api/message/greeting", endpoint::execute))
                .build()
                .start();

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/api/message/greeting?title=Ignored"))
                    .header("Origin", "http://localhost")
                    .header("X-CSRF-Token", security.csrfToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertEquals(400, response.statusCode());
            assertFalse(invoked.get());
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
