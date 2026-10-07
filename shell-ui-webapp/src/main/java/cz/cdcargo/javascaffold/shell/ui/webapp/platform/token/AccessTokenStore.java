package cz.cdcargo.javascaffold.shell.ui.webapp.platform.token;

import java.time.Instant;
import java.util.Optional;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;

/**
 * Defines persistence operations for typed access tokens.
 *
 * Implementations receive raw token values at the boundary and are responsible
 * for protecting them appropriately while stored.
 */
public interface AccessTokenStore {

    /**
     * Persists a token and its authentication metadata.
     *
     * @param token     token value to store
     * @param type      token type
     * @param principal principal associated with the token
     * @param expiresAt token expiration instant
     */
    void save(String token, AccessTokenType type, Principal principal, Instant expiresAt);

    /**
     * Finds a non-expired token of the expected type.
     *
     * @param token token value to authenticate
     * @param type  expected token type
     * @return associated principal, or an empty optional when the token is
     *         missing, invalid, expired, or has another type
     */
    Optional<Principal> findPrincipal(String token, AccessTokenType type);

    /**
     * Revokes a token if it is present.
     *
     * @param token token value to revoke
     */
    void revoke(String token);

    /**
     * Removes every token that has reached its expiration instant.
     */
    void removeExpired();
}
