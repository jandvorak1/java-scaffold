package cz.cdcargo.javascaffold.shell.ui.webapp.platform.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Role;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Permission;

class AccessTokenTest {

    private static final String HASH = "token-hash";
    private static final AccessTokenType TYPE = AccessTokenType.SESSION;
    private static final Instant EXPIRES_AT = Instant.parse("2030-01-01T00:00:00Z");

    private static Principal principal() {
        return new Principal("local-user", Set.of(Role.USER), Set.of(Permission.READ));
    }

    private static AccessToken token() {
        return new AccessToken(HASH, TYPE, principal(), EXPIRES_AT);
    }

    @Test
    void testExpiresAt() {
        assertEquals(EXPIRES_AT, token().expiresAt());
    }

    @Test
    void testHash() {
        assertEquals(HASH, token().hash());
    }

    @Test
    void testType() {
        assertEquals(TYPE, token().type());
    }

    @Test
    void testIsExpired() {
        var clock = Clock.fixed(Instant.parse("2029-01-01T00:00:00Z"), ZoneOffset.UTC);
        assertFalse(token().isExpired(clock.instant()));
    }

    @Test
    void testIsExpiredAtExpirationInstant() {
        assertTrue(token().isExpired(EXPIRES_AT));
    }

    @Test
    void testAccessToken() {
        var principal = principal();
        var token = new AccessToken(HASH, TYPE, principal, EXPIRES_AT);

        assertEquals(HASH, token.hash());
        assertEquals(TYPE, token.type());
        assertEquals(principal, token.principal());
        assertEquals(EXPIRES_AT, token.expiresAt());
    }
}
