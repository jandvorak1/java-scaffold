package cz.cdcargo.javascaffold.core.application.message;

import java.util.List;

import cz.cdcargo.javascaffold.core.domain.message.Message;

/**
 * Defines the inbound application boundary for reading all available messages.
 *
 * Implementations return a non-null immutable snapshot without null elements
 * in source order.
 */
@FunctionalInterface
public interface ReadAllMessageUseCase {

    /**
     * Reads all messages currently available to the application.
     *
     * @return a non-null, unmodifiable list without null elements in source
     *         order, or an empty list when no messages are available
     */
    List<Message> execute();
}
