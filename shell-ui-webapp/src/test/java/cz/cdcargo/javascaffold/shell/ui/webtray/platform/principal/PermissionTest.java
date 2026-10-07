package cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PermissionTest {

    @Test
    void testId() {
        assertEquals("write", Permission.WRITE.id());
    }
}
