package cz.cdcargo.javascaffold.core.platform.logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(Resources.SYSTEM_PROPERTIES)
class FileHandlerTest {

    private static final String APP_LOG = "app.log";

    @TempDir
    Path tempDir;

    private String originalPreferencesPath;

    @BeforeEach
    void setUp() {
        originalPreferencesPath = System.getProperty("prefs.path");
    }

    @AfterEach
    void tearDown() {
        if (originalPreferencesPath == null) {
            System.clearProperty("prefs.path");
        } else {
            System.setProperty("prefs.path", originalPreferencesPath);
        }
    }

    @Test
    void testConstructor() {
        // Arrange + Act
        var handler = new FileHandler(createPreferencesStore());

        // Assert
        assertEquals(Level.INFO, handler.getLevel());
    }

    @Test
    void testConstructorNull() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new FileHandler(null));
    }

    @Test
    void testPublish() throws Exception {
        // Arrange
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        var record = new LogRecord(Level.INFO, "Test message");
        record.setLoggerName("Test.Logger");

        // Act
        var handler = new FileHandler(preferences);
        handler.publish(record);

        // Assert
        assertTrue(Files.exists(logFile));
        var content = Files.readString(logFile);
        assertTrue(content.contains("Test message"));
        assertTrue(content.contains("Test.Logger"));
        assertTrue(content.contains("[INFO]"));
    }

    @Test
    void testClose() {
        // Arrange
        var handler = new FileHandler(createPreferencesStore());

        // Act + Assert
        assertDoesNotThrow(handler::close);
        assertDoesNotThrow(handler::close);
    }

    private FilePreferencesStore createPreferencesStore() {
        // Arrange + Act + Assert
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
