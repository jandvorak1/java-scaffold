package cz.cdcargo.javascaffold.core.domain.message;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable UUID value object that identifies a message.
 *
 * @param value UUID identifier of the message
 */
public record MessageID(UUID value) {

    /**
     * Creates a message identifier from value.
     *
     * @param value UUID identifier of the message
     * @throws NullPointerException if value is null
     */
    public MessageID {
        Objects.requireNonNull(value, "Value must not be null");
    }
}
