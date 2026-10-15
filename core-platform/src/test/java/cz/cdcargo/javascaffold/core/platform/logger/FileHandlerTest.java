package cz.cdcargo.javascaffold.core.platform.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void storePreferencesPath() {
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
    void testPublishWritesFormattedRecord() throws Exception {
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
    void testConstructorUsesDefaultLevel() {
        // Arrange + Act
        var handler = new FileHandler(createPreferencesStore());

        // Assert
        assertEquals(Level.INFO, handler.getLevel());
    }

    @Test
    void testConstructorUsesConfiguredLevel() {
        // Arrange
        var preferences = createPreferencesStore();
        preferences.putLevel("logs.file.level", Level.FINE);

        // Act
        var handler = new FileHandler(preferences);

        // Assert
        assertEquals(Level.FINE, handler.getLevel());
    }

    @Test
    void testConstructorUsesConfiguredAliasLevel() {
        // Arrange
        var preferences = createPreferencesStore();
        preferences.put("logs.file.level", "ERROR");

        // Act
        var handler = new FileHandler(preferences);

        // Assert
        assertEquals(Level.SEVERE, handler.getLevel());
    }

    @Test
    void testDefaultConstructorUsesUserPreferences() {
        // Arrange
        var preferencesDirectory = tempDir.resolve("user");
        FilePreferencesStore.at(preferencesDirectory).putLevel("logs.file.level", Level.FINE);
        System.setProperty("prefs.path", preferencesDirectory.toString());

        // Act
        var handler = new FileHandler();

        // Assert
        assertEquals(Level.FINE, handler.getLevel());
    }

    @Test
    void testConstructorWithPreferencesRejectsNullPreferences() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new FileHandler(null));
    }

    @Test
    void testPublishIgnoresNullRecord() {
        // Arrange
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);

        // Act
        var handler = new FileHandler(preferences);
        handler.publish(null);

        // Assert
        assertFalse(Files.exists(logFile));
    }

    @Test
    void testPublishUsesAppLogForConfiguredDirectory() throws Exception {
        // Arrange
        var directory = tempDir.resolve("logs");
        Files.createDirectories(directory);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", directory);

        // Act
        var handler = new FileHandler(preferences);
        handler.publish(new LogRecord(Level.INFO, "Test message"));

        // Assert
        assertTrue(Files.exists(directory.resolve(APP_LOG)));
    }

    @Test
    void testPublishIgnoresRecordBelowConfiguredLevel() {
        // Arrange
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        preferences.putLevel("logs.file.level", Level.INFO);
        var record = new LogRecord(Level.FINE, "Test message below level");
        record.setLoggerName("Test.Logger");

        // Act
        var handler = new FileHandler(preferences);
        handler.publish(record);

        // Assert
        assertFalse(Files.exists(logFile));
    }

    @Test
    void testPublishRotatesFileAtConfiguredSize() throws Exception {
        // Arrange
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        preferences.putInt("logs.file.size", 10);
        preferences.putInt("logs.file.rotate", 2);
        Files.writeString(logFile, "12345678901234567890");
        var record = new LogRecord(Level.INFO, "Test message after rotation");
        record.setLoggerName("Test.Logger");

        // Act
        var handler = new FileHandler(preferences);
        handler.publish(record);
        var current = Files.readString(logFile);
        var rotated = Files.readString(tempDir.resolve(APP_LOG + ".1"));

        // Assert
        assertTrue(current.contains("Test message after rotation"));
        assertFalse(rotated.isBlank());
        assertTrue(Files.exists(logFile));
        assertTrue(Files.exists(tempDir.resolve(APP_LOG + ".1")));
    }

    @Test
    void testPublishRotatesFileBeforeRecordExceedsConfiguredSize() throws Exception {
        // Arrange
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        preferences.putInt("logs.file.size", 100);
        preferences.putInt("logs.file.rotate", 1);
        Files.writeString(logFile, "1234567890".repeat(9));

        // Act
        var handler = new FileHandler(preferences);
        handler.publish(new LogRecord(Level.INFO, "Test message"));

        // Assert
        assertTrue(Files.exists(tempDir.resolve(APP_LOG + ".1")));
        assertTrue(Files.readString(logFile).contains("Test message"));
    }

    @Test
    void testPublishTruncatesLogWhenRotationIsDisabled() throws Exception {
        // Arrange
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        preferences.putInt("logs.file.size", 10);
        preferences.putInt("logs.file.rotate", 0);
        Files.writeString(logFile, "12345678901234567890");
        var record = new LogRecord(Level.INFO, "Current message");

        // Act
        var handler = new FileHandler(preferences);
        handler.publish(record);

        // Assert
        assertTrue(Files.readString(logFile).contains("Current message"));
        assertFalse(Files.exists(tempDir.resolve(APP_LOG + ".1")));
    }

    @Test
    void testCloseIsIdempotent() {
        // Arrange
        var handler = new FileHandler(createPreferencesStore());

        // Act
        handler.close();
        handler.close();

        // Assert
        // No exception should be thrown
    }

    @Test
    void testPublishIgnoresRecordsAfterClose() {
        // Arrange
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        var handler = new FileHandler(preferences);
        handler.setFilter(record -> {
            throw new AssertionError("Filter must not be invoked after close");
        });

        // Act
        handler.close();
        handler.publish(new LogRecord(Level.INFO, "Ignored message"));

        // Assert
        assertFalse(Files.exists(logFile));
    }

    private FilePreferencesStore createPreferencesStore() {
        // Arrange + Act + Assert
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
