package cz.cdcargo.javascaffold.core.domain.message;

import java.io.Serial;

import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;

/**
 * Signals that a message title is blank.
 */
public final class MessageTitleBlankException extends AbstractBusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception describing a blank message title.
     */
    public MessageTitleBlankException() {
        super("Message title must not be blank");
    }
}
