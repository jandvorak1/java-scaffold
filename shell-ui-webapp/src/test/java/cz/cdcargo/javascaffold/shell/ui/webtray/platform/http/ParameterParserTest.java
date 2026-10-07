package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.helidon.http.BadRequestException;

class ParameterParserTest {

    @Test
    void testParse() {
        var output = ParameterParser.parse("count", "123", Integer::parseInt);

        assertEquals(123, output);
    }

    @Test
    void testParseInvalid() {
        var exception = assertThrows(BadRequestException.class,
                () -> ParameterParser.parse("count", "abc", Integer::parseInt));

        assertEquals("Invalid parameter: count", exception.getMessage());
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    @Test
    void testParseNull() {
        var exception = assertThrows(NullPointerException.class, () -> ParameterParser.parse("count", "123", null));

        assertEquals("Parser must not be null", exception.getMessage());
    }

    @Test
    void testParseWithNullResult() {
        var exception = assertThrows(BadRequestException.class,
                () -> ParameterParser.parse("value", "input", ignored -> null));

        assertEquals("Invalid parameter: value", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void testParseBoolean() {
        var output = ParameterParser.parseBoolean("true");
        assertEquals(true, output);
    }
}
