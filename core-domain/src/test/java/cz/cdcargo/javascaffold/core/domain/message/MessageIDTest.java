package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MessageIDTest {

    @Test
    void testConstructor() {
        // Arrange
        var value = UUID.randomUUID();

        // Act + Assert
        assertEquals(value, new MessageID(value).value());
    }

    @Test
    void testConstructorNull() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new MessageID(null));
    }
}
