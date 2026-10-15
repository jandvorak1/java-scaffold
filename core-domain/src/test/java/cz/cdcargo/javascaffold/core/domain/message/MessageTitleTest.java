package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MessageTitleTest {

    @Test
    void testConstructor() {
        // Arrange
        var value = "World";

        // Act + Assert
        assertEquals(value, new MessageTitle(value).value());
    }

    @Test
    void testConstructorMaxLength() {
        // Arrange
        var value = "a".repeat(255);

        // Act + Assert
        assertEquals(value, new MessageTitle(value).value());
    }

    @Test
    void testConstructorTooLong() {
        // Arrange + Act
        var exception = assertThrows(MessageTitleLengthException.class,
                () -> new MessageTitle("a".repeat(256)));

        // Assert
        assertEquals(255, exception.maxLength());
    }

    @Test
    void testConstructorBlank() {
        // Arrange + Act + Assert
        assertThrows(MessageTitleBlankException.class, () -> new MessageTitle(""));
        assertThrows(MessageTitleBlankException.class, () -> new MessageTitle(" \t\n\u2003"));
    }

    @Test
    void testConstructorNullValue() {
        // Arrange + Act + Assert
        assertEquals("Value must not be null",
                assertThrows(NullPointerException.class, () -> new MessageTitle(null)).getMessage());
    }
}
