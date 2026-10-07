package cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class PrincipalTest {

    private static final String SUBJECT = "local-user";

    private static Principal principal() {
        return new Principal(SUBJECT, Set.of(Role.USER), Set.of(Permission.READ));
    }

    @Test
    void testHasRole() {
        assertTrue(principal().hasRole(Role.USER));
    }

    @Test
    void testHasPermission() {
        assertTrue(principal().hasPermission(Permission.READ));
    }

    @Test
    void testPrincipal() {
        var principal = new Principal(SUBJECT, Set.of(Role.USER), Set.of(Permission.READ));
        assertEquals(SUBJECT, principal.subject());
        assertEquals(Set.of(Role.USER), principal.roles());
        assertEquals(Set.of(Permission.READ), principal.permissions());
    }

    @Test
    void testRoleSet() {
        assertEquals(Set.of(Role.USER), principal().roles());
    }

    @Test
    void testPermissionSet() {
        assertEquals(Set.of(Permission.READ), principal().permissions());
    }

    @Test
    void testSubject() {
        assertEquals(SUBJECT, principal().subject());
    }

    @Test
    void testPrincipalCopiesRoles() {
        var roles = new HashSet<>(Set.of(Role.USER));
        var principal = new Principal(SUBJECT, roles, Set.of());
        roles.add(Role.ADMIN);

        assertEquals(Set.of(Role.USER), principal.roles());
    }

    @Test
    void testPrincipalCopiesPermissions() {
        var permissions = new HashSet<>(Set.of(Permission.READ));
        var principal = new Principal(SUBJECT, Set.of(), permissions);
        permissions.add(Permission.WRITE);

        assertEquals(Set.of(Permission.READ), principal.permissions());
    }

    @Test
    void testPrincipalWithNullCollections() {
        var principal = new Principal(SUBJECT, null, null);

        assertEquals(Set.of(), principal.roles());
        assertEquals(Set.of(), principal.permissions());
    }

    @Test
    void testPrincipalWithBlankSubject() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> new Principal(" ", Set.of(), Set.of()));

        assertEquals("Subject must not be blank", exception.getMessage());
    }

    @Test
    void testHasRoleWithNullRole() {
        var exception = assertThrows(NullPointerException.class, () -> principal().hasRole(null));

        assertEquals("Role must not be null", exception.getMessage());
    }

    @Test
    void testHasPermissionWithNullPermission() {
        var exception = assertThrows(NullPointerException.class, () -> principal().hasPermission(null));

        assertEquals("Permission must not be null", exception.getMessage());
    }
}
