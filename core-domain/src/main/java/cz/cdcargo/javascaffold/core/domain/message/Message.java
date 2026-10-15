package cz.cdcargo.javascaffold.core.domain.message;

import java.util.Objects;

/**
 * Immutable domain message identified by id and described by title.
 *
 * @param id    identifier of the message
 * @param title title of the message
 */
public record Message(MessageID id, MessageTitle title) {

    /**
     * Creates a message with its required id and title.
     *
     * @param id    identifier of the message
     * @param title title of the message
     * @throws NullPointerException if id or title is null
     */
    public Message {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(title, "Title must not be null");
    }
}
