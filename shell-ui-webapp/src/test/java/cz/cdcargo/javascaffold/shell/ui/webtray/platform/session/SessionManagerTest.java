package cz.cdcargo.javascaffold.shell.ui.webapp.platform.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.AccessTokenService;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.InMemoryAccessTokenStore;

class SessionManagerTest {

    @Test
    void testSessionManagerWithNonPositiveLifetime() {
        var tokenService = new AccessTokenService(new InMemoryAccessTokenStore(Clock.systemUTC()), Clock.systemUTC(),
                Duration.ofHours(1), Duration.ofHours(1));
        var exception = assertThrows(IllegalArgumentException.class,
                () -> new SessionManager(tokenService, new SessionCookie("TEST_SESSION", true), Duration.ZERO));

        assertEquals("Session lifetime must be positive", exception.getMessage());
    }
}
