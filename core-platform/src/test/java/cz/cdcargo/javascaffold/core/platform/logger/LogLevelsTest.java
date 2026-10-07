package cz.cdcargo.javascaffold.core.platform.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.logging.Level;

import org.junit.jupiter.api.Test;

class LogLevelsTest {

    @Test
    void testParseMapsApplicationAliases() {
        assertEquals(Level.SEVERE, LogLevels.parse("ERROR", Level.INFO));
        assertEquals(Level.WARNING, LogLevels.parse("WARN", Level.INFO));
        assertEquals(Level.WARNING, LogLevels.parse("WARNING", Level.INFO));
        assertEquals(Level.FINE, LogLevels.parse("DEBUG", Level.INFO));
        assertEquals(Level.FINEST, LogLevels.parse("TRACE", Level.INFO));
    }

    @Test
    void testParseMapsStandardJavaLevels() {
        assertEquals(Level.OFF, LogLevels.parse("OFF", Level.INFO));
        assertEquals(Level.SEVERE, LogLevels.parse("SEVERE", Level.INFO));
        assertEquals(Level.INFO, LogLevels.parse("INFO", Level.WARNING));
        assertEquals(Level.CONFIG, LogLevels.parse("CONFIG", Level.INFO));
        assertEquals(Level.FINE, LogLevels.parse("FINE", Level.INFO));
        assertEquals(Level.FINER, LogLevels.parse("FINER", Level.INFO));
        assertEquals(Level.FINEST, LogLevels.parse("FINEST", Level.INFO));
        assertEquals(Level.ALL, LogLevels.parse("ALL", Level.INFO));
    }

    @Test
    void testParseMapsNumericJavaLevel() {
        assertEquals(Level.INFO, LogLevels.parse("800", Level.WARNING));
    }

    @Test
    void testParseIgnoresWhitespaceAndLetterCase() {
        assertEquals(Level.WARNING, LogLevels.parse("  wArN  ", Level.INFO));
    }

    @Test
    void testParseReturnsDefaultForMissingOrUnknownValue() {
        assertEquals(Level.CONFIG, LogLevels.parse(null, Level.CONFIG));
        assertEquals(Level.CONFIG, LogLevels.parse(" \t", Level.CONFIG));
        assertEquals(Level.CONFIG, LogLevels.parse("unknown", Level.CONFIG));
    }

    @Test
    void testParseRejectsNullDefaultLevel() {
        assertThrows(NullPointerException.class, () -> LogLevels.parse("INFO", null));
    }
}
