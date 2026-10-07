package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Defines supported time display formats.
 *
 * Every constant exposes a stable external identifier and a formatter pattern.
 * Formatter instances are created on demand and use the root locale.
 */
public enum SupportedTimeFormat {

    /** Time using a twenty-four-hour clock. */
    HH_MM_SS_24("hh_mm_ss_24", "HH:mm:ss"),
    /** Time using a twelve-hour clock and an AM or PM marker. */
    HH_MM_SS_12("hh_mm_ss_12", "h:mm:ss a"),
    /** Technical time format using a twenty-four-hour clock. */
    TECHNICAL("technical", "HH:mm:ss");

    private final String id;
    private final String pattern;

    SupportedTimeFormat(String id, String pattern) {
        this.id = id;
        this.pattern = pattern;
    }

    /**
     * Returns the stable identifier used in preferences and API payloads.
     *
     * @return format identifier
     */
    public String id() {
        return id;
    }

    /**
     * Returns the DateTimeFormatter pattern for this format.
     *
     * @return formatter pattern
     */
    public String pattern() {
        return pattern;
    }

    /**
     * Finds a time format by its stable identifier.
     *
     * @param id format identifier
     * @return matching time format
     * @throws NullPointerException     if id is null
     * @throws IllegalArgumentException if no format has the supplied identifier
     */
    public static SupportedTimeFormat byId(String id) {
        Objects.requireNonNull(id, "ID must not be null");
        for (var value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No time format found for ID: " + id);
    }

    /**
     * Creates a formatter using the root locale.
     *
     * @return formatter for this time format
     */
    public DateTimeFormatter toDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
    }
}
