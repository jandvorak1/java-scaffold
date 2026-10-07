package cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class AuthorizationExceptionTest {

    @Test
    void testAuthorizationException() {
        var exception = new AuthorizationException();

        assertEquals("Authorization failed", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testAuthorizationExceptionWithCause() {
        var cause = new IllegalStateException("Cause");
        var exception = new AuthorizationException(cause);

        assertEquals("Authorization failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
