package cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void testId() {
        assertEquals("admin", Role.ADMIN.id());
    }
}
