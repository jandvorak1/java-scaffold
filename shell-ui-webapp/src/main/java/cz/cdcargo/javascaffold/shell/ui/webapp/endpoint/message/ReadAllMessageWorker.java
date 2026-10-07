package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.application.message.ReadAllMessageUseCase;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorker;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;
import cz.cdcargo.javascaffold.core.platform.status.TextStatusChannel;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonObject;
import io.helidon.json.JsonValue;

/**
 * Retrieves all messages in the background and converts them to JSON.
 *
 * Text status messages emitted by the use case are forwarded as worker
 * progress. Cancellation requests are passed to the use case through its
 * cancellation token.
 */
public final class ReadAllMessageWorker extends BackgroundWorker<JsonObject, String> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_RECORDS = "records";

    private final CancellationToken cancellationToken;
    private final ReadAllMessageUseCase useCase;

    /**
     * Creates a worker for asynchronous retrieval of all messages.
     *
     * @param token             the worker's unique token
     * @param cancellationToken token used to cancel the running use case
     * @param useCase           use case invoked to retrieve the messages
     * @throws NullPointerException     if token, cancellationToken, or useCase is
     *                                  null
     * @throws IllegalArgumentException if token is blank
     */
    protected ReadAllMessageWorker(String token, CancellationToken cancellationToken, ReadAllMessageUseCase useCase) {
        super(token);
        this.cancellationToken = Objects.requireNonNull(cancellationToken, "Cancellation token must not be null");
        this.useCase = Objects.requireNonNull(useCase, "Use case must not be null");
    }

    @Override
    protected JsonObject doInBackground() throws Exception {
        CancellationToken.register(cancellationToken);
        TextStatusChannel.register(this::publish);
        try {
            var records = useCase.execute().stream()
                    .<JsonValue>map(message -> JsonValue.objectBuilder()
                            .set(FIELD_ID, message.id().value().toString())
                            .set(FIELD_TITLE, message.title().value())
                            .build())
                    .toList();
            return JsonValue.objectBuilder()
                    .set(FIELD_RECORDS, JsonArray.create(records))
                    .build();
        } finally {
            try {
                TextStatusChannel.clear();
            } finally {
                CancellationToken.clear();
            }
        }
    }

    @Override
    protected void onCancelRequested() {
        cancellationToken.cancel();
    }
}
