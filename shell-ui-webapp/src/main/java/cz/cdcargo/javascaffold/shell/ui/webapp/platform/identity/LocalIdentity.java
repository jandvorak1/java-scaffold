package cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity;

import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principal;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.principal.Principals;
import io.helidon.webserver.http.ServerRequest;

/**
 * Resolves every request to the predefined local application principal.
 *
 * This implementation performs no credential lookup and is intended for the
 * trusted local application endpoint.
 */
public final class LocalIdentity implements Identity {

    @Override
    public Principal requirePrincipal(ServerRequest request) {
        return Principals.local();
    }
}
