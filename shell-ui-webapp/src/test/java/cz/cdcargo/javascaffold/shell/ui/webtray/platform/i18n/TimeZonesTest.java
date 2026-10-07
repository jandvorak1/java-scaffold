package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class TimeZonesTest {

    @Test
    void testIsSupported() {
        assertTrue(TimeZones.isSupported(ZoneId.of("Europe/Prague")));
        assertFalse(TimeZones.isSupported(ZoneId.of("UTC")));
    }

    @Test
    void testResolve() {
        var zone = ZoneId.of("Europe/Berlin");
        
        assertEquals(zone, TimeZones.resolve(ZoneId.of("Europe/Berlin")));
        assertThrows(NullPointerException.class, () -> TimeZones.resolve(null));
    }

    @Test
    void testSupported() {
        assertTrue(TimeZones.supported().contains(ZoneId.of("Europe/Prague")));
        assertTrue(TimeZones.supported().contains(ZoneId.of("America/New_York")));
    }

    @Test
    void testSupportedExcludesZoneWithMoreThanTwoParts() {
        assertFalse(TimeZones.supported().contains(ZoneId.of("America/Argentina/Buenos_Aires")));
    }
}
