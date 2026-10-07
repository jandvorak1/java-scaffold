package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MessageTitleLengthExceptionTest {

    @Test
    void testConstructorStoresMaximumLength() {
        var maxLength = 10;
        var exception = new MessageTitleLengthException(maxLength);

        assertEquals(maxLength, exception.maxLength());
    }

    @Test
    void testConstructorSetsDiagnosticMessage() {
        var exception = new MessageTitleLengthException(10);

        assertEquals("Message title length must not exceed 10", exception.getMessage());
    }

    @Test
    void testConstructorAcceptsZeroMaximumLength() {
        var exception = new MessageTitleLengthException(0);

        assertEquals(0, exception.maxLength());
    }

    @Test
    void testConstructorRejectsNegativeMaximumLength() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> new MessageTitleLengthException(-1));

        assertEquals("Max length must not be negative", exception.getMessage());
    }
}
