package cz.cdcargo.javascaffold.shell.ui.webapp.platform.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Role;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Permission;

class AccessTokenServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
    private static final Duration SESSION_LIFETIME = Duration.ofHours(1);
    private static final Duration BEARER_LIFETIME = Duration.ofDays(1);

    private static Principal principal() {
        return new Principal("local-user", Set.of(Role.USER), Set.of(Permission.WRITE));
    }

    private static AccessTokenService service(AccessTokenStore store) {
        return new AccessTokenService(store, CLOCK, SESSION_LIFETIME, BEARER_LIFETIME);
    }

    @Test
    void testAuthenticateBearer() {
        var store = new InMemoryAccessTokenStore(CLOCK);
        var service = service(store);
        var principal = principal();
        var token = service.issueBearer(principal);

        assertEquals(principal, service.authenticateBearer(token).orElseThrow());
    }

    @Test
    void testAuthenticateSession() {
        var store = new InMemoryAccessTokenStore(CLOCK);
        var service = service(store);
        var principal = principal();
        var token = service.issueSession(principal);

        assertEquals(principal, service.authenticateSession(token).orElseThrow());
    }

    @Test
    void testIssueBearer() {
        var store = new InMemoryAccessTokenStore(CLOCK);
        var service = service(store);
        var principal = principal();
        var token = service.issueBearer(principal);

        assertFalse(token.isBlank());
        assertEquals(principal, service.authenticateBearer(token).orElseThrow());
    }

    @Test
    void testIssueSession() {
        var store = new InMemoryAccessTokenStore(CLOCK);
        var service = service(store);
        var principal = principal();
        var token = service.issueSession(principal);

        assertFalse(token.isBlank());
        assertEquals(principal, service.authenticateSession(token).orElseThrow());
    }

    @Test
    void testRemoveExpired() {
        var clock = new TestClock(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        var store = new InMemoryAccessTokenStore(clock);
        var principal = principal();
        var service = new AccessTokenService(store, clock, Duration.ofNanos(1), BEARER_LIFETIME);
        var token = service.issueSession(principal);
        clock.plus(Duration.ofNanos(1));
        service.removeExpired();

        assertTrue(service.authenticateSession(token).isEmpty());
    }

    @Test
    void testRevoke() {
        var store = new InMemoryAccessTokenStore(CLOCK);
        var service = service(store);
        var principal = principal();
        var token = service.issueSession(principal);
        service.revoke(token);

        assertTrue(service.authenticateSession(token).isEmpty());
    }

    private static final class TestClock extends Clock {

        private Instant instant;
        private final ZoneId zone;

        private TestClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void plus(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new TestClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
