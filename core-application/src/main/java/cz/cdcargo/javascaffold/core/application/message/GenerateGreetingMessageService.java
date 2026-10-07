package cz.cdcargo.javascaffold.core.application.message;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageID;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitle;

/**
 * Creates greeting messages from supplied titles.
 *
 * The service preserves the supplied title and surrounds it with a greeting.
 * Empty and blank titles produce valid non-blank greetings. The completed
 * greeting is validated before an identifier is generated.
 */
public final class GenerateGreetingMessageService implements GenerateGreetingMessageUseCase {

    private final Supplier<UUID> idGenerator;

    /**
     * Creates a service that generates random message identifiers.
     */
    public GenerateGreetingMessageService() {
        this(UUID::randomUUID);
    }

    /**
     * Creates a service that obtains message identifiers from a supplier.
     *
     * The supplier is called once for every title that produces a valid
     * greeting. It must return a non-null UUID.
     *
     * @param idGenerator supplier of identifiers for valid greeting messages
     * @throws NullPointerException if idGenerator is null
     */
    public GenerateGreetingMessageService(Supplier<UUID> idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "ID generator must not be null");
    }

    @Override
    public Message execute(String title) {
        Objects.requireNonNull(title, "Title must not be null");
        var greeting = new MessageTitle("Hello " + title + "!");
        var id = Objects.requireNonNull(idGenerator.get(), "Generated ID must not be null");
        return new Message(new MessageID(id), greeting);
    }
}
