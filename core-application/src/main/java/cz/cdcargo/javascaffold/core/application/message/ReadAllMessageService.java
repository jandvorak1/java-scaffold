package cz.cdcargo.javascaffold.core.application.message;

import java.util.List;
import java.util.Objects;

import cz.cdcargo.javascaffold.core.domain.message.Message;

/**
 * Reads messages through an outbound port and returns an immutable snapshot.
 *
 * The service preserves source order and rejects a null result or null message
 * before creating the snapshot.
 */
public final class ReadAllMessageService implements ReadAllMessageUseCase {

    private final LoadAllMessagePort loadAllMessagePort;

    /**
     * Creates a service that loads messages through an outbound port.
     *
     * @param loadAllMessagePort port used to obtain messages
     * @throws NullPointerException if loadAllMessagePort is null
     */
    public ReadAllMessageService(LoadAllMessagePort loadAllMessagePort) {
        this.loadAllMessagePort = Objects.requireNonNull(loadAllMessagePort,
                "Load all messages port must not be null");
    }

    @Override
    public List<Message> execute() {
        var messages = Objects.requireNonNull(loadAllMessagePort.execute(), "Loaded messages must not be null");
        for (var message : messages) {
            Objects.requireNonNull(message, "Loaded messages must not contain null");
        }
        return List.copyOf(messages);
    }
}
