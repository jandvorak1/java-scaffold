package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

class AuthorizationErrorHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testAuthorizationErrorHandlerRejectsNullPreferences() {
        assertThrows(NullPointerException.class, () -> new AuthorizationErrorHandler(null, null));
    }

    @Test
    void testAuthorizationErrorHandlerRejectsNullWriter() {
        assertThrows(NullPointerException.class, () -> new AuthorizationErrorHandler(createPreferences(), null));
    }

    @Test
    void testExecuteRejectsNullRequest() {
        assertThrows(NullPointerException.class, () -> createHandler(createPreferences()).execute(null, null, null));
    }

    private AuthorizationErrorHandler createHandler(FilePreferencesStore preferences) {
        var writer = new ErrorResponseWriter(new ResponseWriter(new WebSecurity("http://localhost")));
        return new AuthorizationErrorHandler(preferences, writer);
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
