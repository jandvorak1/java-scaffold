package cz.cdcargo.javascaffold.infra.db.inmemory.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;
import cz.cdcargo.javascaffold.core.platform.status.TextStatusChannel;

class LoadAllMessageAdapterTest {

    private static final List<String> MESSAGE_TITLES = List.of(
            "Hello World!", "Hello Czechia!", "Hello Slovakia!",
            "Hello Poland!", "Hello Austria!", "Hello Germany!");

    @AfterEach
    void tearDown() {
        CancellationToken.clear();
        TextStatusChannel.clear();
        Thread.interrupted();
    }

    @Test
    void testExecuteReturnsMessagesAndPublishesProgress() {
        var statuses = new ArrayList<String>();
        TextStatusChannel.register(statuses::add);

        var output = new LoadAllMessageAdapter(() -> "Loading message {0}").execute();

        assertEquals(MESSAGE_TITLES, output.stream().map(message -> message.title().value()).toList());
        assertEquals(List.of("Loading message 1", "Loading message 2", "Loading message 3", "Loading message 4",
                "Loading message 5", "Loading message 6"), statuses);
    }

    @Test
    void testExecuteWrapsCancellation() {
        var token = new CancellationToken();
        token.cancel();
        CancellationToken.register(token);

        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> "test").execute());

        assertInstanceOf(CancellationException.class, exception.getCause());
    }

    @Test
    void testExecuteWrapsCancellationDuringProcessing() {
        var token = new CancellationToken();
        CancellationToken.register(token);

        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> {
                    token.cancel();
                    return "test";
                }).execute());

        assertInstanceOf(CancellationException.class, exception.getCause());
    }

    @Test
    void testExecuteWrapsSupplierFailure() {
        var cause = new IllegalStateException("Failure");

        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> {
                    throw cause;
                }).execute());

        assertSame(cause, exception.getCause());
    }

    @Test
    void testExecuteWrapsNullStatusMessage() {
        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> null).execute());

        var cause = assertInstanceOf(NullPointerException.class, exception.getCause());
        assertEquals("Busy message must not be null", cause.getMessage());
    }

    @Test
    void testExecuteRestoresInterruptedStatus() {
        Thread.currentThread().interrupt();

        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> "test").execute());

        assertInstanceOf(InterruptedException.class, exception.getCause());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void testConstructorRejectsNullBusyMessageSupplier() {
        var nullSupplierException = assertThrows(NullPointerException.class, () -> new LoadAllMessageAdapter(null));

        assertEquals("Busy message supplier must not be null", nullSupplierException.getMessage());
    }
}
