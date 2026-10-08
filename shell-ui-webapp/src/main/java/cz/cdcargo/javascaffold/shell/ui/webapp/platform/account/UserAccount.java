package cz.cdcargo.javascaffold.shell.ui.webapp.platform.account;

import java.util.Objects;
import java.util.Set;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Permission;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Role;

public record UserAccount(String subject, String email, boolean active, Set<Role> roles, Set<Permission> permissions) {

    public UserAccount {
        Objects.requireNonNull(subject, "Subject must not be null");
        Objects.requireNonNull(email, "Email must not be null");

        if (subject.isBlank()) {
            throw new IllegalArgumentException("Subject must not be blank");
        }
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public Principal principal() {
        return new Principal(subject, roles, permissions);
    }

}
