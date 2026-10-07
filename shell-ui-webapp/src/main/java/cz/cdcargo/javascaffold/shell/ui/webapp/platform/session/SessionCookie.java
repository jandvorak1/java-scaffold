package cz.cdcargo.javascaffold.shell.ui.webapp.platform.session;

import java.time.Duration;
import java.util.Objects;

import io.helidon.http.SetCookie;
import io.helidon.webserver.http.ServerRequest;

/**
 * Defines the HTTP cookie used to transport a session token.
 *
 * Created cookies are restricted to the application root, inaccessible to
 * client-side scripts, and use the strict same-site policy. The secure flag is
 * controlled by the application configuration.
 */
public final class SessionCookie {

    private final String name;
    private final boolean secure;

    /**
     * Creates a session cookie definition with a validated HTTP cookie name.
     *
     * @param name   cookie name
     * @param secure whether the cookie must be sent only over secure connections
     * @throws NullPointerException     if name is null
     * @throws IllegalArgumentException if name is blank or contains a character
     *                                  that is not permitted in a cookie name
     */
    public SessionCookie(String name, boolean secure) {
        this.name = requireCookieName(name);
        this.secure = secure;
    }

    /**
     * Reads the first matching session cookie from an HTTP request.
     *
     * @param request HTTP request containing the session cookie
     * @return session token, or an empty string when the cookie is absent
     * @throws NullPointerException if request is null
     */
    public String read(ServerRequest request) {
        Objects.requireNonNull(request, "Server request must not be null");
        return request.headers().cookies().first(name).orElse("");
    }

    /**
     * Returns the name used for session cookies.
     *
     * @return cookie name
     */
    public String name() {
        return name;
    }

    /**
     * Creates a session cookie containing the supplied token.
     *
     * @param token  session token
     * @param maxAge maximum cookie lifetime
     * @return session cookie header value
     * @throws NullPointerException     if token or maxAge is null
     * @throws IllegalArgumentException if token is blank or maxAge is not positive
     */
    public SetCookie create(String token, Duration maxAge) {
        Objects.requireNonNull(token, "Token must not be null");
        Objects.requireNonNull(maxAge, "Max age must not be null");
        if (token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }
        if (maxAge.isZero() || maxAge.isNegative()) {
            throw new IllegalArgumentException("Max age must be positive");
        }
        return SetCookie.builder(name, token).httpOnly(true).secure(secure).sameSite(SetCookie.SameSite.STRICT)
                .path("/").maxAge(maxAge).build();
    }

    /**
     * Creates an expired session cookie that removes the token from the client.
     *
     * @return expired session cookie header value
     */
    public SetCookie clear() {
        return SetCookie.builder(name, "").httpOnly(true).secure(secure).sameSite(SetCookie.SameSite.STRICT).path("/")
                .maxAge(Duration.ZERO).build();
    }

    private static String requireCookieName(String name) {
        Objects.requireNonNull(name, "Cookie name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Cookie name must not be blank");
        }
        for (var index = 0; index < name.length(); index++) {
            var character = name.charAt(index);
            if (character <= 0x20 || character >= 0x7f || "()<>@,;:\\\"/[]?={}".indexOf(character) >= 0) {
                throw new IllegalArgumentException("Invalid cookie name: " + name);
            }
        }
        return name;
    }
}
