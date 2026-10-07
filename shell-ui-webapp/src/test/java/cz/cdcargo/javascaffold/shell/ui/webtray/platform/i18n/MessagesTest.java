package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class MessagesTest {
    private static final Locale CZECH_LOCALE = Locale.forLanguageTag("cs-CZ");
    private static final Locale ENGLISH_LOCALE = Locale.forLanguageTag("en-US");
    private static final String ACTION_KEY = "main.ok.action";
    private static final String DIALOG_MESSAGE_KEY = "application.tray.dialog.message";

    @Test
    void testGet() {
        var messages = new Messages(ENGLISH_LOCALE);

        assertEquals("OK", messages.get(ACTION_KEY));
    }

    @Test
    void testGetWithArguments() {
        var messages = new Messages(ENGLISH_LOCALE);

        assertEquals("Test version 1.0.0", messages.get(DIALOG_MESSAGE_KEY, "Test", "1.0.0"));
    }

    @Test
    void testGetMissing() {
        var messages = new Messages(ENGLISH_LOCALE);

        assertEquals("missing", messages.get("missing"));
    }

    @Test
    void testGetWithNullArguments() {
        var messages = new Messages(ENGLISH_LOCALE);
        var exception = assertThrows(NullPointerException.class,
                () -> messages.get(DIALOG_MESSAGE_KEY, (Object[]) null));

        assertEquals("Arguments must not be null", exception.getMessage());
    }

    @Test
    void testLocale() {
        var messages = new Messages(CZECH_LOCALE);
        
        assertEquals(CZECH_LOCALE, messages.locale());
    }
}
