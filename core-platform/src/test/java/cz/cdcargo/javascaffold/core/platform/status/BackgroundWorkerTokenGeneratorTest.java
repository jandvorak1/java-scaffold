package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class BackgroundWorkerTokenGeneratorTest {

    @Test
    void testGenerateReturnsDistinctUrlSafeTokens() {
        var token = BackgroundWorkerTokenGenerator.generate();
        var anotherToken = BackgroundWorkerTokenGenerator.generate();
        var decoded = assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(token));

        assertFalse(token.isBlank());
        assertEquals(43, token.length());
        assertEquals(32, decoded.length);
        assertNotEquals(token, anotherToken);
    }
}
