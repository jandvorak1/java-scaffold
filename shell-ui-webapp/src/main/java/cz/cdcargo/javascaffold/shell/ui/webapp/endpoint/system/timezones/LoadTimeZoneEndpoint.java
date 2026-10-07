package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.timezones;

import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.TimeZones;
import io.helidon.http.Status;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Returns the time zones available for application settings.
 *
 * Each response entry provides an identifier and a short display name. The
 * JSON document is prepared when the class is initialized because the
 * supported time zones remain unchanged while the application runs.
 */
public final class LoadTimeZoneEndpoint {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_TIMEZONES = "timezones";
    private static final JsonValue TIME_ZONES_RESPONSE = createResponse();

    private final ResponseWriter writer;

    /**
     * Creates an endpoint that writes time-zone responses.
     *
     * @param writer sends the JSON response
     * @throws NullPointerException if writer is null
     */
    public LoadTimeZoneEndpoint(ResponseWriter writer) {
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
    }

    /**
     * Sends the sorted supported time zones as a successful JSON response.
     *
     * @param request  received HTTP request
     * @param response response that receives the time zones
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        writer.sendJson(response, Status.OK_200, TIME_ZONES_RESPONSE);
    }

    private static JsonValue createResponse() {
        var timeZones = TimeZones.supportedSorted().stream()
                .<JsonValue>map(zone -> JsonValue.objectBuilder()
                        .set(FIELD_ID, zone.getId())
                        .set(FIELD_NAME, zone.getDisplayName(TextStyle.SHORT, Locale.ROOT))
                        .build())
                .toList();
        return JsonValue.objectBuilder()
                .set(FIELD_TIMEZONES, JsonArray.create(timeZones))
                .build();
    }
}
