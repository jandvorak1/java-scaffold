package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorker;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import io.helidon.json.JsonObject;
import io.helidon.json.JsonValue;
import io.helidon.webserver.WebServer;

class StatusJobEndpointTest {

    @TempDir
    Path tempDir;

    @Test
    void testConstructorRejectsNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new StatusJobEndpoint(null, null, null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWriter() {
        var exception = assertThrows(NullPointerException.class,
                () -> new StatusJobEndpoint(createPreferences(), null, null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullRegistry() {
        var security = new WebSecurity("http://localhost");
        var exception = assertThrows(NullPointerException.class,
                () -> new StatusJobEndpoint(createPreferences(), new ResponseWriter(security), null));

        assertEquals("Background worker registry must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var exception = assertThrows(NullPointerException.class,
                () -> createEndpoint(new BackgroundWorkerRegistry()).execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    @Test
    void testExecuteReturnsCompletedWorkerStatus() throws Exception {
        var completed = new CountDownLatch(1);
        var registry = new BackgroundWorkerRegistry();
        var worker = new BackgroundWorker<JsonObject, String>("worker-token") {
            @Override
            protected JsonObject doInBackground() {
                publish("progress");
                return JsonValue.objectBuilder().set("value", "done").build();
            }

            @Override
            protected void done(JsonObject result) {
                completed.countDown();
            }
        };
        registry.add(worker);
        worker.execute();
        assertTrue(completed.await(5, TimeUnit.SECONDS));
        var server = createServer(createEndpoint(registry));

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + server.port() + "/jobs/worker-token?offset=0"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var body = response.body();

            assertEquals(200, response.statusCode());
            assertEquals("application/json", response.headers().firstValue("Content-Type").orElseThrow());
            assertTrue(body.contains("\"token\":\"worker-token\""));
            assertTrue(body.contains("\"state\":\"DONE\""));
            assertTrue(body.contains("\"offset\":1"));
            assertTrue(body.contains("\"message\":\"progress\""));
            assertTrue(body.contains("\"result\":{\"value\":\"done\"}"));
        } finally {
            server.stop();
        }
    }

    private StatusJobEndpoint createEndpoint(BackgroundWorkerRegistry registry) {
        return new StatusJobEndpoint(createPreferences(), new ResponseWriter(new WebSecurity("http://localhost")),
                registry);
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }

    private WebServer createServer(StatusJobEndpoint endpoint) {
        return WebServer.builder()
                .port(0)
                .routing(routing -> routing.get("/jobs/{token}", endpoint::execute))
                .build()
                .start();
    }
}
