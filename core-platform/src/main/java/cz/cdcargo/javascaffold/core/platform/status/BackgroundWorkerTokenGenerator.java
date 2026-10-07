package cz.cdcargo.javascaffold.core.platform.status;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates cryptographically strong 256-bit tokens for background workers.
 *
 * Tokens are encoded as URL-safe Base64 text without padding.
 */
public final class BackgroundWorkerTokenGenerator {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder TOKEN_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private BackgroundWorkerTokenGenerator() {
    }

    /**
     * Creates a token from cryptographically strong random bytes.
     *
     * @return a new background worker token
     */
    public static String generate() {
        var bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return TOKEN_ENCODER.encodeToString(bytes);
    }
}
