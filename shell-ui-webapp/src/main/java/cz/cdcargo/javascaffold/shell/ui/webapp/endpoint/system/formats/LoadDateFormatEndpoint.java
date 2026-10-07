package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.formats;

import java.util.Arrays;
import java.util.Objects;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateFormat;
import io.helidon.http.Status;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Returns the date formats available for application settings.
 *
 * Each entry contains a stable identifier, a localized title and description,
 * and the formatter pattern used by the application.
 */
public final class LoadDateFormatEndpoint {

    private static final String PREF_LOCALE = "settings.locale";

    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_PATTERN = "pattern";
    private static final String FIELD_FORMATS = "formats";

    private static final String SUFFIX_TITLE = ".title";
    private static final String SUFFIX_DESCRIPTION = ".description";

    private final PreferencesStore preferences;
    private final ResponseWriter writer;

    /**
     * Creates an endpoint from preferences and a response writer.
     *
     * @param preferences resolves the display locale for labels
     * @param writer      writes the JSON response
     * @throws NullPointerException if preferences or writer is null
     */
    public LoadDateFormatEndpoint(PreferencesStore preferences, ResponseWriter writer) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    /**
     * Sends all supported date formats as a localized JSON response.
     *
     * @param request  received HTTP request
     * @param response response that receives the format list
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        var messages = new Messages(Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT)));
        var formats = Arrays.stream(SupportedDateFormat.values())
                .<JsonValue>map(format -> {
                    var key = messageKey(format);
                    return JsonValue.objectBuilder()
                            .set(FIELD_ID, format.id())
                            .set(FIELD_TITLE, messages.get(key + SUFFIX_TITLE))
                            .set(FIELD_DESCRIPTION, messages.get(key + SUFFIX_DESCRIPTION))
                            .set(FIELD_PATTERN, format.pattern())
                            .build();
                })
                .toList();
        var json = JsonValue.objectBuilder()
                .set(FIELD_FORMATS, JsonArray.create(formats))
                .build();
        writer.sendJson(response, Status.OK_200, json);
    }

    private static String messageKey(SupportedDateFormat format) {
        return switch (format) {
            case DD_MM_YYYY_DOT -> "endpoint.system.formats.date.ddmmyyyy.dot";
            case DD_MM_YYYY_SLASH -> "endpoint.system.formats.date.ddmmyyyy.slash";
            case DD_MM_YYYY_DASH -> "endpoint.system.formats.date.ddmmyyyy.dash";
            case MM_DD_YYYY_DOT -> "endpoint.system.formats.date.mmddyyyy.dot";
            case MM_DD_YYYY_SLASH -> "endpoint.system.formats.date.mmddyyyy.slash";
            case MM_DD_YYYY_DASH -> "endpoint.system.formats.date.mmddyyyy.dash";
            case YYYY_MM_DD_DOT -> "endpoint.system.formats.date.yyyymmdd.dot";
            case YYYY_MM_DD_SLASH -> "endpoint.system.formats.date.yyyymmdd.slash";
            case YYYY_MM_DD_DASH -> "endpoint.system.formats.date.yyyymmdd.dash";
            case TECHNICAL -> "endpoint.system.formats.date.technical";
        };
    }

}
