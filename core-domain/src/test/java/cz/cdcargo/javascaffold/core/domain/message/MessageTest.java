package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MessageTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String TITLE = "hello";

    @Test
    void testConstructor() {
        // Arrange + Act
        var message = createMessage();

        // Assert
        assertSame(ID, message.id().value());
        assertSame(TITLE, message.title().value());
    }

    @Test
    void testEqualsForSameValues() {
        // Arrange
        var first = createMessage();
        var second = createMessage();

        // Act + Assert
        assertEquals(first, second);
    }

    private static Message createMessage() {
        return new Message(new MessageID(ID), new MessageTitle(TITLE));
    }
}
