package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class BackgroundWorkerSnapshotTest {

    @Test
    void testConstructor() {
        // Arrange
        var token = "worker-token";
        var state = BackgroundWorkerState.DONE;
        var createdAt = Instant.parse("2026-08-31T10:00:00Z");
        var finishedAt = createdAt.plusSeconds(600);
        var messages = List.of("first", "second");
        var result = "worker-result";

        // Act
        var snapshot = new BackgroundWorkerSnapshot<>(token, state, createdAt, finishedAt, 2, messages, null, result);

        // Assert
        assertEquals(token, snapshot.token());
        assertEquals(state, snapshot.state());
        assertEquals(createdAt, snapshot.createdAt());
        assertEquals(finishedAt, snapshot.finishedAt());
        assertEquals(2, snapshot.messageOffset());
        assertEquals(messages, snapshot.messages());
        assertEquals(result, snapshot.result());
    }
}
