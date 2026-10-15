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
    void testConstructorNullValue() {
        // Arrange + Act + Assert
        assertEquals("Value must not be null",
                assertThrows(NullPointerException.class, () -> new MessageID(null)).getMessage());
    }
}
