package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageID;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitle;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;

class ReadAllMessageWorkerTest {

    @Test
    void testReadAllMessageWorkerWithNullCancellationToken() {
        var exception = assertThrows(NullPointerException.class,
                () -> new ReadAllMessageWorker("token", null, List::of));

        assertEquals("Cancellation token must not be null", exception.getMessage());
    }

    @Test
    void testReadAllMessageWorkerWithNullUseCase() {
        var exception = assertThrows(NullPointerException.class,
                () -> new ReadAllMessageWorker("token", new CancellationToken(), null));

        assertEquals("Use case must not be null", exception.getMessage());
    }

    @Test
    void testDoInBackground() throws Exception {
        var id = UUID.fromString("f75dfc2e-f04b-4cc2-89cc-b21e391b4582");
        var message = new Message(new MessageID(id), new MessageTitle("Test title"));
        var worker = new ReadAllMessageWorker("token", new CancellationToken(), () -> List.of(message));

        var result = worker.doInBackground();

        var records = result.arrayValue("records").orElseThrow();
        var record = records.get(0).orElseThrow().asObject();
        assertEquals(1, records.values().size());
        assertEquals(id.toString(), record.stringValue("id").orElseThrow());
        assertEquals("Test title", record.stringValue("title").orElseThrow());
    }

    @Test
    void testDoInBackgroundClearsThreadChannels() throws Exception {
        var cancellationToken = new CancellationToken();
        var worker = new ReadAllMessageWorker("token", cancellationToken, List::of);

        worker.doInBackground();
        cancellationToken.cancel();

        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }

    @Test
    void testOnCancelRequested() {
        var cancellationToken = new CancellationToken();
        var worker = new ReadAllMessageWorker("token", cancellationToken, List::of);

        worker.onCancelRequested();

        assertTrue(cancellationToken.isCancelled());
    }
}
