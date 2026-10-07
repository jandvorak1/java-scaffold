package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.preferences;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDecimalFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.TimeZones;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

import io.helidon.http.HttpException;
import io.helidon.http.Status;
import io.helidon.json.JsonObject;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Validates and persists interface preferences received from the client.
 *
 * State-changing request checks protect the operation. Missing fields use
 * application defaults, and all values are validated before any preference is
 * written so rejected input cannot produce a partial update.
 */
public final class SavePreferencesEndpoint {

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

    private final WebSecurity security;
    private final PreferencesStore preferences;
    private final ResponseWriter writer;

    /**
     * Creates an endpoint from request security, preference storage, and a
     * response writer.
     *
     * @param security    validates state-changing requests
     * @param preferences persists the validated settings
     * @param writer      sends the no-content acknowledgement
     * @throws NullPointerException if any dependency is null
     */
    public SavePreferencesEndpoint(WebSecurity security, PreferencesStore preferences, ResponseWriter writer) {
        this.security = Objects.requireNonNull(security, "Web security must not be null");
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    /**
     * Validates the request and saves preferences from its JSON body.
     *
     * @param request  provides the security data and submitted preferences
     * @param response receives the no-content acknowledgement
     * @throws NullPointerException if request or response is null
     * @throws HttpException        if a submitted format or time-zone identifier is
     *                              invalid
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        security.requireStateChangingRequest(request);
        var json = request.content().as(JsonObject.class);
        var values = resolvePreferences(json);

        preferences.putLocale(PREF_LOCALE, values.locale());
        preferences.put(PREF_DECIMAL_FORMAT, values.decimalFormat().id());
        preferences.put(PREF_DATE_FORMAT, values.dateFormat().id());
        preferences.put(PREF_TIME_FORMAT, values.timeFormat().id());
        preferences.put(PREF_DATETIME_FORMAT, values.dateTimeFormat().id());
        preferences.putZoneId(PREF_TIMEZONE, values.timeZone());
        preferences.put(PREF_THEME, values.theme());
        preferences.putBoolean(PREF_TOUCH, values.touch());

        writer.sendEmpty(response, Status.NO_CONTENT_204);
    }

    private static PreferenceValues resolvePreferences(JsonObject json) {
        try {
            var locale = Locales.resolve(Locale.forLanguageTag(
                    json.stringValue(FIELD_LOCALE).orElse(Locales.DEFAULT.toLanguageTag())));
            var decimalFormat = SupportedDecimalFormat.byId(
                    json.stringValue(FIELD_DECIMAL_FORMAT).orElse(SupportedDecimalFormat.SPACE_COMMA.id()));
            var dateFormat = SupportedDateFormat.byId(
                    json.stringValue(FIELD_DATE_FORMAT).orElse(SupportedDateFormat.DD_MM_YYYY_DOT.id()));
            var timeFormat = SupportedTimeFormat.byId(
                    json.stringValue(FIELD_TIME_FORMAT).orElse(SupportedTimeFormat.HH_MM_SS_24.id()));
            var dateTimeFormat = SupportedDateTimeFormat.byId(json.stringValue(FIELD_DATETIME_FORMAT)
                    .orElse(SupportedDateTimeFormat.DD_MM_YYYY_DOT_HH_MM_SS.id()));
            var timeZone = TimeZones.resolve(ZoneId.of(
                    json.stringValue(FIELD_TIMEZONE).orElse(TimeZones.DEFAULT.getId())));
            var theme = json.stringValue(FIELD_THEME).orElse(DEFAULT_THEME);
            var touch = json.booleanValue(FIELD_TOUCH).orElse(false);
            return new PreferenceValues(locale, decimalFormat, dateFormat, timeFormat, dateTimeFormat, timeZone, theme,
                    touch);
        } catch (IllegalArgumentException | DateTimeException exception) {
            throw new HttpException("Invalid preference value", Status.BAD_REQUEST_400, exception);
        }
    }

    private record PreferenceValues(Locale locale, SupportedDecimalFormat decimalFormat, SupportedDateFormat dateFormat,
            SupportedTimeFormat timeFormat, SupportedDateTimeFormat dateTimeFormat, ZoneId timeZone, String theme,
            boolean touch) {
    }
}
