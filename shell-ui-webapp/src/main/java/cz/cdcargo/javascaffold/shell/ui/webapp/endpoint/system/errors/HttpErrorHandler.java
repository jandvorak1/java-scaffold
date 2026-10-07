package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;
import io.helidon.http.HttpException;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Handles HTTP exceptions raised while processing a request.
 *
 * The exception status is preserved and the client receives a localized
 * generic message. The original exception is retained only in the diagnostic
 * log.
 */
public final class HttpErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(HttpErrorHandler.class.getName());

    private static final String PREF_LOCALE = "settings.locale";
    private static final String MESSAGE_KEY_HTTP = "endpoint.system.errors.http.message";

    private final PreferencesStore preferences;
    private final ErrorResponseWriter writer;

    /**
     * Creates a handler using the supplied preferences and response writer.
     *
     * @param preferences preference store containing the selected locale
     * @param writer      error response writer
     * @throws NullPointerException if preferences or writer is null
     */
    public HttpErrorHandler(PreferencesStore preferences, ErrorResponseWriter writer) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Error response writer must not be null");
    }

    /**
     * Logs an HTTP exception and sends a response with its status.
     *
     * @param request   HTTP request that caused the exception
     * @param response  response to complete
     * @param exception HTTP exception
     * @throws NullPointerException if request, response, or exception is null
     */
    public void execute(ServerRequest request, ServerResponse response, HttpException exception) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");
        var messages = new Messages(Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT)));
        var status = exception.status();

        LOGGER.log(Level.WARNING, "HTTP error occurred", exception);

        var message = messages.get(MESSAGE_KEY_HTTP);
        writer.send(request, response, status, message);
    }
}
