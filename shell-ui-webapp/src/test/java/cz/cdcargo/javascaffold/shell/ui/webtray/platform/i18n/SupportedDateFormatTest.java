package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class SupportedDateFormatTest {

    @Test
    void testById() {
        var result = SupportedDateFormat.byId("dd_mm_yyyy_dash");
        
        assertSame(SupportedDateFormat.DD_MM_YYYY_DASH, result);
    }

    @Test
    void testToDateTimeFormatter() {
        assertDoesNotThrow(() -> SupportedDateFormat.DD_MM_YYYY_DASH.toDateTimeFormatter());
    }
}
