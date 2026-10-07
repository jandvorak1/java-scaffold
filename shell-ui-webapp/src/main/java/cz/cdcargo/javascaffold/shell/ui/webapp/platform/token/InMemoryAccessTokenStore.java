package cz.cdcargo.javascaffold.shell.ui.webapp.platform.token;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import io.helidon.common.Base64Value;
import io.helidon.common.crypto.HashDigest;

/**
 * Provides concurrent in-memory storage for access tokens.
 *
 * Raw token secrets are converted to SHA-512/256 hashes before becoming map
 * keys and are never retained. Expired entries are removed when accessed or
 * during explicit cleanup.
 */
public final class InMemoryAccessTokenStore implements AccessTokenStore {

    private final Map<String, AccessToken> tokens = new ConcurrentHashMap<>();
    private final Clock clock;

    /**
     * Creates an empty store using the UTC system clock.
     */
    public InMemoryAccessTokenStore() {
        this(Clock.systemUTC());
    }

    /**
     * Creates an empty store using the supplied clock for expiration checks.
     *
     * @param clock clock used to evaluate token expiration
     * @throws NullPointerException if clock is null
     */
    public InMemoryAccessTokenStore(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public void save(String token, AccessTokenType type, Principal principal, Instant expiresAt) {
        Objects.requireNonNull(token, "Token must not be null");
        Objects.requireNonNull(type, "Type must not be null");
        Objects.requireNonNull(principal, "Principal must not be null");
        Objects.requireNonNull(expiresAt, "Expiration time must not be null");
        if (token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }
        if (!expiresAt.isAfter(clock.instant())) {
            throw new IllegalArgumentException("Expiration time must be in the future");
        }
        var hash = hash(token);
        tokens.put(hash, new AccessToken(hash, type, principal, expiresAt));
    }

    @Override
    public Optional<Principal> findPrincipal(String token, AccessTokenType type) {
        Objects.requireNonNull(type, "Type must not be null");
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        var hash = hash(token);
        var accessToken = tokens.get(hash);
        if (accessToken == null) {
            return Optional.empty();
        }
        if (accessToken.type() != type) {
            return Optional.empty();
        }
        if (isExpired(accessToken)) {
            tokens.remove(hash, accessToken);
            return Optional.empty();
        }
        return Optional.of(accessToken.principal());
    }

    @Override
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        tokens.remove(hash(token));
    }

    @Override
    public void removeExpired() {
        tokens.values().removeIf(this::isExpired);
    }

    private boolean isExpired(AccessToken accessToken) {
        return accessToken.isExpired(clock.instant());
    }

    private static String hash(String token) {
        var digest = HashDigest.builder().algorithm(HashDigest.ALGORITHM_SHA_512_256).build();
        var hash = digest.digest(Base64Value.create(token.getBytes(StandardCharsets.UTF_8)));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash.toBytes());
    }
}
