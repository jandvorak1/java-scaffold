package cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

class PrincipalsTest {

    @Test
    void testLocal() {
        var expected = new Principal(Principals.LOCAL_SUBJECT, Set.of(Role.ADMIN, Role.USER, Role.GUEST),
                Set.of(Permission.READ, Permission.WRITE));

        assertEquals(expected, Principals.local());
    }
}
