package cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Role;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Permission;
import io.helidon.webserver.http.ServerRequest;

class IdentityTest {

    private static final Principal PRINCIPAL = new Principal("local-user", Set.of(Role.USER), Set.of(Permission.READ));

    private final Identity identity = new TestIdentity();

    @Test
    void testRequireRole() {
        assertDoesNotThrow(() -> identity.requireRole(PRINCIPAL, Role.USER));
    }

    @Test
    void testRequirePermission() {
        assertDoesNotThrow(() -> identity.requirePermission(PRINCIPAL, Permission.READ));
    }

    @Test
    void testRequireRoleWithoutRole() {
        assertThrows(AuthorizationException.class, () -> identity.requireRole(PRINCIPAL, Role.ADMIN));
    }

    @Test
    void testRequirePermissionWithoutPermission() {
        assertThrows(AuthorizationException.class, () -> identity.requirePermission(PRINCIPAL, Permission.WRITE));
    }

    @Test
    void testRequireRoleWithNullPrincipal() {
        var exception = assertThrows(NullPointerException.class,
                () -> identity.requireRole((Principal) null, Role.USER));

        assertEquals("Principal must not be null", exception.getMessage());
    }

    @Test
    void testRequirePermissionWithNullPermission() {
        var exception = assertThrows(NullPointerException.class, () -> identity.requirePermission(PRINCIPAL, null));

        assertEquals("Permission must not be null", exception.getMessage());
    }

    private static final class TestIdentity implements Identity {

        @Override
        public Principal requirePrincipal(ServerRequest req) {
            return PRINCIPAL;
        }
    }
}
