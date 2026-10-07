package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.locales;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import io.helidon.http.Status;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Provides the locales supported by the application.
 *
 * Locale names are translated using the current display-locale preference and
 * sorted accordingly. Each entry also includes its name in the represented
 * locale so users can recognize it independently of the current language.
 */
public final class LoadLocaleEndpoint {

    private static final String PREF_LOCALE = "settings.locale";

    private static final String FIELD_TAG = "tag";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SUBNAME = "subname";
    private static final String FIELD_LOCALES = "locales";

    private final PreferencesStore preferences;
    private final ResponseWriter writer;

    /**
     * Creates a locale endpoint with its preference store and response writer.
     *
     * @param preferences store used to read the display locale preference
     * @param writer      response writer used to send the locales
     * @throws NullPointerException if preferences or writer is null
     */
    public LoadLocaleEndpoint(PreferencesStore preferences, ResponseWriter writer) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    /**
     * Sends the supported locales as a localized JSON response.
     *
     * @param request  incoming HTTP request
     * @param response HTTP response to write
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        var displayLocale = Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT));
        var locales = Locales.supported(displayLocale).stream()
                .<JsonValue>map(locale -> JsonValue.objectBuilder()
                        .set(FIELD_TAG, locale.toLanguageTag())
                        .set(FIELD_NAME, locale.getDisplayName(displayLocale))
                        .set(FIELD_SUBNAME, locale.getDisplayName(locale))
                        .build())
                .toList();
        var json = JsonValue.objectBuilder()
                .set(FIELD_LOCALES, JsonArray.create(locales))
                .build();
        writer.sendJson(response, Status.OK_200, json);
    }
}
