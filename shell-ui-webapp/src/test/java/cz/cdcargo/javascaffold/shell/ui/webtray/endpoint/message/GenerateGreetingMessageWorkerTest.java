package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageID;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitle;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;

class GenerateGreetingMessageWorkerTest {

    private static final UUID ID = UUID.fromString("f75dfc2e-f04b-4cc2-89cc-b21e391b4582");

    @Test
    void testGenerateGreetingMessageWorkerWithNullCancellationToken() {
        var exception = assertThrows(NullPointerException.class,
                () -> new GenerateGreetingMessageWorker("token", null, GenerateGreetingMessageWorkerTest::message,
                        "Test title"));

        assertEquals("Cancellation token must not be null", exception.getMessage());
    }

    @Test
    void testGenerateGreetingMessageWorkerWithNullUseCase() {
        var exception = assertThrows(NullPointerException.class,
                () -> new GenerateGreetingMessageWorker("token", new CancellationToken(), null, "Test title"));

        assertEquals("Use case must not be null", exception.getMessage());
    }

    @Test
    void testGenerateGreetingMessageWorkerWithNullTitle() {
        var exception = assertThrows(NullPointerException.class,
                () -> new GenerateGreetingMessageWorker("token", new CancellationToken(),
                        GenerateGreetingMessageWorkerTest::message, null));

        assertEquals("Title must not be null", exception.getMessage());
    }

    @Test
    void testDoInBackground() throws Exception {
        var worker = new GenerateGreetingMessageWorker("token", new CancellationToken(),
                GenerateGreetingMessageWorkerTest::message, "Test title");

        var result = worker.doInBackground();

        assertEquals(ID.toString(), result.stringValue("id").orElseThrow());
        assertEquals("Test title", result.stringValue("title").orElseThrow());
    }

    @Test
    void testDoInBackgroundPassesTitleToUseCase() throws Exception {
        var actualTitle = new String[1];
        var worker = new GenerateGreetingMessageWorker("token", new CancellationToken(), title -> {
            actualTitle[0] = title;
            return message(title);
        }, "Test title");

        worker.doInBackground();

        assertEquals("Test title", actualTitle[0]);
    }

    @Test
    void testDoInBackgroundClearsThreadChannels() throws Exception {
        var cancellationToken = new CancellationToken();
        var worker = new GenerateGreetingMessageWorker("token", cancellationToken,
                GenerateGreetingMessageWorkerTest::message, "Test title");

        worker.doInBackground();
        cancellationToken.cancel();

        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }

    @Test
    void testOnCancelRequested() {
        var cancellationToken = new CancellationToken();
        var worker = new GenerateGreetingMessageWorker("token", cancellationToken,
                GenerateGreetingMessageWorkerTest::message, "Test title");

        worker.onCancelRequested();

        assertTrue(cancellationToken.isCancelled());
    }

    private static Message message(String title) {
        return new Message(new MessageID(ID), new MessageTitle(title));
    }
}
