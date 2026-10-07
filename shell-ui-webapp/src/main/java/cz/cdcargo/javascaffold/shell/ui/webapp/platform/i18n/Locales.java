package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Defines the immutable set of application locales and resolves requested
 * locales against it.
 *
 * Unsupported locales fall back to the default locale. Locales intended for
 * presentation can be returned in display-name order for a chosen language.
 */
public final class Locales {

    /**
     * The default application locale.
     */
    public static final Locale DEFAULT = Locale.forLanguageTag("cs-CZ");
    private static final Set<Locale> SUPPORTED = Set.of(Locale.forLanguageTag("cs-CZ"), Locale.forLanguageTag("en-US"));

    private Locales() {
    }

    /**
     * Returns the supported locales in their canonical, immutable set.
     *
     * @return supported locales
     */
    public static Set<Locale> supported() {
        return SUPPORTED;
    }

    /**
     * Checks whether a locale is supported by the application.
     *
     * @param locale locale to check
     * @return true if the locale is supported, otherwise false
     * @throws NullPointerException if locale is null
     */
    public static boolean isSupported(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null");
        return SUPPORTED.contains(locale);
    }

    /**
     * Resolves an unsupported locale to the default locale.
     *
     * @param locale locale to resolve
     * @return the supplied locale when supported, otherwise the default locale
     * @throws NullPointerException if locale is null
     */
    public static Locale resolve(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null");
        return SUPPORTED.contains(locale) ? locale : DEFAULT;
    }

    /**
     * Returns supported locales sorted by their display names.
     *
     * @param displayLocale locale used to display and sort locale names
     * @return supported locales sorted for the supplied display locale
     * @throws NullPointerException if displayLocale is null
     */
    public static List<Locale> supported(Locale displayLocale) {
        Objects.requireNonNull(displayLocale, "Display locale must not be null");
        var collator = Collator.getInstance(displayLocale);
        collator.setStrength(Collator.PRIMARY);
        return SUPPORTED.stream()
                .sorted(Comparator.comparing(locale -> locale.getDisplayName(displayLocale), collator))
                .toList();
    }
}
