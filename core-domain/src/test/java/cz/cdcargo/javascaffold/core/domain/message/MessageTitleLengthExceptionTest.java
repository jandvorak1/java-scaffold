package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MessageTitleLengthExceptionTest {

    @Test
    void testConstructor() {
        // Arrange + Act
        var exception = new MessageTitleLengthException(255);

        // Assert
        assertEquals(255, exception.maxLength());
        assertEquals("Maximum length must not exceed 255 characters", exception.getMessage());
    }

    @Test
    void testConstructorZeroMaxLength() {
        // Arrange + Act
        var exception = new MessageTitleLengthException(0);

        // Assert
        assertEquals(0, exception.maxLength());
        assertEquals("Maximum length must not exceed 0 characters", exception.getMessage());
    }

    @Test
    void testConstructorNegativeMaxLength() {
        // Arrange + Act + Assert
        assertEquals("Maximum length must not be negative", assertThrows(IllegalArgumentException.class,
                () -> new MessageTitleLengthException(-1)).getMessage());
    }
}
