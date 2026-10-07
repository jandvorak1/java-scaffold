package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class SupportedDecimalFormatTest {

    @Test
    void testById() {
        var result = SupportedDecimalFormat.byId("space_comma");

        assertSame(SupportedDecimalFormat.SPACE_COMMA, result);
    }

    @Test
    void testToDecimalFormat() {
        var decimalFormat = SupportedDecimalFormat.SPACE_COMMA.toDecimalFormat();
        var format = SupportedDecimalFormat.SPACE_COMMA;

        assertEquals(format.pattern(), decimalFormat.toPattern());
        assertEquals(format.decimalSeparator(), decimalFormat.getDecimalFormatSymbols().getDecimalSeparator());
        assertEquals(format.groupingSeparator(), decimalFormat.getDecimalFormatSymbols().getGroupingSeparator());
        assertEquals(format.minimumFractionDigits(), decimalFormat.getMinimumFractionDigits());
        assertEquals(format.maximumFractionDigits(), decimalFormat.getMaximumFractionDigits());
    }

    @Test
    void testToWholeNumberFormat() {
        var decimalFormat = SupportedDecimalFormat.SPACE_COMMA.toWholeNumberFormat();

        assertEquals("1 235", decimalFormat.format(1234.5));
    }

    @Test
    void testToWholeNumberFormatTechnicalDoesNotGroup() {
        var decimalFormat = SupportedDecimalFormat.TECHNICAL.toWholeNumberFormat();
        
        assertEquals("1235", decimalFormat.format(1234.5));
    }
}
