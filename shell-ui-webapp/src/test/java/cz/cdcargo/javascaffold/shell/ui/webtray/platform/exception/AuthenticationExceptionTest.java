package cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class AuthenticationExceptionTest {

    @Test
    void testAuthenticationException() {
        var exception = new AuthenticationException();

        assertEquals("Authentication failed", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testAuthenticationExceptionWithCause() {
        var cause = new IllegalStateException("Cause");
        var exception = new AuthenticationException(cause);

        assertEquals("Authentication failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
