package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessageTitleBlankExceptionTest {

    @Test
    void testConstructorSetsDiagnosticMessage() {
        var exception = new MessageTitleBlankException();

        assertEquals("Value must not be blank", exception.getMessage());
    }
}
