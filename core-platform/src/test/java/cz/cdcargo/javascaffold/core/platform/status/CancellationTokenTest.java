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
        CancellationToken.register(new CancellationToken());
        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }

    @Test
    void testRegisterRejectsNullToken() {
        assertThrows(NullPointerException.class, () -> CancellationToken.register(null));
    }

    @Test
    void testCancel() {
        var token = new CancellationToken();
        CancellationToken.register(token);
        token.cancel();
        assertThrows(CancellationException.class, CancellationToken::throwIfCancelled);
    }

    @Test
    void testIsCancelledReflectsCancellationRequest() {
        var token = new CancellationToken();

        assertFalse(token.isCancelled());
        token.cancel();

        assertTrue(token.isCancelled());
    }

    @Test
    void testCancelIsolated() throws InterruptedException {
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
        other.join();
        assertFalse(threwInOtherThread.get());
    }

    @Test
    void testThrowIfCancelled() {
        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }

    @Test
    void testClear() {
        var token = new CancellationToken();
        CancellationToken.register(token);
        token.cancel();
        CancellationToken.clear();
        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }
}
