package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

class FailureTest {

    @Test
    void testOf() {
        // Arrange
        var failure = Failure.of("Test");

        // Act + Assert
        assertEquals("Test", failure.message());
        assertNull(failure.cause());
    }

    @Test
    void testOfWithCause() {
        // Arrange
        var cause = new RuntimeException("Cause");
        var failure = Failure.of("Test", cause);

        // Act + Assert
        assertEquals("Test", failure.message());
        assertSame(cause, failure.cause());
    }

    @Test
    void testFrom() {
        // Arrange
        var cause = new RuntimeException("Test");
        var failure = Failure.from(cause);

        // Act + Assert
        assertEquals("Test", failure.message());
        assertSame(cause, failure.cause());
    }
}
