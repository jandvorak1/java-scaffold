package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;
import io.helidon.http.Status;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Handles business-rule violations raised while processing an HTTP request.
 *
 * The client receives an unprocessable response. Known exception types use a
 * specific localized message and all other types use a generic safe message.
 */
public final class BusinessErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(BusinessErrorHandler.class.getName());

    private static final String PREF_LOCALE = "settings.locale";

    private final PreferencesStore preferences;
    private final ErrorResponseWriter writer;

    /**
     * Creates a handler using the supplied preferences and response writer.
     *
     * @param preferences preference store containing the selected locale
     * @param writer      error response writer
     * @throws NullPointerException if preferences or writer is null
     */
    public BusinessErrorHandler(PreferencesStore preferences, ErrorResponseWriter writer) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.writer = Objects.requireNonNull(writer, "Error response writer must not be null");
    }

    /**
     * Logs a business-rule violation and sends an unprocessable response.
     *
     * @param request   HTTP request that violated a business rule
     * @param response  response to complete
     * @param exception business-rule violation
     * @throws NullPointerException if request, response, or exception is null
     */
    public void execute(ServerRequest request, ServerResponse response, AbstractBusinessException exception) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");
        var messages = new Messages(Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT)));
        var status = Status.UNPROCESSABLE_CONTENT_422;

        LOGGER.log(Level.FINE, "Business error occurred", exception);

        var message = BusinessErrorMessages.resolve(messages, exception);
        writer.send(request, response, status, message);
    }
}
