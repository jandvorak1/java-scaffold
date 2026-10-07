package cz.cdcargo.javascaffold.infra.db.sqlite.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class LoadAllMessageFailedExceptionTest {

    @Test
    void testConstructorSetsMessageAndPreservesCause() {
        var cause = new IllegalStateException("Failure");

        var exception = new LoadAllMessageFailedException(cause);

        assertEquals("Loading all messages failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
