package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

/**
 * Defines supported decimal and whole-number display formats.
 *
 * Every constant exposes a stable external identifier together with its
 * pattern, separators, and fraction precision. A new mutable DecimalFormat is
 * returned for every request.
 */
public enum SupportedDecimalFormat {

    /** Decimal values with a comma and grouping spaces. */
    SPACE_COMMA("space_comma", "#,##0.00", ',', ' ', 2, 2),
    /** Decimal values with a dot and grouping commas. */
    COMMA_DOT("comma_dot", "#,##0.00", '.', ',', 2, 2),
    /** Decimal values with a comma and grouping dots. */
    DOT_COMMA("dot_comma", "#,##0.00", ',', '.', 2, 2),
    /** Decimal values with a dot and grouping apostrophes. */
    APOSTROPHE_DOT("apostrophe_dot", "#,##0.00", '.', '\'', 2, 2),
    /** Technical decimal format without grouping separators. */
    TECHNICAL("technical", "0.00", '.', Character.MIN_VALUE, 2, 2);

    private final String id;
    private final String pattern;
    private final char decimalSeparator;
    private final char groupingSeparator;
    private final int minimumFractionDigits;
    private final int maximumFractionDigits;

    SupportedDecimalFormat(String id, String pattern, char decimalSeparator, char groupingSeparator,
            int minimumFractionDigits, int maximumFractionDigits) {
        this.id = id;
        this.pattern = pattern;
        this.decimalSeparator = decimalSeparator;
        this.groupingSeparator = groupingSeparator;
        this.minimumFractionDigits = minimumFractionDigits;
        this.maximumFractionDigits = maximumFractionDigits;
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
     * Returns the DecimalFormat pattern for this format.
     *
     * @return formatter pattern
     */
    public String pattern() {
        return pattern;
    }

    /**
     * Returns the decimal separator used by this format.
     *
     * @return decimal separator
     */
    public char decimalSeparator() {
        return decimalSeparator;
    }

    /**
     * Returns the grouping separator used by this format.
     *
     * @return grouping separator
     */
    public char groupingSeparator() {
        return groupingSeparator;
    }

    /**
     * Returns the minimum number of fraction digits used by this format.
     *
     * @return minimum fraction digits
     */
    public int minimumFractionDigits() {
        return minimumFractionDigits;
    }

    /**
     * Returns the maximum number of fraction digits used by this format.
     *
     * @return maximum fraction digits
     */
    public int maximumFractionDigits() {
        return maximumFractionDigits;
    }

    /**
     * Finds a decimal format by its stable identifier.
     *
     * @param id format identifier
     * @return matching decimal format
     * @throws NullPointerException     if id is null
     * @throws IllegalArgumentException if no format has the supplied identifier
     */
    public static SupportedDecimalFormat byId(String id) {
        Objects.requireNonNull(id, "ID must not be null");
        for (var value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No decimal format found for ID: " + id);
    }

    /**
     * Creates a formatter for decimal values.
     *
     * @return configured decimal formatter
     */
    public DecimalFormat toDecimalFormat() {
        var symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);

        var decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);
        decimalFormat.setParseIntegerOnly(false);
        decimalFormat.setMinimumFractionDigits(minimumFractionDigits);
        decimalFormat.setMaximumFractionDigits(maximumFractionDigits);
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat;
    }

    /**
     * Creates a formatter for whole-number values.
     *
     * @return configured whole-number formatter
     */
    public DecimalFormat toWholeNumberFormat() {
        var symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);

        var decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);
        decimalFormat.setParseIntegerOnly(true);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setGroupingUsed(groupingSeparator != Character.MIN_VALUE);
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat;
    }
}
