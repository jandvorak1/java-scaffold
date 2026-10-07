package cz.cdcargo.javascaffold.core.platform.preferences;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Defines storage and typed access for application preferences.
 *
 * Implementations persist string values, while the default methods convert
 * them to common value types. Methods with external override names use the
 * first valid value in this order: environment variable, system property,
 * stored preference, and supplied default. Blank or invalid external and
 * stored values are ignored.
 */
public interface PreferencesStore {

    /**
     * Returns a stored string value.
     *
     * @param key          preference key
     * @param defaultValue value returned when the preference is unavailable
     * @return stored value, or defaultValue when the key has no value
     * @throws NullPointerException if key is null
     */
    String get(String key, String defaultValue);

    /**
     * Stores a string value under the given key.
     *
     * @param key   preference key
     * @param value value to store
     * @throws NullPointerException when key or value is null
     */
    void put(String key, String value);

    /**
     * Removes the value stored under the given key.
     *
     * @param key preference key
     * @throws NullPointerException when key is null
     */
    void remove(String key);

    /**
     * Returns a string value with optional external overrides.
     *
     * @param key             preference key
     * @param defaultValue    final fallback value
     * @param envVariableName environment variable name, or null to disable it
     * @param sysPropertyName system property name, or null to disable it
     * @return first non-blank external value, stored value, or defaultValue
     * @throws NullPointerException if key is null
     */
    default String get(String key, String defaultValue, String envVariableName, String sysPropertyName) {
        Objects.requireNonNull(key, "Key must not be null");
        var value = getEnvironmentVariable(envVariableName);
        if (value != null) {
            return value;
        }
        value = getSystemProperty(sysPropertyName);
        if (value != null) {
            return value;
        }
        return get(key, defaultValue);
    }

    /**
     * Returns an integer value.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return stored integer, or defaultValue
     * @throws NullPointerException if key is null
     */
    default int getInt(String key, int defaultValue) {
        return getParsed(key, defaultValue, null, null, Integer::parseInt);
    }

    /**
     * Returns an integer value with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid integer, or defaultValue
     * @throws NullPointerException if key is null
     */
    default int getInt(String key, int defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, Integer::parseInt);
    }

    /**
     * Stores an integer value.
     * 
     * @param key   preference key
     * @param value value to store
     * @throws NullPointerException if key is null
     */
    default void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    /**
     * Returns a long value.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return stored long, or defaultValue
     * @throws NullPointerException if key is null
     */
    default long getLong(String key, long defaultValue) {
        return getParsed(key, defaultValue, null, null, Long::parseLong);
    }

    /**
     * Returns a long value with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid long, or defaultValue
     * @throws NullPointerException if key is null
     */
    default long getLong(String key, long defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, Long::parseLong);
    }

    /**
     * Stores a long value.
     * 
     * @param key   preference key
     * @param value value to store
     * @throws NullPointerException if key is null
     */
    default void putLong(String key, long value) {
        put(key, Long.toString(value));
    }

    /**
     * Returns a double value.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return stored double, or defaultValue
     * @throws NullPointerException if key is null
     */
    default double getDouble(String key, double defaultValue) {
        return getParsed(key, defaultValue, null, null, Double::parseDouble);
    }

    /**
     * Returns a double value with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid double, or defaultValue
     * @throws NullPointerException if key is null
     */
    default double getDouble(String key, double defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, Double::parseDouble);
    }

    /**
     * Stores a double value.
     * 
     * @param key   preference key
     * @param value value to store
     * @throws NullPointerException if key is null
     */
    default void putDouble(String key, double value) {
        put(key, Double.toString(value));
    }

    /**
     * Returns a float value.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return stored float, or defaultValue
     * @throws NullPointerException if key is null
     */
    default float getFloat(String key, float defaultValue) {
        return getParsed(key, defaultValue, null, null, Float::parseFloat);
    }

    /**
     * Returns a float value with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid float, or defaultValue
     * @throws NullPointerException if key is null
     */
    default float getFloat(String key, float defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, Float::parseFloat);
    }

    /**
     * Stores a float value.
     * 
     * @param key   preference key
     * @param value value to store
     * @throws NullPointerException if key is null
     */
    default void putFloat(String key, float value) {
        put(key, Float.toString(value));
    }

    /**
     * Returns a boolean preference. Only case-insensitive true and false values
     * are valid.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return stored boolean, or defaultValue
     * @throws NullPointerException if key is null
     */
    default boolean getBoolean(String key, boolean defaultValue) {
        return getParsed(key, defaultValue, null, null, PreferencesStore::parseBoolean);
    }

    /**
     * Returns a boolean value with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid boolean, or defaultValue
     * @throws NullPointerException if key is null
     */
    default boolean getBoolean(String key, boolean defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, PreferencesStore::parseBoolean);
    }

    /**
     * Stores a boolean value.
     * 
     * @param key   preference key
     * @param value value to store
     * @throws NullPointerException if key is null
     */
    default void putBoolean(String key, boolean value) {
        put(key, Boolean.toString(value));
    }

    /**
     * Returns bytes decoded from standard Base64 text.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return decoded bytes, or defaultValue
     * @throws NullPointerException if key is null
     */
    default byte[] getByteArray(String key, byte[] defaultValue) {
        return getParsed(key, defaultValue, null, null, value -> Base64.getDecoder().decode(value));
    }

    /**
     * Returns Base64-decoded bytes with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first value that can be decoded, or defaultValue
     * @throws NullPointerException if key is null
     */
    default byte[] getByteArray(String key, byte[] defaultValue, String envVariableName,
            String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName,
                value -> Base64.getDecoder().decode(value));
    }

    /**
     * Stores bytes as standard Base64 text.
     * 
     * @param key   preference key
     * @param value bytes to store
     * @throws NullPointerException if key or value is null
     */
    default void putByteArray(String key, byte[] value) {
        Objects.requireNonNull(value, "Value must not be null");
        put(key, Base64.getEncoder().encodeToString(value));
    }

    /**
     * Returns a local date parsed from ISO-8601 text.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return parsed date, or defaultValue
     * @throws NullPointerException if key is null
     */
    default LocalDate getLocalDate(String key, LocalDate defaultValue) {
        return getParsed(key, defaultValue, null, null,
                value -> LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /**
     * Returns an ISO-8601 local date with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid local date, or defaultValue
     * @throws NullPointerException if key is null
     */
    default LocalDate getLocalDate(String key, LocalDate defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName,
                value -> LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /**
     * Stores a local date in ISO-8601 format.
     * 
     * @param key   preference key
     * @param value date to store
     * @throws NullPointerException if key or value is null
     */
    default void putLocalDate(String key, LocalDate value) {
        Objects.requireNonNull(value, "Value must not be null");
        put(key, value.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /**
     * Returns a local time parsed from ISO-8601 text.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return parsed time, or defaultValue
     * @throws NullPointerException if key is null
     */
    default LocalTime getLocalTime(String key, LocalTime defaultValue) {
        return getParsed(key, defaultValue, null, null,
                value -> LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME));
    }

    /**
     * Returns an ISO-8601 local time with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid local time, or defaultValue
     * @throws NullPointerException if key is null
     */
    default LocalTime getLocalTime(String key, LocalTime defaultValue, String envVariableName,
            String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName,
                value -> LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME));
    }

    /**
     * Stores a local time in ISO-8601 format.
     * 
     * @param key   preference key
     * @param value time to store
     * @throws NullPointerException if key or value is null
     */
    default void putLocalTime(String key, LocalTime value) {
        Objects.requireNonNull(value, "Value must not be null");
        put(key, value.format(DateTimeFormatter.ISO_LOCAL_TIME));
    }

    /**
     * Returns a local date-time parsed from ISO-8601 text.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return parsed date-time, or defaultValue
     * @throws NullPointerException if key is null
     */
    default LocalDateTime getLocalDateTime(String key, LocalDateTime defaultValue) {
        return getParsed(key, defaultValue, null, null,
                value -> LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Returns an ISO-8601 local date-time with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid local date-time, or defaultValue
     * @throws NullPointerException if key is null
     */
    default LocalDateTime getLocalDateTime(String key, LocalDateTime defaultValue, String envVariableName,
            String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName,
                value -> LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Stores a local date-time in ISO-8601 format.
     * 
     * @param key   preference key
     * @param value date-time to store
     * @throws NullPointerException if key or value is null
     */
    default void putLocalDateTime(String key, LocalDateTime value) {
        Objects.requireNonNull(value, "Value must not be null");
        put(key, value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Returns a locale parsed from a BCP 47 language tag.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return parsed locale, or defaultValue
     * @throws NullPointerException if key is null
     */
    default Locale getLocale(String key, Locale defaultValue) {
        return getParsed(key, defaultValue, null, null, PreferencesStore::parseLocale);
    }

    /**
     * Returns a locale with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid locale, or defaultValue
     * @throws NullPointerException if key is null
     */
    default Locale getLocale(String key, Locale defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, PreferencesStore::parseLocale);
    }

    /**
     * Stores a locale as a BCP 47 language tag.
     * 
     * @param key   preference key
     * @param value locale to store
     * @throws NullPointerException if key or value is null
     */
    default void putLocale(String key, Locale value) {
        Objects.requireNonNull(value, "Value must not be null");
        put(key, value.toLanguageTag());
    }

    /**
     * Returns a time zone parsed from its identifier.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return parsed time zone, or defaultValue
     * @throws NullPointerException if key is null
     */
    default ZoneId getZoneId(String key, ZoneId defaultValue) {
        return getParsed(key, defaultValue, null, null, ZoneId::of);
    }

    /**
     * Returns a time zone with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid time zone, or defaultValue
     * @throws NullPointerException if key is null
     */
    default ZoneId getZoneId(String key, ZoneId defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, ZoneId::of);
    }

    /**
     * Stores a time-zone identifier.
     * 
     * @param key   preference key
     * @param value time zone to store
     * @throws NullPointerException if key or value is null
     */
    default void putZoneId(String key, ZoneId value) {
        Objects.requireNonNull(value, "Value must not be null");
        put(key, value.getId());
    }

    /**
     * Returns a path parsed using the current platform syntax.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return parsed path, or defaultValue
     * @throws NullPointerException if key is null
     */
    default Path getPath(String key, Path defaultValue) {
        return getParsed(key, defaultValue, null, null, Path::of);
    }

    /**
     * Returns a path with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid path, or defaultValue
     * @throws NullPointerException if key is null
     */
    default Path getPath(String key, Path defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, Path::of);
    }

    /**
     * Stores a path using its platform representation.
     * 
     * @param key   preference key
     * @param value path to store
     * @throws NullPointerException if key or value is null
     */
    default void putPath(String key, Path value) {
        Objects.requireNonNull(value, "Value must not be null");
        put(key, value.toString());
    }

    /**
     * Returns a Java logging level parsed from its name or integer value.
     * 
     * @param key          preference key
     * @param defaultValue fallback value
     * @return parsed logging level, or defaultValue
     * @throws NullPointerException if key is null
     */
    default Level getLevel(String key, Level defaultValue) {
        return getParsed(key, defaultValue, null, null, Level::parse);
    }

    /**
     * Returns a Java logging level with optional external overrides.
     * 
     * @param key             preference key
     * @param defaultValue    fallback value
     * @param envVariableName environment variable name, or null
     * @param sysPropertyName system property name, or null
     * @return first valid logging level, or defaultValue
     * @throws NullPointerException if key is null
     */
    default Level getLevel(String key, Level defaultValue, String envVariableName, String sysPropertyName) {
        return getParsed(key, defaultValue, envVariableName, sysPropertyName, Level::parse);
    }

    /**
     * Stores a Java logging level using its name.
     * 
     * @param key   preference key
     * @param value logging level to store
     * @throws NullPointerException if key or value is null
     */
    default void putLevel(String key, Level value) {
        Objects.requireNonNull(value, "Value must not be null");
        put(key, value.toString());
    }

    private <T> T getParsed(String key, T defaultValue, String envVariableName, String sysPropertyName,
            Function<String, T> parser) {
        Objects.requireNonNull(key, "Key must not be null");
        var value = tryParse(getEnvironmentVariable(envVariableName), parser);
        if (value != null) {
            return value;
        }
        value = tryParse(getSystemProperty(sysPropertyName), parser);
        if (value != null) {
            return value;
        }
        value = tryParse(get(key, null), parser);
        return value != null ? value : defaultValue;
    }

    private static String getEnvironmentVariable(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        var value = System.getenv(name);
        return value == null || value.isBlank() ? null : value;
    }

    private static String getSystemProperty(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        var value = System.getProperty(name);
        return value == null || value.isBlank() ? null : value;
    }

    private static <T> T tryParse(String value, Function<String, T> parser) {
        if (value == null) {
            return null;
        }
        try {
            return parser.apply(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return null;
    }

    private static Locale parseLocale(String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("Locale must not be blank");
        }
        return new Locale.Builder().setLanguageTag(value).build();
    }
}
