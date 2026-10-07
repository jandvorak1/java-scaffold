package cz.cdcargo.javascaffold.shell.ui.webapp.platform.http;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.helidon.http.BadRequestException;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonObject;

/**
 * Provides typed and validated access to properties stored in a JSON object.
 *
 * Missing required properties, incompatible JSON value types, and failed text
 * conversions are reported as bad HTTP requests identifying the property.
 */
public final class JsonParameters {

    /**
     * Creates a JSON parameter accessor.
     *
     * @param json JSON object containing the parameters
     * @return accessor backed by the supplied JSON object
     * @throws NullPointerException if json is null
     */
    public static JsonParameters of(JsonObject json) {
        return new JsonParameters(json);
    }

    private static BadRequestException missing(String name) {
        return new BadRequestException("Missing required JSON property: " + name);
    }

    private static BadRequestException invalid(String name) {
        return new BadRequestException("Invalid JSON property: " + name);
    }

    private static BadRequestException invalid(String name, RuntimeException cause) {
        return new BadRequestException("Invalid JSON property: " + name, cause);
    }

    private final JsonObject json;

    private JsonParameters(JsonObject json) {
        this.json = Objects.requireNonNull(json, "JSON object must not be null");
    }

    /**
     * Returns a required string property.
     *
     * @param name property name
     * @return property value
     * @throws BadRequestException  if the property is missing or invalid
     * @throws NullPointerException if name is null
     */
    public String requiredString(String name) {
        return requiredValue(name, json::stringValue);
    }

    /**
     * Returns an optional string property.
     *
     * @param name property name
     * @return property value when present
     * @throws BadRequestException  if the property is invalid
     * @throws NullPointerException if name is null
     */
    public Optional<String> optionalString(String name) {
        Objects.requireNonNull(name, "Property name must not be null");
        return optionalValue(name, json::stringValue);
    }

    /**
     * Returns a required non-blank string property.
     *
     * @param name property name
     * @return non-blank property value
     * @throws BadRequestException  if the property is missing or blank
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
     * Returns a required integer property.
     *
     * @param name property name
     * @return parsed integer value
     */
    public int requiredInt(String name) {
        return requiredValue(name, key -> json.numberValue(key).map(BigDecimal::intValueExact));
    }

    /**
     * Returns an optional integer property.
     *
     * @param name property name
     * @return parsed integer value when present
     */
    public Optional<Integer> optionalInt(String name) {
        return optionalValue(name, key -> json.numberValue(key).map(BigDecimal::intValueExact));
    }

    /**
     * Returns a required long property.
     *
     * @param name property name
     * @return parsed long value
     */
    public long requiredLong(String name) {
        return requiredValue(name, key -> json.numberValue(key).map(BigDecimal::longValueExact));
    }

    /**
     * Returns an optional long property.
     *
     * @param name property name
     * @return parsed long value when present
     */
    public Optional<Long> optionalLong(String name) {
        return optionalValue(name, key -> json.numberValue(key).map(BigDecimal::longValueExact));
    }

    /**
     * Returns a required double property.
     *
     * @param name property name
     * @return parsed double value
     */
    public double requiredDouble(String name) {
        return requiredValue(name, json::doubleValue);
    }

    /**
     * Returns an optional double property.
     *
     * @param name property name
     * @return parsed double value when present
     */
    public Optional<Double> optionalDouble(String name) {
        return optionalValue(name, json::doubleValue);
    }

    /**
     * Returns a required decimal property.
     *
     * @param name property name
     * @return parsed decimal value
     */
    public BigDecimal requiredBigDecimal(String name) {
        return requiredValue(name, json::numberValue);
    }

    /**
     * Returns an optional decimal property.
     *
     * @param name property name
     * @return parsed decimal value when present
     */
    public Optional<BigDecimal> optionalBigDecimal(String name) {
        return optionalValue(name, json::numberValue);
    }

    /**
     * Returns a required boolean property.
     *
     * @param name property name
     * @return parsed boolean value
     */
    public boolean requiredBoolean(String name) {
        return requiredValue(name, json::booleanValue);
    }

    /**
     * Returns an optional boolean property.
     *
     * @param name property name
     * @return parsed boolean value when present
     */
    public Optional<Boolean> optionalBoolean(String name) {
        return optionalValue(name, json::booleanValue);
    }

    /**
     * Returns a required local date property.
     *
     * @param name property name
     * @return parsed local date value
     */
    public LocalDate requiredLocalDate(String name) {
        return required(name, LocalDate::parse);
    }

    /**
     * Returns an optional local date property.
     *
     * @param name property name
     * @return parsed local date value when present
     */
    public Optional<LocalDate> optionalLocalDate(String name) {
        return optional(name, LocalDate::parse);
    }

    /**
     * Returns a required local time property.
     *
     * @param name property name
     * @return parsed local time value
     */
    public LocalTime requiredLocalTime(String name) {
        return required(name, LocalTime::parse);
    }

    /**
     * Returns an optional local time property.
     *
     * @param name property name
     * @return parsed local time value when present
     */
    public Optional<LocalTime> optionalLocalTime(String name) {
        return optional(name, LocalTime::parse);
    }

    /**
     * Returns a required local date-time property.
     *
     * @param name property name
     * @return parsed local date-time value
     */
    public LocalDateTime requiredLocalDateTime(String name) {
        return required(name, LocalDateTime::parse);
    }

    /**
     * Returns an optional local date-time property.
     *
     * @param name property name
     * @return parsed local date-time value when present
     */
    public Optional<LocalDateTime> optionalLocalDateTime(String name) {
        return optional(name, LocalDateTime::parse);
    }

    /**
     * Returns a required UUID property.
     *
     * @param name property name
     * @return parsed UUID value
     */
    public UUID requiredUUID(String name) {
        return required(name, UUID::fromString);
    }

    /**
     * Returns an optional UUID property.
     *
     * @param name property name
     * @return parsed UUID value when present
     */
    public Optional<UUID> optionalUUID(String name) {
        return optional(name, UUID::fromString);
    }

    /**
     * Returns a required nested JSON object property.
     *
     * @param name property name
     * @return nested JSON object
     */
    public JsonObject requiredObject(String name) {
        return requiredValue(name, json::objectValue);
    }

    /**
     * Returns an optional nested JSON object property.
     *
     * @param name property name
     * @return nested JSON object when present
     */
    public Optional<JsonObject> optionalObject(String name) {
        return optionalValue(name, json::objectValue);
    }

    /**
     * Returns a required JSON array property.
     *
     * @param name property name
     * @return JSON array
     */
    public JsonArray requiredArray(String name) {
        return requiredValue(name, json::arrayValue);
    }

    /**
     * Returns an optional JSON array property.
     *
     * @param name property name
     * @return JSON array when present
     */
    public Optional<JsonArray> optionalArray(String name) {
        return optionalValue(name, json::arrayValue);
    }

    /**
     * Returns a required string property converted by the supplied parser.
     *
     * @param name   property name
     * @param parser conversion function
     * @param <T>    converted value type
     * @return converted property value
     * @throws BadRequestException  if the property is missing or invalid
     * @throws NullPointerException if name or parser is null
     */
    public <T> T required(String name, Function<String, T> parser) {
        Objects.requireNonNull(parser, "Parser must not be null");
        return parse(name, requiredString(name), parser);
    }

    /**
     * Returns an optional string property converted by the supplied parser.
     *
     * @param name   property name
     * @param parser conversion function
     * @param <T>    converted value type
     * @return converted property value when present
     * @throws BadRequestException  if the property is invalid
     * @throws NullPointerException if name or parser is null
     */
    public <T> Optional<T> optional(String name, Function<String, T> parser) {
        Objects.requireNonNull(parser, "Parser must not be null");
        return optionalString(name).map(value -> parse(name, value, parser));
    }

    private static <T> T parse(String name, String value, Function<String, T> parser) {
        try {
            return ParameterParser.parse(name, value, parser);
        } catch (BadRequestException exception) {
            throw invalid(name, exception);
        }
    }

    private <T> T requiredValue(String name, Function<String, Optional<T>> accessor) {
        Objects.requireNonNull(name, "Property name must not be null");
        if (!json.containsKey(name)) {
            throw missing(name);
        }
        try {
            return accessor.apply(name).orElseThrow(() -> invalid(name));
        } catch (BadRequestException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw invalid(name, exception);
        }
    }

    private <T> Optional<T> optionalValue(String name, Function<String, Optional<T>> accessor) {
        Objects.requireNonNull(name, "Property name must not be null");
        if (!json.containsKey(name)) {
            return Optional.empty();
        }
        try {
            return accessor.apply(name);
        } catch (RuntimeException exception) {
            throw invalid(name, exception);
        }
    }
}
