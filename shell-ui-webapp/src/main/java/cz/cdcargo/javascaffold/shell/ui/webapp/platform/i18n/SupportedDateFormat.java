package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Defines supported date display formats.
 *
 * Every constant exposes a stable external identifier and a formatter pattern.
 * Formatter instances are created on demand and use the root locale.
 */
public enum SupportedDateFormat {

    /** Date with day, month, and year separated by dots. */
    DD_MM_YYYY_DOT("dd_mm_yyyy_dot", "dd.MM.yyyy"),
    /** Date with day, month, and year separated by slashes. */
    DD_MM_YYYY_SLASH("dd_mm_yyyy_slash", "dd/MM/yyyy"),
    /** Date with day, month, and year separated by dashes. */
    DD_MM_YYYY_DASH("dd_mm_yyyy_dash", "dd-MM-yyyy"),
    /** Date with month, day, and year separated by dots. */
    MM_DD_YYYY_DOT("mm_dd_yyyy_dot", "MM.dd.yyyy"),
    /** Date with month, day, and year separated by slashes. */
    MM_DD_YYYY_SLASH("mm_dd_yyyy_slash", "MM/dd/yyyy"),
    /** Date with month, day, and year separated by dashes. */
    MM_DD_YYYY_DASH("mm_dd_yyyy_dash", "MM-dd-yyyy"),
    /** Date with year, month, and day separated by dots. */
    YYYY_MM_DD_DOT("yyyy_mm_dd_dot", "yyyy.MM.dd"),
    /** Date with year, month, and day separated by slashes. */
    YYYY_MM_DD_SLASH("yyyy_mm_dd_slash", "yyyy/MM/dd"),
    /** Date with year, month, and day separated by dashes. */
    YYYY_MM_DD_DASH("yyyy_mm_dd_dash", "yyyy-MM-dd"),
    /** Technical date format using the ISO-like year-month-day order. */
    TECHNICAL("technical", "yyyy-MM-dd");

    private final String id;
    private final String pattern;

    SupportedDateFormat(String id, String pattern) {
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
     * Finds a date format by its stable identifier.
     *
     * @param id format identifier
     * @return matching date format
     * @throws NullPointerException     if id is null
     * @throws IllegalArgumentException if no format has the supplied identifier
     */
    public static SupportedDateFormat byId(String id) {
        Objects.requireNonNull(id, "ID must not be null");
        for (var value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No date format found for ID: " + id);
    }

    /**
     * Creates a formatter using the root locale.
     *
     * @return formatter for this date format
     */
    public DateTimeFormatter toDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
    }
}
