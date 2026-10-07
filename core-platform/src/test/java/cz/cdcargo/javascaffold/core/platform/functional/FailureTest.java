package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FailureTest {

    @Test
    void testOfCreatesFailureWithoutCause() {
        var failure = Failure.of("Test");

        assertEquals("Test", failure.message());
        assertNull(failure.cause());
    }

    @Test
    void testOfCreatesFailureWithCause() {
        var cause = new RuntimeException("Cause");

        var failure = Failure.of("Test", cause);

        assertEquals("Test", failure.message());
        assertSame(cause, failure.cause());
    }

    @Test
    void testFromUsesCauseMessageAndPreservesCause() {
        var cause = new RuntimeException("Test");

        var failure = Failure.from(cause);

        assertEquals("Test", failure.message());
        assertSame(cause, failure.cause());
    }

    @Test
    void testFromUsesCauseTypeWhenMessageIsNull() {
        var cause = new RuntimeException();

        var failure = Failure.from(cause);

        assertEquals(RuntimeException.class.getName(), failure.message());
        assertSame(cause, failure.cause());
    }

    @Test
    void testOfRejectsNullMessage() {
        assertThrows(NullPointerException.class, () -> Failure.of(null));
        assertThrows(NullPointerException.class, () -> Failure.of(null, new RuntimeException()));
    }

    @Test
    void testFromRejectsNullCause() {
        assertThrows(NullPointerException.class, () -> Failure.from(null));
    }
}
