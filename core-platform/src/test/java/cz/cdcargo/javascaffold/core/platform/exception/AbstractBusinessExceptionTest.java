package cz.cdcargo.javascaffold.core.platform.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class AbstractBusinessExceptionTest {

    @Test
    void testConstructorWithoutArguments() {
        var exception = new TestBusinessException();

        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessage() {
        var exception = new TestBusinessException("Business rule violated");

        assertEquals("Business rule violated", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithCause() {
        var cause = new IllegalStateException("Invalid state");
        var exception = new TestBusinessException(cause);

        assertEquals(cause.toString(), exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        var cause = new IllegalStateException("Invalid state");
        var exception = new TestBusinessException("Business rule violated", cause);

        assertEquals("Business rule violated", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    private static final class TestBusinessException extends AbstractBusinessException {

        private static final long serialVersionUID = 1L;

        private TestBusinessException() {
            super();
        }

        private TestBusinessException(String message) {
            super(message);
        }

        private TestBusinessException(Throwable cause) {
            super(cause);
        }

        private TestBusinessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
