package cz.cdcargo.javascaffold.core.application.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageID;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitle;

class ReadAllMessageServiceTest {

    private static final Message FIRST_MESSAGE = createMessage(
            "bc090a73-2ca7-45a4-a279-8eed695d3e70", "First");
    private static final Message SECOND_MESSAGE = createMessage(
            "4c0cd8d8-dd77-4ade-a41a-f7cbafde0a1a", "Second");

    @Test
    void testExecutePreservesSourceOrder() {
        var service = new ReadAllMessageService(() -> List.of(FIRST_MESSAGE, SECOND_MESSAGE));

        assertEquals(List.of(FIRST_MESSAGE, SECOND_MESSAGE), service.execute());
    }

    @Test
    void testExecuteReturnsSnapshot() {
        var loadedMessages = new ArrayList<>(List.of(FIRST_MESSAGE, SECOND_MESSAGE));
        var service = new ReadAllMessageService(() -> loadedMessages);

        var result = service.execute();
        loadedMessages.clear();

        assertEquals(List.of(FIRST_MESSAGE, SECOND_MESSAGE), result);
    }

    @Test
    void testExecuteReturnsImmutableList() {
        var service = new ReadAllMessageService(() -> List.of(FIRST_MESSAGE));

        var result = service.execute();

        assertThrows(UnsupportedOperationException.class, () -> result.add(FIRST_MESSAGE));
    }

    @Test
    void testExecuteReturnsEmptyListWhenPortHasNoMessages() {
        var service = new ReadAllMessageService(List::of);

        assertEquals(List.of(), service.execute());
    }

    @Test
    void testConstructorRejectsNullPort() {
        var exception = assertThrows(NullPointerException.class, () -> new ReadAllMessageService(null));

        assertEquals("Load all messages port must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullListFromPort() {
        var service = new ReadAllMessageService(() -> null);

        var exception = assertThrows(NullPointerException.class, service::execute);

        assertEquals("Loaded messages must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullMessageFromPort() {
        var loadedMessages = new ArrayList<Message>();
        loadedMessages.add(null);
        var service = new ReadAllMessageService(() -> loadedMessages);

        var exception = assertThrows(NullPointerException.class, service::execute);

        assertEquals("Loaded messages must not contain null", exception.getMessage());
    }

    private static Message createMessage(String id, String title) {
        return new Message(new MessageID(UUID.fromString(id)), new MessageTitle(title));
    }
}
