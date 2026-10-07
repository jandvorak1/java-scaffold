package cz.cdcargo.javascaffold.shell.ui.webapp.platform.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class SessionCookieTest {

    @Test
    void testClear() {
        var cookie = new SessionCookie("TEST_SESSION", true);
        var setCookie = cookie.clear();

        assertEquals("TEST_SESSION", setCookie.name());
        assertEquals("", setCookie.value());
        assertTrue(setCookie.httpOnly());
        assertTrue(setCookie.secure());
        assertEquals(io.helidon.http.SetCookie.SameSite.STRICT, setCookie.sameSite().orElseThrow());
        assertEquals("/", setCookie.path().orElseThrow());
        assertEquals(Duration.ZERO, setCookie.maxAge().orElseThrow());
    }

    @Test
    void testCreate() {
        var cookie = new SessionCookie("TEST_SESSION", false);
        var setCookie = cookie.create("session-token", Duration.ofHours(1));

        assertEquals("TEST_SESSION", setCookie.name());
        assertEquals("session-token", setCookie.value());
        assertTrue(setCookie.httpOnly());
        assertFalse(setCookie.secure());
        assertEquals(io.helidon.http.SetCookie.SameSite.STRICT, setCookie.sameSite().orElseThrow());
        assertEquals("/", setCookie.path().orElseThrow());
        assertEquals(Duration.ofHours(1), setCookie.maxAge().orElseThrow());
    }

    @Test
    void testName() {
        var cookie = new SessionCookie("TEST_SESSION", false);
        assertEquals("TEST_SESSION", cookie.name());
    }

    @Test
    void testSessionCookieWithInvalidName() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> new SessionCookie("TEST=SESSION", true));

        assertEquals("Invalid cookie name: TEST=SESSION", exception.getMessage());
    }

    @Test
    void testCreateWithNonPositiveMaxAge() {
        var cookie = new SessionCookie("TEST_SESSION", true);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> cookie.create("session-token", Duration.ZERO));

        assertEquals("Max age must be positive", exception.getMessage());
    }
}
