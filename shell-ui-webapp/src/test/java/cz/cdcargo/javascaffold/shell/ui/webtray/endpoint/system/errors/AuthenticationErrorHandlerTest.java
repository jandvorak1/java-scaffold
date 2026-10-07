package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

class AuthenticationErrorHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testAuthenticationErrorHandlerRejectsNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new AuthenticationErrorHandler(null, null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testAuthenticationErrorHandlerRejectsNullWriter() {
        var exception = assertThrows(NullPointerException.class,
                () -> new AuthenticationErrorHandler(createPreferences(), null));

        assertEquals("Error response writer must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var exception = assertThrows(NullPointerException.class,
                () -> createHandler(createPreferences()).execute(null, null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    private AuthenticationErrorHandler createHandler(FilePreferencesStore preferences) {
        var writer = new ErrorResponseWriter(new ResponseWriter(new WebSecurity("http://localhost")));
        return new AuthenticationErrorHandler(preferences, writer);
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
