package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class SupportedDateTimeFormatTest {

    @Test
    void testById() {
        var result = SupportedDateTimeFormat.byId("dd_mm_yyyy_dash_hh_mm_ss");
        
        assertSame(SupportedDateTimeFormat.DD_MM_YYYY_DASH_HH_MM_SS, result);
    }

    @Test
    void testToDateTimeFormatter() {
        assertDoesNotThrow(() -> SupportedDateTimeFormat.DD_MM_YYYY_DASH_HH_MM_SS.toDateTimeFormatter());
    }
}
