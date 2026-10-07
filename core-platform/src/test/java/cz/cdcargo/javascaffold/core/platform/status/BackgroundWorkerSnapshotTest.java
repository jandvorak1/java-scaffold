package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class BackgroundWorkerSnapshotTest {

    @Test
    void testConstructorStoresCompletedWorkerData() {
        var token = "worker-token";
        var state = BackgroundWorkerState.DONE;
        var createdAt = Instant.parse("2026-08-31T10:00:00Z");
        var finishedAt = createdAt.plusSeconds(600);
        var messages = List.of("first", "second");
        var result = "worker-result";
        var snapshot = new BackgroundWorkerSnapshot<>(token, state, createdAt, finishedAt, 2, messages, null, result);

        assertEquals(token, snapshot.token());
        assertEquals(state, snapshot.state());
        assertEquals(createdAt, snapshot.createdAt());
        assertEquals(finishedAt, snapshot.finishedAt());
        assertEquals(2, snapshot.messageOffset());
        assertEquals(messages, snapshot.messages());
        assertEquals(result, snapshot.result());
    }

    @Test
    void testConstructorDefensivelyCopiesMessages() {
        var messages = new ArrayList<>(List.of("first"));
        var snapshot = new BackgroundWorkerSnapshot<Void, String>("token", BackgroundWorkerState.READY, Instant.EPOCH,
                null, 1, messages, null, null);

        messages.add("second");

        assertEquals(List.of("first"), snapshot.messages());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.messages().add("third"));
    }

    @Test
    void testConstructorRejectsInvalidMessageOffset() {
        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<Void, String>("token",
                BackgroundWorkerState.READY, Instant.EPOCH, null, -1, List.of(), null, null));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<Void, String>("token",
                BackgroundWorkerState.READY, Instant.EPOCH, null, 0, List.of("message"), null, null));
    }

    @Test
    void testConstructorRejectsMissingOrBlankToken() {
        assertThrows(NullPointerException.class, () -> new BackgroundWorkerSnapshot<Void, String>(null,
                BackgroundWorkerState.READY, Instant.EPOCH, null, 0, List.of(), null, null));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<Void, String>(" ",
                BackgroundWorkerState.READY, Instant.EPOCH, null, 0, List.of(), null, null));
    }

    @Test
    void testConstructorRejectsInconsistentLifecycleData() {
        var createdAt = Instant.parse("2026-08-31T10:00:00Z");
        var finishedAt = createdAt.plusSeconds(1);
        var error = new Exception("failure");

        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<Void, String>("token",
                BackgroundWorkerState.DONE, createdAt, null, 0, List.of(), null, null));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<Void, String>("token",
                BackgroundWorkerState.RUNNING, createdAt, finishedAt, 0, List.of(), null, null));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<Void, String>("token",
                BackgroundWorkerState.DONE, createdAt, createdAt.minusSeconds(1), 0, List.of(), null, null));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<Void, String>("token",
                BackgroundWorkerState.FAILED, createdAt, finishedAt, 0, List.of(), null, null));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<Void, String>("token",
                BackgroundWorkerState.DONE, createdAt, finishedAt, 0, List.of(), error, null));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundWorkerSnapshot<String, String>("token",
                BackgroundWorkerState.CANCELLED, createdAt, finishedAt, 0, List.of(), null, "result"));
    }
}
