package cz.cdcargo.javascaffold.core.platform.naming;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Provides utility methods for normalizing text values.
 */
public final class Texts {

    private Texts() {
    }

    /**
     * Converts letter and number segments in a value to Pascal case.
     *
     * Characters other than letters and numbers separate segments. The first character of each segment is
     * converted to uppercase and the remaining characters to lowercase using the root locale. A null or blank
     * value is returned unchanged.
     *
     * @param value the value to convert
     * @return the converted value, or the original value when it is null or blank
     */
    public static String toPascalCase(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Arrays.stream(value.split("[^\\p{L}\\p{N}]+"))
                .filter(part -> !part.isBlank())
                .map(Texts::capitalize)
                .collect(Collectors.joining());
    }

    private static String capitalize(String value) {
        var lowerCaseValue = value.toLowerCase(Locale.ROOT);
        var firstCharacterEnd = lowerCaseValue.offsetByCodePoints(0, 1);
        return lowerCaseValue.substring(0, firstCharacterEnd).toUpperCase(Locale.ROOT)
                + lowerCaseValue.substring(firstCharacterEnd);
    }
}
