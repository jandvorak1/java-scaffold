package cz.cdcargo.javascaffold.core.domain.message;

import java.util.Objects;

/**
 * Represents an immutable message with an identifier and title.
 *
 * Both components are required value objects, so every message has a stable
 * identity and a validated title.
 *
 * @param id    unique identifier of the message
 * @param title validated title of the message
 */
public record Message(MessageID id, MessageTitle title) {

    /**
     * Creates a message from its required identifier and title.
     *
     * @param id    unique identifier of the message
     * @param title validated title of the message
     * @throws NullPointerException if id or title is null
     */
    public Message {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(title, "Title must not be null");
    }
}
