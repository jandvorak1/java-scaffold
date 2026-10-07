package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Defines supported combined date and time display formats.
 *
 * Every constant exposes a stable external identifier and a formatter pattern.
 * Formatter instances are created on demand and use the root locale.
 */
public enum SupportedDateTimeFormat {

    /** Date and time with day, month, and year separated by dots. */
    DD_MM_YYYY_DOT_HH_MM_SS("dd_mm_yyyy_dot_hh_mm_ss", "dd.MM.yyyy HH:mm:ss"),
    /** Date and time with day, month, and year separated by slashes. */
    DD_MM_YYYY_SLASH_HH_MM_SS("dd_mm_yyyy_slash_hh_mm_ss", "dd/MM/yyyy HH:mm:ss"),
    /** Date and time with day, month, and year separated by dashes. */
    DD_MM_YYYY_DASH_HH_MM_SS("dd_mm_yyyy_dash_hh_mm_ss", "dd-MM-yyyy HH:mm:ss"),
    /** Date and time with month, day, and year separated by dots. */
    MM_DD_YYYY_DOT_HH_MM_SS("mm_dd_yyyy_dot_hh_mm_ss", "MM.dd.yyyy HH:mm:ss"),
    /** Date and time with month, day, and year separated by slashes. */
    MM_DD_YYYY_SLASH_HH_MM_SS("mm_dd_yyyy_slash_hh_mm_ss", "MM/dd/yyyy HH:mm:ss"),
    /** Date and time with month, day, and year using a twelve-hour clock. */
    MM_DD_YYYY_SLASH_HH_MM_SS_A("mm_dd_yyyy_slash_hh_mm_ss_a", "MM/dd/yyyy h:mm:ss a"),
    /** Date and time with month, day, and year separated by dashes. */
    MM_DD_YYYY_DASH_HH_MM_SS("mm_dd_yyyy_dash_hh_mm_ss", "MM-dd-yyyy HH:mm:ss"),
    /** Date and time with year, month, and day separated by dots. */
    YYYY_MM_DD_DOT_HH_MM_SS("yyyy_mm_dd_dot_hh_mm_ss", "yyyy.MM.dd HH:mm:ss"),
    /** Date and time with year, month, and day separated by slashes. */
    YYYY_MM_DD_SLASH_HH_MM_SS("yyyy_mm_dd_slash_hh_mm_ss", "yyyy/MM/dd HH:mm:ss"),
    /** Date and time with year, month, and day separated by dashes. */
    YYYY_MM_DD_DASH_HH_MM_SS("yyyy_mm_dd_dash_hh_mm_ss", "yyyy-MM-dd HH:mm:ss"),
    /** Technical date-time format using the ISO-like year-month-day order. */
    TECHNICAL("technical", "yyyy-MM-dd'T'HH:mm:ss");

    private final String id;
    private final String pattern;

    SupportedDateTimeFormat(String id, String pattern) {
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
     * Finds a date-time format by its stable identifier.
     *
     * @param id format identifier
     * @return matching date-time format
     * @throws NullPointerException     if id is null
     * @throws IllegalArgumentException if no format has the supplied identifier
     */
    public static SupportedDateTimeFormat byId(String id) {
        Objects.requireNonNull(id, "ID must not be null");
        for (var value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No date-time format found for ID: " + id);
    }

    /**
     * Creates a formatter using the root locale.
     *
     * @return formatter for this date-time format
     */
    public DateTimeFormatter toDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
    }
}
