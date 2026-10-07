package cz.cdcargo.javascaffold.shell.ui.webapp.platform.token;

import java.time.Instant;
import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;

/**
 * Describes a persisted access token without retaining its original secret.
 *
 * The hash identifies the token in a store, while the type, principal, and
 * expiration instant define how and for whom it may be used.
 *
 * @param hash      hashed token value
 * @param type      token type
 * @param principal principal associated with the token
 * @param expiresAt instant at which the token expires
 */
public record AccessToken(String hash, AccessTokenType type, Principal principal, Instant expiresAt) {

    public AccessToken {
        Objects.requireNonNull(hash, "Hash must not be null");
        Objects.requireNonNull(type, "Type must not be null");
        Objects.requireNonNull(principal, "Principal must not be null");
        Objects.requireNonNull(expiresAt, "Expiration time must not be null");
        if (hash.isBlank()) {
            throw new IllegalArgumentException("Hash must not be blank");
        }
    }

    /**
     * Determines whether the token is expired at the supplied instant.
     *
     * @param now instant used for the expiration check
     * @return true when the expiration instant is equal to or earlier than now
     * @throws NullPointerException if now is null
     */
    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "Current time must not be null");
        return !expiresAt.isAfter(now);
    }
}
