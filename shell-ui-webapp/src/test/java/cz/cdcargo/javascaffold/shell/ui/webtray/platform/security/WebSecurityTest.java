package cz.cdcargo.javascaffold.shell.ui.webapp.platform.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

class WebSecurityTest {

    @Test
    void testCsrfToken() {
        var security = new WebSecurity("http://localhost:5402,http://127.0.0.1:5402");
        var token = security.csrfToken();
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void testParseOrigins() {
        assertEquals(Set.of("https://example.com", "http://localhost:8080"),
                WebSecurity.parseOrigins(" HTTPS://Example.COM:443/, http://localhost:8080 "));
    }

    @Test
    void testParseOriginsWithPath() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> WebSecurity.parseOrigins("https://example.com/path"));

        assertEquals("Invalid origin: https://example.com/path", exception.getMessage());
    }

    @Test
    void testWebSecurityWithNullOrigins() {
        var exception = assertThrows(NullPointerException.class, () -> new WebSecurity(null));

        assertEquals("Same origins must not be null", exception.getMessage());
    }
}
