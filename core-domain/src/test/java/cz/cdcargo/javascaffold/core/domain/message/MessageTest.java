package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void testConstructorStoresComponents() {
        var id = new MessageID(UUID.randomUUID());
        var title = new MessageTitle("hello");
        var message = new Message(id, title);

        assertSame(id, message.id());
        assertSame(title, message.title());
    }

    @Test
    void testConstructorRejectsNullId() {
        var title = new MessageTitle("hello");

        var exception = assertThrows(NullPointerException.class, () -> new Message(null, title));

        assertEquals("ID must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullTitle() {
        var id = new MessageID(UUID.randomUUID());

        var exception = assertThrows(NullPointerException.class, () -> new Message(id, null));

        assertEquals("Title must not be null", exception.getMessage());
    }
}
