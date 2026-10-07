package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Handles authorization failures raised while processing an HTTP request.
 *
 * A forbidden response containing a localized generic message is sent to the
 * client. The original exception is retained only in the diagnostic log.
 */
public final class AuthorizationErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(AuthorizationErrorHandler.class.getName());

    private static final String PREF_LOCALE = "settings.locale";
    private static final String MESSAGE_KEY_AUTHORIZATION = "endpoint.system.errors.authorization.message";

    private final PreferencesStore preferences;
    private final ErrorResponseWriter writer;

    /**
     * Creates a handler using the supplied preferences and response writer.
     *
     * @param preferences preference store containing the selected locale
     * @param writer      error response writer
     * @throws NullPointerException if preferences or writer is null
     */
    public AuthorizationErrorHandler(PreferencesStore preferences, ErrorResponseWriter writer) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Error response writer must not be null");
    }

    /**
     * Logs an authorization failure and sends a forbidden response.
     *
     * @param request   HTTP request for which access was denied
     * @param response  response to complete
     * @param exception authorization failure
     * @throws NullPointerException if request, response, or exception is null
     */
    public void execute(ServerRequest request, ServerResponse response, AuthorizationException exception) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");
        var messages = new Messages(Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT)));
        var status = Status.FORBIDDEN_403;

        LOGGER.log(Level.FINE, "Authorization error occurred", exception);

        var message = messages.get(MESSAGE_KEY_AUTHORIZATION);
        writer.send(request, response, status, message);
    }
}
