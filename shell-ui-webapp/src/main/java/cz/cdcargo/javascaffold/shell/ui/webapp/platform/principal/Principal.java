package cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal;

import java.util.Objects;
import java.util.Set;

/**
 * Represents an authenticated subject together with its assigned roles and
 * permissions.
 *
 * The subject must contain non-whitespace text. Role and permission collections
 * are defensively copied; null collections are normalized to immutable empty
 * sets.
 *
 * @param subject     unique subject identifier
 * @param roles       roles assigned to the subject; null is treated as an empty
 *                    set
 * @param permissions permissions assigned to the subject; null is treated as an
 *                    empty set
 */
public record Principal(String subject, Set<Role> roles, Set<Permission> permissions) {

    public Principal {
        Objects.requireNonNull(subject, "Subject must not be null");
        if (subject.isBlank()) {
            throw new IllegalArgumentException("Subject must not be blank");
        }
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    /**
     * Checks whether this principal has the specified role.
     *
     * @param role role to check
     * @return true if the principal has the role, otherwise false
     * @throws NullPointerException if role is null
     */
    public boolean hasRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");
        return roles.contains(role);
    }

    /**
     * Checks whether this principal has the specified permission.
     *
     * @param permission permission to check
     * @return true if the principal has the permission, otherwise false
     * @throws NullPointerException if permission is null
     */
    public boolean hasPermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission must not be null");
        return permissions.contains(permission);
    }
}
