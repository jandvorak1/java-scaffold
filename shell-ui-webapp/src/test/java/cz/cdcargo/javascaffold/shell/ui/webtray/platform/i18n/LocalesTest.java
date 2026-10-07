package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class LocalesTest {

    @Test
    void testIsSupported() {
        assertTrue(Locales.isSupported(Locale.forLanguageTag("cs-CZ")));
        assertTrue(Locales.isSupported(Locale.forLanguageTag("en-US")));
    }

    @Test
    void testResolve() {
        var locale = Locale.forLanguageTag("cs-CZ");
        assertEquals(locale, Locales.resolve(locale));
        assertEquals(Locales.DEFAULT, Locales.resolve(Locale.forLanguageTag("de-DE")));
        assertThrows(NullPointerException.class, () -> Locales.resolve(null));
    }

    @Test
    void testSupported() {
        assertEquals(2, Locales.supported().size());
        assertTrue(Locales.supported().contains(Locale.forLanguageTag("cs-CZ")));
        assertTrue(Locales.supported().contains(Locale.forLanguageTag("en-US")));
    }

    @Test
    void testSupportedWithDisplayLocale() {
        assertEquals(List.of(Locale.forLanguageTag("cs-CZ"), Locale.forLanguageTag("en-US")),
                Locales.supported(Locale.ENGLISH));
    }
}
