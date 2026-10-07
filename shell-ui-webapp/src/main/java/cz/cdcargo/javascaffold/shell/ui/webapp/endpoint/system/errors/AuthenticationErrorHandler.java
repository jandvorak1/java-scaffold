package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthenticationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Handles authentication failures raised while processing an HTTP request.
 *
 * An unauthorized response containing a localized generic message is sent to
 * the client. The original exception is retained only in the diagnostic log.
 */
public final class AuthenticationErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationErrorHandler.class.getName());

    private static final String PREF_LOCALE = "settings.locale";
    private static final String MESSAGE_KEY_AUTHENTICATION = "endpoint.system.errors.authentication.message";

    private final PreferencesStore preferences;
    private final ErrorResponseWriter writer;

    /**
     * Creates a handler using the supplied preferences and response writer.
     *
     * @param preferences preference store containing the selected locale
     * @param writer      error response writer
     * @throws NullPointerException if preferences or writer is null
     */
    public AuthenticationErrorHandler(PreferencesStore preferences, ErrorResponseWriter writer) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Error response writer must not be null");
    }

    /**
     * Logs an authentication failure and sends an unauthorized response.
     *
     * @param request   HTTP request that failed authentication
     * @param response  response to complete
     * @param exception authentication failure
     * @throws NullPointerException if request, response, or exception is null
     */
    public void execute(ServerRequest request, ServerResponse response, AuthenticationException exception) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");
        var messages = new Messages(Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT)));
        var status = Status.UNAUTHORIZED_401;

        LOGGER.log(Level.FINE, "Authentication error occurred", exception);

        var message = messages.get(MESSAGE_KEY_AUTHENTICATION);
        writer.send(request, response, status, message);
    }
}
