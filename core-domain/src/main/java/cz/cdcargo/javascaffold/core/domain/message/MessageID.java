package cz.cdcargo.javascaffold.core.domain.message;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents the immutable UUID identity of a message.
 *
 * @param value UUID that uniquely identifies the message
 */
public record MessageID(UUID value) {

    /**
     * Creates a message identifier from a UUID.
     *
     * @param value UUID that uniquely identifies the message
     * @throws NullPointerException if value is null
     */
    public MessageID {
        Objects.requireNonNull(value, "Value must not be null");
    }
}
