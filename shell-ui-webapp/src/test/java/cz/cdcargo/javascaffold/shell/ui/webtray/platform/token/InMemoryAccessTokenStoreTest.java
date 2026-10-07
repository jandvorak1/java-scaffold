package cz.cdcargo.javascaffold.shell.ui.webapp.platform.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

class InMemoryAccessTokenStoreTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private static Principal principal() {
        return new Principal("local-user", Set.of(Role.USER), Set.of(Permission.READ));
    }

    @Test
    void testFindPrincipal() {
        var clock = new TestClock(NOW, ZoneOffset.UTC);
        var store = new InMemoryAccessTokenStore(clock);
        var principal = principal();

        store.save("session-token", AccessTokenType.SESSION, principal, NOW.plus(Duration.ofHours(1)));

        assertEquals(principal, store.findPrincipal("session-token", AccessTokenType.SESSION).orElseThrow());
    }

    @Test
    void testFindPrincipalWithNullType() {
        var store = new InMemoryAccessTokenStore(Clock.fixed(NOW, ZoneOffset.UTC));
        var exception = assertThrows(NullPointerException.class, () -> store.findPrincipal("", null));

        assertEquals("Type must not be null", exception.getMessage());
    }

    @Test
    void testRemoveExpired() {
        var clock = new TestClock(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        var store = new InMemoryAccessTokenStore(clock);
        var principal = principal();

        store.save("session-token", AccessTokenType.SESSION, principal, NOW.plus(Duration.ofSeconds(1)));
        clock.plus(Duration.ofSeconds(2));
        store.removeExpired();

        assertTrue(store.findPrincipal("session-token", AccessTokenType.SESSION).isEmpty());
    }

    @Test
    void testRevoke() {
        var clock = Clock.fixed(NOW, ZoneId.of("UTC"));
        var store = new InMemoryAccessTokenStore(clock);
        var principal = principal();

        store.save("session-token", AccessTokenType.SESSION, principal, NOW.plus(Duration.ofHours(1)));
        store.revoke("session-token");

        assertTrue(store.findPrincipal("session-token", AccessTokenType.SESSION).isEmpty());
    }

    @Test
    void testSave() {
        var clock = Clock.fixed(NOW, ZoneId.of("UTC"));
        var store = new InMemoryAccessTokenStore(clock);
        var principal = principal();

        store.save("session-token", AccessTokenType.SESSION, principal, NOW.plus(Duration.ofHours(1)));

        assertEquals(principal, store.findPrincipal("session-token", AccessTokenType.SESSION).orElseThrow());
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
