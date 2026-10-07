package cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.session.SessionCookie;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.AccessTokenService;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.InMemoryAccessTokenStore;

class SessionCookieIdentityTest {

    @Test
    void testSessionCookieIdentityWithNullService() {
        var exception = assertThrows(NullPointerException.class,
                () -> new SessionCookieIdentity(null, new SessionCookie("session", true)));

        assertEquals("Access token service must not be null", exception.getMessage());
    }

    @Test
    void testSessionCookieIdentityWithNullCookie() {
        var service = new AccessTokenService(new InMemoryAccessTokenStore(), Clock.systemUTC(), Duration.ofHours(1),
                Duration.ofHours(1));
        var exception = assertThrows(NullPointerException.class, () -> new SessionCookieIdentity(service, null));

        assertEquals("Session cookie must not be null", exception.getMessage());
    }
}
