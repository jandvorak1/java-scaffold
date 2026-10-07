package cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal;

/**
 * Defines permissions controlling access to application operations.
 *
 * Each permission exposes a stable lowercase identifier suitable for
 * persistence and external representations.
 */
public enum Permission {
    /** Permission required for write operations. */
    WRITE("write"),
    /** Permission required for read operations. */
    READ("read");

    private final String id;

    Permission(String id) {
        this.id = id;
    }

    /**
     * Returns the stable identifier of this permission.
     *
     * @return permission identifier
     */
    public String id() {
        return id;
    }
}
