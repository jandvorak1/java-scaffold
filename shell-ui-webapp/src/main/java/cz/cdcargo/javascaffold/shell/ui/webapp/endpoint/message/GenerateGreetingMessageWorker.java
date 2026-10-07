package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.application.message.GenerateGreetingMessageUseCase;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorker;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;
import cz.cdcargo.javascaffold.core.platform.status.TextStatusChannel;
import io.helidon.json.JsonObject;
import io.helidon.json.JsonValue;

/**
 * Generates a greeting message in the background and converts it to JSON.
 *
 * Text status messages emitted by the use case are forwarded as worker
 * progress. Cancellation requests are passed to the use case through its
 * cancellation token.
 */
public final class GenerateGreetingMessageWorker extends BackgroundWorker<JsonObject, String> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";

    private final CancellationToken cancellationToken;
    private final GenerateGreetingMessageUseCase useCase;
    private final String title;

    /**
     * Creates a worker for asynchronous greeting generation with a title.
     *
     * @param token             the worker's unique token
     * @param cancellationToken token used to cancel the running use case
     * @param useCase           use case invoked to generate the greeting
     * @param title             title used to generate the greeting message
     * @throws NullPointerException     if token, cancellationToken, useCase, or
     *                                  title is null
     * @throws IllegalArgumentException if token is blank
     */
    protected GenerateGreetingMessageWorker(String token, CancellationToken cancellationToken,
            GenerateGreetingMessageUseCase useCase, String title) {
        super(token);
        this.cancellationToken = Objects.requireNonNull(cancellationToken, "Cancellation token must not be null");
        this.useCase = Objects.requireNonNull(useCase, "Use case must not be null");
        this.title = Objects.requireNonNull(title, "Title must not be null");
    }

    @Override
    protected JsonObject doInBackground() throws Exception {
        CancellationToken.register(cancellationToken);
        TextStatusChannel.register(this::publish);
        try {
            var result = useCase.execute(title);
            return JsonValue.objectBuilder()
                    .set(FIELD_ID, result.id().value().toString())
                    .set(FIELD_TITLE, result.title().value())
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
