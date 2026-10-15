package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FailureTest {

    @Test
    void testOfCreatesFailureWithoutCause() {
        // Arrange
        var failure = Failure.of("Test");

        // Act + Assert
        assertEquals("Test", failure.message());
        assertNull(failure.cause());
    }

    @Test
    void testOfCreatesFailureWithCause() {
        // Arrange
        var cause = new RuntimeException("Cause");
        var failure = Failure.of("Test", cause);

        // Act + Assert
        assertEquals("Test", failure.message());
        assertSame(cause, failure.cause());
    }

    @Test
    void testFromUsesCauseMessageAndPreservesCause() {
        // Arrange
        var cause = new RuntimeException("Test");
        var failure = Failure.from(cause);

        // Act + Assert
        assertEquals("Test", failure.message());
        assertSame(cause, failure.cause());
    }

    @Test
    void testFromUsesCauseTypeWhenMessageIsNull() {
        // Arrange
        var cause = new RuntimeException();
        var failure = Failure.from(cause);

        // Act + Assert
        assertEquals(RuntimeException.class.getName(), failure.message());
        assertSame(cause, failure.cause());
    }

    @Test
    void testOfRejectsNullMessage() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> Failure.of(null));
        assertThrows(NullPointerException.class, () -> Failure.of(null, new RuntimeException()));
    }

    @Test
    void testFromRejectsNullCause() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> Failure.from(null));
    }
}
