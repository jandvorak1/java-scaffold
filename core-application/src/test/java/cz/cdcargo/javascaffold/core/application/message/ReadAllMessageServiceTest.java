package cz.cdcargo.javascaffold.core.application.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageID;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitle;

class ReadAllMessageServiceTest {

    @Test
    void testConstructor() {
        // Arrange
        var service = new ReadAllMessageService(() -> List.of());

        // Act + Assert
        assertEquals(List.of(), service.execute());
    }

    @Test
    void testConstructorNull() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new ReadAllMessageService(null));
    }

    @Test
    void testExecute() {
        // Arrange
        var messageList = createMessageList();
        var loadAllMessagePort = (LoadAllMessagePort) () -> messageList;
        var service = new ReadAllMessageService(loadAllMessagePort);

        // Act
        var result = service.execute();

        // Assert
        assertEquals(messageList, result);
    }

    private static List<Message> createMessageList() {
        var messagFirst = new Message(new MessageID(UUID.randomUUID()),
                new MessageTitle("First"));
        var messageSecond = new Message(new MessageID(UUID.randomUUID()),
                new MessageTitle("Second"));
        return List.of(messagFirst, messageSecond);
    }
}
