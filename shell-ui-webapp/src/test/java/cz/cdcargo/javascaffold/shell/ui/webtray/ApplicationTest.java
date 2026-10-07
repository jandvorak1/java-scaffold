package cz.cdcargo.javascaffold.shell.ui.webapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ApplicationTest {

    @Test
    void testApplicationWithNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new Application(null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }
}
