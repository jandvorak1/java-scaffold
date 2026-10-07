package cz.cdcargo.javascaffold.core.platform.logger;

import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Converts textual logging levels to Java logging levels.
 *
 * Application aliases ERROR, WARN, DEBUG, and TRACE are supported in addition
 * to standard Java level names and numeric values. Matching ignores letter case
 * and surrounding whitespace. Missing or unsupported values return the supplied
 * default level.
 */
public final class LogLevels {

    private LogLevels() {
    }

    /**
     * Converts a textual logging level to its Java logging representation.
     *
     * @param value        text to convert
     * @param defaultLevel level returned for a missing or unsupported value
     * @return resolved logging level, or defaultLevel when no match exists
     * @throws NullPointerException when defaultLevel is null
     */
    public static Level parse(String value, Level defaultLevel) {
        Objects.requireNonNull(defaultLevel, "Default level must not be null");
        if (value == null || value.isBlank()) {
            return defaultLevel;
        }
        return switch (value.strip().toUpperCase(Locale.ROOT)) {
            case "OFF" -> Level.OFF;
            case "ERROR", "SEVERE" -> Level.SEVERE;
            case "WARN", "WARNING" -> Level.WARNING;
            case "INFO" -> Level.INFO;
            case "CONFIG" -> Level.CONFIG;
            case "DEBUG", "FINE" -> Level.FINE;
            case "FINER" -> Level.FINER;
            case "TRACE", "FINEST" -> Level.FINEST;
            case "ALL" -> Level.ALL;
            default -> parseJavaLevel(value.strip(), defaultLevel);
        };
    }

    private static Level parseJavaLevel(String value, Level defaultLevel) {
        try {
            return Level.parse(value);
        } catch (IllegalArgumentException ignored) {
            return defaultLevel;
        }
    }
}
