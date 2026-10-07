package cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BearerTokenIdentityTest {

    @Test
    void testBearerTokenIdentityWithNullService() {
        var exception = assertThrows(NullPointerException.class, () -> new BearerTokenIdentity(null));

        assertEquals("Access token service must not be null", exception.getMessage());
    }

    @Test
    void testReadBearerToken() {
        assertEquals("token", BearerTokenIdentity.readBearerToken("Bearer token"));
    }

    @Test
    void testReadBearerTokenWithLowercaseScheme() {
        assertEquals("token", BearerTokenIdentity.readBearerToken("bearer token"));
    }

    @Test
    void testReadBearerTokenWithInvalidScheme() {
        assertEquals("", BearerTokenIdentity.readBearerToken("Basic token"));
    }

    @Test
    void testReadBearerTokenWithBlankToken() {
        assertEquals("", BearerTokenIdentity.readBearerToken("Bearer "));
    }
}
