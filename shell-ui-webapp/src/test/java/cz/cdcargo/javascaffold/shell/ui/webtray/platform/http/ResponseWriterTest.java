package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ResponseWriterTest {

    @Test
    void testResponseWriterWithNullWebSecurity() {
        var exception = assertThrows(NullPointerException.class, () -> new ResponseWriter(null));

        assertEquals("Web security must not be null", exception.getMessage());
    }
}
