package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.helidon.common.parameters.Parameters;
import io.helidon.http.BadRequestException;

/**
 * Provides shared conversion and validation for URI parameter collections.
 *
 * Subclasses identify the parameter source used in client-facing validation
 * errors. Missing required parameters and values rejected by a parser produce
 * a bad request exception.
 */
public abstract class AbstractURIParameters {

    private final Parameters parameters;
    private final String parameterSource;

    /**
     * Creates a parameter accessor backed by the supplied parameters.
     *
     * @param parameters      source of URI parameters
     * @param parameterSource source name used in validation messages
     * @throws NullPointerException if parameters or parameterSource is null
     */
    protected AbstractURIParameters(Parameters parameters, String parameterSource) {
        this.parameters = Objects.requireNonNull(parameters, "URI parameters must not be null");
        this.parameterSource = Objects.requireNonNull(parameterSource, "Parameter source must not be null");
    }

    /**
     * Returns a required string parameter.
     *
     * @param name parameter name
     * @return parameter value
     * @throws BadRequestException  if the parameter is missing
     * @throws NullPointerException if name is null
     */
    public String requiredString(String name) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        return optionalString(name).orElseThrow(() -> missing(name));
    }

    /**
     * Returns an optional string parameter.
     *
     * @param name parameter name
     * @return parameter value when present
     * @throws NullPointerException if name is null
     */
    public Optional<String> optionalString(String name) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        return parameters.first(name).asOptional();
    }

    /**
     * Returns a required string parameter that contains non-whitespace text.
     *
     * @param name parameter name
     * @return non-blank parameter value
     * @throws BadRequestException  if the parameter is missing or blank
     * @throws NullPointerException if name is null
     */
    public String requiredNonBlankString(String name) {
        var value = requiredString(name);
        if (value.isBlank()) {
            throw invalid(name);
        }
        return value;
    }

    /**
     * Returns a required integer parameter.
     *
     * @param name parameter name
     * @return parsed integer value
     */
    public int requiredInt(String name) {
        return required(name, Integer::parseInt);
    }

    /**
     * Returns an optional integer parameter.
     *
     * @param name parameter name
     * @return parsed integer value when present
     */
    public Optional<Integer> optionalInt(String name) {
        return optional(name, Integer::parseInt);
    }

    /**
     * Returns a required long parameter.
     *
     * @param name parameter name
     * @return parsed long value
     */
    public long requiredLong(String name) {
        return required(name, Long::parseLong);
    }

    /**
     * Returns an optional long parameter.
     *
     * @param name parameter name
     * @return parsed long value when present
     */
    public Optional<Long> optionalLong(String name) {
        return optional(name, Long::parseLong);
    }

    /**
     * Returns a required double parameter.
     *
     * @param name parameter name
     * @return parsed double value
     */
    public double requiredDouble(String name) {
        return required(name, Double::parseDouble);
    }

    /**
     * Returns an optional double parameter.
     *
     * @param name parameter name
     * @return parsed double value when present
     */
    public Optional<Double> optionalDouble(String name) {
        return optional(name, Double::parseDouble);
    }

    /**
     * Returns a required decimal parameter.
     *
     * @param name parameter name
     * @return parsed decimal value
     */
    public BigDecimal requiredBigDecimal(String name) {
        return required(name, BigDecimal::new);
    }

    /**
     * Returns an optional decimal parameter.
     *
     * @param name parameter name
     * @return parsed decimal value when present
     */
    public Optional<BigDecimal> optionalBigDecimal(String name) {
        return optional(name, BigDecimal::new);
    }

    /**
     * Returns a required boolean parameter.
     *
     * @param name parameter name
     * @return parsed boolean value
     */
    public boolean requiredBoolean(String name) {
        return required(name, ParameterParser::parseBoolean);
    }

    /**
     * Returns an optional boolean parameter.
     *
     * @param name parameter name
     * @return parsed boolean value when present
     */
    public Optional<Boolean> optionalBoolean(String name) {
        return optional(name, ParameterParser::parseBoolean);
    }

    /**
     * Returns a required local date parameter.
     *
     * @param name parameter name
     * @return parsed local date value
     */
    public LocalDate requiredLocalDate(String name) {
        return required(name, LocalDate::parse);
    }

    /**
     * Returns an optional local date parameter.
     *
     * @param name parameter name
     * @return parsed local date value when present
     */
    public Optional<LocalDate> optionalLocalDate(String name) {
        return optional(name, LocalDate::parse);
    }

    /**
     * Returns a required local time parameter.
     *
     * @param name parameter name
     * @return parsed local time value
     */
    public LocalTime requiredLocalTime(String name) {
        return required(name, LocalTime::parse);
    }

    /**
     * Returns an optional local time parameter.
     *
     * @param name parameter name
     * @return parsed local time value when present
     */
    public Optional<LocalTime> optionalLocalTime(String name) {
        return optional(name, LocalTime::parse);
    }

    /**
     * Returns a required local date-time parameter.
     *
     * @param name parameter name
     * @return parsed local date-time value
     */
    public LocalDateTime requiredLocalDateTime(String name) {
        return required(name, LocalDateTime::parse);
    }

    /**
     * Returns an optional local date-time parameter.
     *
     * @param name parameter name
     * @return parsed local date-time value when present
     */
    public Optional<LocalDateTime> optionalLocalDateTime(String name) {
        return optional(name, LocalDateTime::parse);
    }

    /**
     * Returns a required UUID parameter.
     *
     * @param name parameter name
     * @return parsed UUID value
     */
    public UUID requiredUUID(String name) {
        return required(name, UUID::fromString);
    }

    /**
     * Returns an optional UUID parameter.
     *
     * @param name parameter name
     * @return parsed UUID value when present
     */
    public Optional<UUID> optionalUUID(String name) {
        return optional(name, UUID::fromString);
    }

    /**
     * Returns a required parameter converted by the supplied parser.
     *
     * @param name   parameter name
     * @param parser conversion function
     * @param <T>    converted value type
     * @return converted parameter value
     * @throws NullPointerException if name or parser is null
     * @throws BadRequestException  if the parameter is missing or invalid
     */
    public <T> T required(String name, Function<String, T> parser) {
        Objects.requireNonNull(parser, "Parser must not be null");
        var value = requiredString(name);
        return parse(name, value, parser);
    }

    /**
     * Returns an optional parameter converted by the supplied parser.
     *
     * @param name   parameter name
     * @param parser conversion function
     * @param <T>    converted value type
     * @return converted parameter value when present
     * @throws NullPointerException if name or parser is null
     * @throws BadRequestException  if the parameter is invalid
     */
    public <T> Optional<T> optional(String name, Function<String, T> parser) {
        Objects.requireNonNull(parser, "Parser must not be null");
        return optionalString(name).map(value -> parse(name, value, parser));
    }

    private <T> T parse(String name, String value, Function<String, T> parser) {
        try {
            return ParameterParser.parse(name, value, parser);
        } catch (BadRequestException exception) {
            throw new BadRequestException("Invalid " + parameterSource + " parameter: " + name, exception);
        }
    }

    private BadRequestException missing(String name) {
        return new BadRequestException("Missing required " + parameterSource + " parameter: " + name);
    }

    private BadRequestException invalid(String name) {
        return new BadRequestException("Invalid " + parameterSource + " parameter: " + name);
    }

}
