package cz.cdcargo.javascaffold.core.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MessageTitleTest {

    private static final int MAX_LENGTH = 255;

    @ParameterizedTest
    @ValueSource(strings = { "Hello", "World" })
    void testConstructor(String value) {
        // Arrange + Act + Assert
        assertEquals(value, new MessageTitle(value).value());
    }

    @Test
    void testConstructorNull() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new MessageTitle(null));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { " ", "   ", " \t\n\u2003" })
    void testConstructorBlank(String value) {
        // Arrange + Act + Assert
        assertThrows(MessageTitleBlankException.class, () -> new MessageTitle(value));
    }

    @Test
    void testConstructorLength() {
        // Arrange + Act + Assert
        assertThrows(MessageTitleLengthException.class,
                () -> new MessageTitle("a".repeat(MAX_LENGTH + 1)));
    }
}
