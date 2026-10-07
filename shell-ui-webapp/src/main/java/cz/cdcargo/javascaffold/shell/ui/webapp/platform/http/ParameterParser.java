package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import io.helidon.http.BadRequestException;

/**
 * Converts textual HTTP parameter values and translates conversion failures
 * into bad request errors that retain the original cause.
 */
public final class ParameterParser {

    private ParameterParser() {
    }

    /**
     * Converts a parameter value with the supplied parser.
     *
     * @param name   parameter name used in an error message
     * @param value  parameter value to convert
     * @param parser conversion function
     * @param <T>    converted value type
     * @return converted parameter value
     * @throws NullPointerException if name, value, or parser is null
     * @throws BadRequestException  if the parser rejects the value
     */
    public static <T> T parse(String name, String value, Function<String, T> parser) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        Objects.requireNonNull(value, "Parameter value must not be null");
        Objects.requireNonNull(parser, "Parser must not be null");
        try {
            var result = parser.apply(value);
            if (result == null) {
                throw new IllegalArgumentException("Parser result must not be null");
            }
            return result;
        } catch (RuntimeException exception) {
            throw new BadRequestException("Invalid parameter: " + name, exception);
        }
    }

    /**
     * Converts a textual boolean value.
     *
     * @param value value to convert; accepted values are true and false,
     *              regardless of letter case
     * @return parsed boolean value
     * @throws NullPointerException     if value is null
     * @throws IllegalArgumentException if value is not true or false
     */
    public static boolean parseBoolean(String value) {
        Objects.requireNonNull(value, "Parameter value must not be null");
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Invalid boolean value");
        };
    }
}
