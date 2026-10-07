package cz.cdcargo.javascaffold.infra.db.inmemory.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class LoadAllMessageFailedExceptionTest {

    @Test
    void testConstructorRetainsMessageAndCause() {
        var cause = new RuntimeException("Failure");

        var exception = new LoadAllMessageFailedException(cause);

        assertEquals("Loading all messages failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
