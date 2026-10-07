package cz.cdcargo.javascaffold.core.application.message;

import java.util.List;

import cz.cdcargo.javascaffold.core.domain.message.Message;

/**
 * Defines the outbound application boundary for loading all messages.
 *
 * Implementations return a non-null list without null elements. They determine
 * the concrete source, record order, and mutability of that list.
 */
@FunctionalInterface
public interface LoadAllMessagePort {

    /**
     * Loads all messages currently available from the configured source.
     *
     * @return a non-null list without null elements, or an empty list when no
     *         messages are available
     */
    List<Message> execute();
}
