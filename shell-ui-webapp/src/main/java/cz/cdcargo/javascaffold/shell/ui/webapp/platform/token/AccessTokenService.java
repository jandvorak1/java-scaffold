package cz.cdcargo.javascaffold.shell.ui.webapp.platform.token;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;

/**
 * Provides the application-level operations for session and bearer tokens.
 *
 * Newly issued tokens contain 256 bits of cryptographically secure randomness.
 * Their expiration is calculated from the configured clock and lifetime before
 * they are passed to the backing store.
 */
public final class AccessTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccessTokenStore store;
    private final Clock clock;
    private final Duration sessionLifetime;
    private final Duration bearerLifetime;

    /**
     * Creates a token service with separate session and bearer lifetimes.
     *
     * @param store           store used to persist and authenticate tokens
     * @param clock           clock used to calculate token expiration
     * @param sessionLifetime lifetime of session tokens
     * @param bearerLifetime  lifetime of bearer tokens
     * @throws NullPointerException     if store, clock, or a lifetime is null
     * @throws IllegalArgumentException if a lifetime is not positive
     */
    public AccessTokenService(AccessTokenStore store, Clock clock, Duration sessionLifetime, Duration bearerLifetime) {
        this.store = Objects.requireNonNull(store, "Store must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
        this.sessionLifetime = requirePositive(sessionLifetime, "Session lifetime");
        this.bearerLifetime = requirePositive(bearerLifetime, "Bearer lifetime");
    }

    /**
     * Issues and stores a session token for a principal.
     *
     * @param principal principal associated with the token
     * @return newly issued session token
     * @throws NullPointerException if principal is null
     */
    public String issueSession(Principal principal) {
        Objects.requireNonNull(principal, "Principal must not be null");
        return issue(principal, AccessTokenType.SESSION, sessionLifetime);
    }

    /**
     * Issues and stores a bearer token for a principal.
     *
     * @param principal principal associated with the token
     * @return newly issued bearer token
     * @throws NullPointerException if principal is null
     */
    public String issueBearer(Principal principal) {
        Objects.requireNonNull(principal, "Principal must not be null");
        return issue(principal, AccessTokenType.BEARER, bearerLifetime);
    }

    /**
     * Authenticates a token as a session token.
     *
     * @param token token to authenticate
     * @return the associated principal, or an empty optional when the token is
     *         invalid
     */
    public Optional<Principal> authenticateSession(String token) {
        return store.findPrincipal(token, AccessTokenType.SESSION);
    }

    /**
     * Authenticates a token as a bearer token.
     *
     * @param token token to authenticate
     * @return the associated principal, or an empty optional when the token is
     *         invalid
     */
    public Optional<Principal> authenticateBearer(String token) {
        return store.findPrincipal(token, AccessTokenType.BEARER);
    }

    /**
     * Revokes a token in the backing store.
     *
     * @param token token to revoke
     */
    public void revoke(String token) {
        store.revoke(token);
    }

    /**
     * Requests removal of all expired tokens from the backing store.
     */
    public void removeExpired() {
        store.removeExpired();
    }

    private String issue(Principal principal, AccessTokenType type, Duration lifetime) {
        var token = generateToken();
        store.save(token, type, principal, clock.instant().plus(lifetime));
        return token;
    }

    private static Duration requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name + " must not be null");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return duration;
    }

    private static String generateToken() {
        var bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
