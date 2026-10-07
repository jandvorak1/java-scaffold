package cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principals;

class LocalIdentityTest {

    @Test
    void testRequirePrincipal() {
        var identity = new LocalIdentity();
        var principal = identity.requirePrincipal(null);
        assertEquals(Principals.local(), principal);
    }
}
