package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.domain.message.MessageTitleBlankException;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitleLengthException;
import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;
import cz.cdcargo.javascaffold.infra.db.duckdb.message.LoadAllMessageFailedException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;

/**
 * Resolves localized client messages for business exceptions.
 *
 * Known exception types receive messages that describe the failed validation
 * or operation. All other types receive a generic message so implementation
 * details are not exposed to clients.
 */
public final class BusinessErrorMessages {

    private BusinessErrorMessages() {
    }

    /**
     * Resolves the localized message appropriate for a business exception.
     *
     * @param messages  localized message source
     * @param exception business exception to describe
     * @return client-safe localized error message
     * @throws NullPointerException if messages or exception is null
     */
    public static String resolve(Messages messages, AbstractBusinessException exception) {
        Objects.requireNonNull(messages, "Messages must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");

        return switch (exception) {
            case MessageTitleBlankException _ ->
                messages.get("endpoint.system.errors.business.message.nadpis.blank");
            case MessageTitleLengthException e ->
                messages.get("endpoint.system.errors.business.message.nadpis.length", e.maxLength());
            case LoadAllMessageFailedException _ ->
                messages.get("endpoint.system.errors.business.load.message.failed");
            default -> messages.get("endpoint.system.errors.business.unknown");
        };
    }
}
