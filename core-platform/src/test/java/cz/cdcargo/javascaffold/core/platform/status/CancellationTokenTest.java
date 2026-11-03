package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CancellationException;
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
    void testClear() {
        // Arrange
        var token = new CancellationToken();

        // Act + Assert
        CancellationToken.register(token);
        token.cancel();
        CancellationToken.clear();
        assertDoesNotThrow(CancellationToken::throwIfCancelled);
    }

    @Test
    void testThrowIfCancelled() {
        // Arrange + Act + Assert
        assertDoesNotThrow(CancellationToken::throwIfCancelled);
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
    void testIsCancelled() {
        // Arrange
        var token = new CancellationToken();

        // Act + Assert
        assertFalse(token.isCancelled());
        token.cancel();
        assertTrue(token.isCancelled());
    }
}
