package cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Resolves and formats localized application messages.
 *
 * Unsupported locales are replaced with the application default before the
 * resource bundle is loaded. A missing message key is returned unchanged so
 * callers always receive a usable diagnostic value.
 */
public final class Messages {

    private static final String BUNDLE_NAME = "i18n.messagebundle";

    private final Locale locale;
    private final ResourceBundle bundle;

    /**
     * Creates a message provider for the supplied locale.
     *
     * @param locale requested message locale
     * @throws NullPointerException if locale is null
     */
    public Messages(Locale locale) {
        this.locale = Locales.resolve(Objects.requireNonNull(locale, "Locale must not be null"));
        this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, this.locale);
    }

    /**
     * Returns the locale used by this message provider.
     *
     * @return resolved message locale
     */
    public Locale locale() {
        return locale;
    }

    /**
     * Returns a localized message without formatting arguments.
     *
     * @param key message key
     * @return localized message, or the key when no message exists
     * @throws NullPointerException if key is null
     */
    public String get(String key) {
        Objects.requireNonNull(key, "Key must not be null");
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns a localized message formatted with the supplied arguments.
     *
     * @param key  message key
     * @param args message format arguments
     * @return formatted localized message, or the key when no message exists
     * @throws NullPointerException     if key is null
     *                                  or args is null
     * @throws IllegalArgumentException if the message pattern is invalid
     */
    public String get(String key, Object... args) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(args, "Arguments must not be null");
        try {
            var pattern = bundle.getString(key);
            return new MessageFormat(pattern, locale).format(args);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
