package cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity;

import java.util.Objects;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Role;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Permission;
import io.helidon.webserver.http.ServerRequest;

/**
 * Defines authentication and authorization operations for HTTP requests.
 *
 * Implementations resolve an authenticated principal. The default operations
 * then enforce required roles and permissions and report denied access through
 * an authorization exception.
 */
public interface Identity {

    /**
     * Resolves the principal associated with an HTTP request.
     *
     * @param request HTTP request to authenticate
     * @return authenticated principal
     */
    Principal requirePrincipal(ServerRequest request);

    /**
     * Requires the request principal to have the specified permission.
     *
     * @param request    HTTP request whose principal is checked
     * @param permission required permission
     * @throws AuthorizationException if the principal lacks the permission
     * @throws NullPointerException   if request or permission is null
     */
    default void requirePermission(ServerRequest request, Permission permission) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(permission, "Permission must not be null");
        requirePermission(requirePrincipal(request), permission);
    }

    /**
     * Requires a principal to have the specified permission.
     *
     * @param principal  principal to check
     * @param permission required permission
     * @throws AuthorizationException if the principal lacks the permission
     * @throws NullPointerException   if principal or permission is null
     */
    default void requirePermission(Principal principal, Permission permission) {
        Objects.requireNonNull(principal, "Principal must not be null");
        Objects.requireNonNull(permission, "Permission must not be null");
        if (!principal.hasPermission(permission)) {
            throw new AuthorizationException();
        }
    }

    /**
     * Requires the request principal to have the specified role.
     *
     * @param request HTTP request whose principal is checked
     * @param role    required role
     * @throws AuthorizationException if the principal lacks the role
     * @throws NullPointerException   if request or role is null
     */
    default void requireRole(ServerRequest request, Role role) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(role, "Role must not be null");
        requireRole(requirePrincipal(request), role);
    }

    /**
     * Requires a principal to have the specified role.
     *
     * @param principal principal to check
     * @param role      required role
     * @throws AuthorizationException if the principal lacks the role
     * @throws NullPointerException   if principal or role is null
     */
    default void requireRole(Principal principal, Role role) {
        Objects.requireNonNull(principal, "Principal must not be null");
        Objects.requireNonNull(role, "Role must not be null");
        if (!principal.hasRole(role)) {
            throw new AuthorizationException();
        }
    }
}
