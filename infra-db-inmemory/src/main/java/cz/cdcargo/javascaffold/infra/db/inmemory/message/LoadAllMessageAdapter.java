package cz.cdcargo.javascaffold.infra.db.inmemory.message;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import cz.cdcargo.javascaffold.core.application.message.LoadAllMessagePort;
import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageID;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitle;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;
import cz.cdcargo.javascaffold.core.platform.status.TextStatusChannel;

/**
 * Loads the predefined in-memory messages in their configured order.
 *
 * Each message is processed with a one-second delay. The adapter checks for
 * cancellation before and after the delay and publishes progress for every
 * completed message. The {0} placeholder in a progress message is replaced by
 * the one-based number of that message. Failures, including interruption, are
 * reported as LoadAllMessageFailedException. Interrupted threads retain their
 * interrupted status.
 */
public final class LoadAllMessageAdapter implements LoadAllMessagePort {

    private static final Duration PROCESSING_DELAY = Duration.ofSeconds(1);
    private static final List<String> MESSAGE_TITLES = List.of(
            "Hello World!", "Hello Czechia!", "Hello Slovakia!",
            "Hello Poland!", "Hello Austria!", "Hello Germany!");

    private final Supplier<String> busyMessageSupplier;

    /**
     * Creates an adapter that obtains a progress message for each loaded
     * message.
     *
     * @param busyMessageSupplier supplier of progress messages that can contain
     *                            the {0} placeholder
     * @throws NullPointerException if busyMessageSupplier is null
     */
    public LoadAllMessageAdapter(Supplier<String> busyMessageSupplier) {
        this.busyMessageSupplier = Objects.requireNonNull(busyMessageSupplier,
                "Busy message supplier must not be null");
    }

    @Override
    public List<Message> execute() {
        var messages = new ArrayList<Message>(MESSAGE_TITLES.size());
        try {
            for (var title : MESSAGE_TITLES) {
                CancellationToken.throwIfCancelled();
                messages.add(new Message(new MessageID(UUID.randomUUID()), new MessageTitle(title)));

                Thread.sleep(PROCESSING_DELAY);
                CancellationToken.throwIfCancelled();
                var busyMessage = Objects.requireNonNull(busyMessageSupplier.get(),
                        "Busy message must not be null");
                TextStatusChannel.send(busyMessage.replace("{0}", Integer.toString(messages.size())));
            }
            return messages;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoadAllMessageFailedException(e);
        } catch (RuntimeException e) {
            throw new LoadAllMessageFailedException(e);
        }
    }
}
