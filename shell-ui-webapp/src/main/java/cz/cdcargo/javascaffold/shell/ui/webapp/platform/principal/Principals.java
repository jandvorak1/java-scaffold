package cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal;

import java.util.Set;

/**
 * Provides immutable predefined principals used by trusted application modes.
 */
public final class Principals {

    /**
     * Subject identifier of the local application principal.
     */
    public static final String LOCAL_SUBJECT = "local";

    private static final Principal LOCAL = new Principal(LOCAL_SUBJECT,
            Set.of(Role.ADMIN, Role.USER, Role.GUEST),
            Set.of(Permission.WRITE, Permission.READ));

    private Principals() {
    }

    /**
     * Returns the shared principal representing the trusted local application.
     *
     * @return immutable local principal with all application roles and permissions
     */
    public static Principal local() {
        return LOCAL;
    }
}
