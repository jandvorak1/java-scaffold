package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors.BusinessErrorMessages;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthenticationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.PathParameters;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.QueryParameters;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;
import io.helidon.http.HttpException;
import io.helidon.http.Status;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonObject;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Returns the current state, output, and result of a background worker.
 *
 * A zero-based message offset can limit the response to output published since
 * a previous request. Worker failures are converted to localized client
 * messages.
 */
public final class StatusJobEndpoint {

    private static final String PATH_PARAM_TOKEN = "token";
    private static final String QUERY_PARAM_OFFSET = "offset";
    private static final String PREF_LOCALE = "settings.locale";

    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_OFFSET = "offset";
    private static final String FIELD_MESSAGES = "messages";
    private static final String FIELD_ERROR = "error";
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_RESULT = "result";

    private static final String MESSAGE_KEY_AUTHENTICATION = "endpoint.system.errors.authentication.message";
    private static final String MESSAGE_KEY_AUTHORIZATION = "endpoint.system.errors.authorization.message";
    private static final String MESSAGE_KEY_HTTP = "endpoint.system.errors.http.message";
    private static final String MESSAGE_KEY_UNEXPECTED = "endpoint.system.errors.unexpected.message";

    private final ResponseWriter writer;
    private final PreferencesStore preferences;
    private final BackgroundWorkerRegistry registry;

    /**
     * Creates an endpoint from preferences, a response writer, and a worker
     * registry.
     *
     * @param preferences resolves the error message locale
     * @param writer      sends the job status response
     * @param registry    finds workers by their tokens
     * @throws NullPointerException if any dependency is null
     */
    public StatusJobEndpoint(PreferencesStore preferences, ResponseWriter writer, BackgroundWorkerRegistry registry) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
        this.registry = Objects.requireNonNull(registry, "Background worker registry must not be null");
    }

    /**
     * Sends the worker state, new messages, and an optional result or error.
     *
     * @param request  supplies the worker token and optional message offset
     * @param response receives the JSON status document
     * @throws NullPointerException                if request or response is null
     * @throws io.helidon.http.BadRequestException if the worker token or offset
     *                                             is invalid
     * @throws java.util.NoSuchElementException    if no worker exists for the token
     * @throws IllegalStateException               if the worker result is not a
     *                                             JSON object
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        var token = PathParameters.of(request).requiredNonBlankString(PATH_PARAM_TOKEN);
        var offset = QueryParameters.of(request).optional(QUERY_PARAM_OFFSET, StatusJobEndpoint::parseOffset).orElse(0);
        var worker = registry.get(token);
        var snapshot = worker.snapshot(offset);
        var messages = snapshot.messages().stream().map(this::toJsonMessage).toList();
        var json = JsonValue.objectBuilder()
                .set(FIELD_TOKEN, snapshot.token())
                .set(FIELD_STATE, snapshot.state().name())
                .set(FIELD_OFFSET, snapshot.messageOffset())
                .set(FIELD_MESSAGES, JsonArray.create(messages));

        if (snapshot.error() != null) {
            var localizedMessages = new Messages(Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT)));
            json.set(FIELD_ERROR, JsonValue.objectBuilder()
                    .set(FIELD_MESSAGE, resolveErrorMessage(snapshot.error(), localizedMessages))
                    .build());
        }

        if (snapshot.result() != null) {
            if (snapshot.result() instanceof JsonObject result) {
                json.set(FIELD_RESULT, result);
            } else {
                throw new IllegalStateException("Unexpected job result type");
            }
        }
        writer.sendJson(response, Status.OK_200, json.build());
    }

    private static int parseOffset(String value) {
        var offset = Integer.parseInt(value);
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }
        return offset;
    }

    private JsonValue toJsonMessage(Object message) {
        return JsonValue.objectBuilder()
                .set(FIELD_MESSAGE, String.valueOf(message))
                .build();
    }

    private String resolveErrorMessage(Throwable error, Messages messages) {
        if (error instanceof AbstractBusinessException exception) {
            return BusinessErrorMessages.resolve(messages, exception);
        } else if (error instanceof AuthenticationException) {
            return messages.get(MESSAGE_KEY_AUTHENTICATION);
        } else if (error instanceof AuthorizationException) {
            return messages.get(MESSAGE_KEY_AUTHORIZATION);
        } else if (error instanceof HttpException) {
            return messages.get(MESSAGE_KEY_HTTP);
        } else {
            return messages.get(MESSAGE_KEY_UNEXPECTED);
        }
    }
}
