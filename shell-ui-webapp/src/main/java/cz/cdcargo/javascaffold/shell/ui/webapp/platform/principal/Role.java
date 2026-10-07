package cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal;

/**
 * Defines application roles assignable to authenticated principals.
 *
 * Each role exposes a stable lowercase identifier suitable for persistence and
 * external representations.
 */
public enum Role {
    /** Administrator role with full application privileges. */
    ADMIN("admin"),
    /** Standard user role. */
    USER("user"),
    /** Guest role with limited privileges. */
    GUEST("guest");

    private final String id;

    Role(String id) {
        this.id = id;
    }

    /**
     * Returns the stable identifier of this role.
     *
     * @return role identifier
     */
    public String id() {
        return id;
    }
}
