package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CancellationTokenTest {

    @BeforeEach
    void setUp() {
        CancellationToken.clear();
    }

    @AfterEach
    void tearDown() {
        CancellationToken.clear();
    }

    @Test
    void testRegister() {
        // Arrange + Act
        CancellationToken.register(new CancellationToken());

        // Assert
        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }

    @Test
    void testRegisterRejectsNullToken() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> CancellationToken.register(null));
    }

    @Test
    void testCancel() {
        // Arrange + Act
        var token = new CancellationToken();
        CancellationToken.register(token);
        token.cancel();

        // Assert
        assertThrows(CancellationException.class, CancellationToken::throwIfCancelled);
    }

    @Test
    void testIsCancelledReflectsCancellationRequest() {
        // Arrange
        var token = new CancellationToken();

        // Act + Assert
        assertFalse(token.isCancelled());
        token.cancel();
        assertTrue(token.isCancelled());
    }

    @Test
    void testCancelIsolated() throws InterruptedException {
        // Arrange
        var token = new CancellationToken();
        CancellationToken.register(token);
        token.cancel();
        var threwInOtherThread = new AtomicBoolean(false);
        var other = Thread.ofVirtual().start(() -> {
            try {
                CancellationToken.throwIfCancelled();
            } catch (CancellationException e) {
                threwInOtherThread.set(true);
            }
        });

        // Act
        other.join();

        // Assert
        assertFalse(threwInOtherThread.get());
    }

    @Test
    void testThrowIfCancelled() {
        // Arrange + Act + Assert
        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }

    @Test
    void testClear() {
        // Arrange
        var token = new CancellationToken();

        // Act + Assert
        CancellationToken.register(token);
        token.cancel();
        CancellationToken.clear();
        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }
}
