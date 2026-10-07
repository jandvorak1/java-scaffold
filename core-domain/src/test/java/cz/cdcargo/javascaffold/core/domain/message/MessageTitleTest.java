package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MessageTitleTest {

    @Test
    void testConstructorStoresValueWithoutModification() {
        var value = "  World  ";
        var title = new MessageTitle(value);

        assertEquals(value, title.value());
    }

    @Test
    void testConstructorRejectsNullValue() {
        var exception = assertThrows(NullPointerException.class, () -> new MessageTitle(null));

        assertEquals("Value must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsEmptyValue() {
        assertThrows(MessageTitleBlankException.class, () -> new MessageTitle(""));
    }

    @Test
    void testConstructorRejectsWhitespaceOnlyValue() {
        assertThrows(MessageTitleBlankException.class, () -> new MessageTitle(" \t\n"));
    }

    @Test
    void testConstructorAcceptsMaximumLength() {
        var value = "😀".repeat(255);
        var title = new MessageTitle(value);

        assertEquals(value, title.value());
    }

    @Test
    void testConstructorRejectsValueAboveMaximumLength() {
        var value = "😀".repeat(256);

        var exception = assertThrows(MessageTitleLengthException.class,
                () -> new MessageTitle(value));

        assertEquals(255, exception.maxLength());
    }
}
