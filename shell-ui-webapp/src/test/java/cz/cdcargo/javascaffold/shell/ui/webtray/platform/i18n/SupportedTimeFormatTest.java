package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SupportedTimeFormatTest {

    @Test
    void testById() {
        var result = SupportedTimeFormat.byId("hh_mm_ss_24");
        
        assertSame(SupportedTimeFormat.HH_MM_SS_24, result);
    }

    @Test
    void testToDateTimeFormatter() {
        assertDoesNotThrow(() -> SupportedTimeFormat.HH_MM_SS_24.toDateTimeFormatter());
    }

    @Test
    void testByIdInvalid() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> SupportedTimeFormat.byId("invalid"));

        assertEquals("No time format found for ID: invalid", exception.getMessage());
    }
}
