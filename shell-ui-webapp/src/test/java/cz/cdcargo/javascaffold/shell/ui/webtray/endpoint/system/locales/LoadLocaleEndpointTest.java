package cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.locales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;

class LoadLocaleEndpointTest {

    @TempDir
    Path tempDir;

    @Test
    void testConstructorRejectsNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new LoadLocaleEndpoint(null, null));

        assertEquals("Preferences store must not be null", exception.getMessage());
    }

    @Test
    void testConstructorRejectsNullWriter() {
        var exception = assertThrows(NullPointerException.class,
                () -> new LoadLocaleEndpoint(createPreferences(), null));

        assertEquals("Response writer must not be null", exception.getMessage());
    }

    @Test
    void testExecuteRejectsNullRequest() {
        var exception = assertThrows(NullPointerException.class,
                () -> createEndpoint(createPreferences()).execute(null, null));

        assertEquals("Server request must not be null", exception.getMessage());
    }

    private LoadLocaleEndpoint createEndpoint(FilePreferencesStore preferences) {
        return new LoadLocaleEndpoint(preferences, new ResponseWriter(new WebSecurity("http://localhost")));
    }

    private FilePreferencesStore createPreferences() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
