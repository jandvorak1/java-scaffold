package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.preferences;

import java.util.Objects;
import java.util.function.Function;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDecimalFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.TimeZones;
import io.helidon.http.Status;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Returns the effective interface preferences for the application.
 *
 * Stored locale, format, and time-zone values are resolved against the
 * supported options. Missing or unsupported values are represented by their
 * application defaults in the JSON response.
 */
public final class LoadPreferencesEndpoint {

    private static final String PREF_LOCALE = "settings.locale";
    private static final String PREF_DECIMAL_FORMAT = "settings.format.decimal";
    private static final String PREF_DATE_FORMAT = "settings.format.date";
    private static final String PREF_TIME_FORMAT = "settings.format.time";
    private static final String PREF_DATETIME_FORMAT = "settings.format.datetime";
    private static final String PREF_TIMEZONE = "settings.timezone";
    private static final String PREF_THEME = "settings.theme";
    private static final String PREF_TOUCH = "settings.touch";
    private static final String DEFAULT_THEME = "sap_fiori_3";

    private static final String FIELD_LOCALE = "locale";
    private static final String FIELD_DECIMAL_FORMAT = "decimalFormat";
    private static final String FIELD_DATE_FORMAT = "dateFormat";
    private static final String FIELD_TIME_FORMAT = "timeFormat";
    private static final String FIELD_DATETIME_FORMAT = "dateTimeFormat";
    private static final String FIELD_TIMEZONE = "timeZone";
    private static final String FIELD_THEME = "theme";
    private static final String FIELD_TOUCH = "touch";

    private final PreferencesStore preferences;
    private final ResponseWriter writer;

    /**
     * Creates an endpoint from a preference store and response writer.
     *
     * @param preferences reads the saved interface settings
     * @param writer      writes the JSON response
     * @throws NullPointerException if preferences or writer is null
     */
    public LoadPreferencesEndpoint(PreferencesStore preferences, ResponseWriter writer) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    /**
     * Sends the resolved preferences as a successful JSON response.
     *
     * @param request  received HTTP request
     * @param response response that receives the preferences
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");

        var locale = Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT));
        var decimalFormat = resolveFormat(
                preferences.get(PREF_DECIMAL_FORMAT, SupportedDecimalFormat.SPACE_COMMA.id()),
                SupportedDecimalFormat.SPACE_COMMA.id(), id -> SupportedDecimalFormat.byId(id).id());
        var dateFormat = resolveFormat(
                preferences.get(PREF_DATE_FORMAT, SupportedDateFormat.DD_MM_YYYY_DOT.id()),
                SupportedDateFormat.DD_MM_YYYY_DOT.id(), id -> SupportedDateFormat.byId(id).id());
        var timeFormat = resolveFormat(
                preferences.get(PREF_TIME_FORMAT, SupportedTimeFormat.HH_MM_SS_24.id()),
                SupportedTimeFormat.HH_MM_SS_24.id(), id -> SupportedTimeFormat.byId(id).id());
        var dateTimeFormat = resolveFormat(
                preferences.get(PREF_DATETIME_FORMAT, SupportedDateTimeFormat.DD_MM_YYYY_DOT_HH_MM_SS.id()),
                SupportedDateTimeFormat.DD_MM_YYYY_DOT_HH_MM_SS.id(),
                id -> SupportedDateTimeFormat.byId(id).id());
        var timeZone = TimeZones.resolve(preferences.getZoneId(PREF_TIMEZONE, TimeZones.DEFAULT));
        var theme = preferences.get(PREF_THEME, DEFAULT_THEME);
        var touch = preferences.getBoolean(PREF_TOUCH, false);
        var json = JsonValue.objectBuilder()
                .set(FIELD_LOCALE, locale.toLanguageTag())
                .set(FIELD_DECIMAL_FORMAT, decimalFormat)
                .set(FIELD_TIME_FORMAT, timeFormat)
                .set(FIELD_DATE_FORMAT, dateFormat)
                .set(FIELD_DATETIME_FORMAT, dateTimeFormat)
                .set(FIELD_TIMEZONE, timeZone.getId())
                .set(FIELD_THEME, theme)
                .set(FIELD_TOUCH, touch)
                .build();
        writer.sendJson(response, Status.OK_200, json);
    }

    private static String resolveFormat(String value, String defaultValue, Function<String, String> resolver) {
        try {
            return resolver.apply(value);
        } catch (IllegalArgumentException exception) {
            return defaultValue;
        }
    }
}
