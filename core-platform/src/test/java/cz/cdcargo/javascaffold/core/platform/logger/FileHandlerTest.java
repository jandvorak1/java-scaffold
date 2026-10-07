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
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);

        var record = new LogRecord(Level.INFO, "Test message");
        record.setLoggerName("Test.Logger");

        var handler = new FileHandler(preferences);
        handler.publish(record);
        assertTrue(Files.exists(logFile));

        var content = Files.readString(logFile);
        assertTrue(content.contains("Test message"));
        assertTrue(content.contains("Test.Logger"));
        assertTrue(content.contains("[INFO]"));
    }

    @Test
    void testConstructorUsesDefaultLevel() {
        var handler = new FileHandler(createPreferencesStore());

        assertEquals(Level.INFO, handler.getLevel());
    }

    @Test
    void testConstructorUsesConfiguredLevel() {
        var preferences = createPreferencesStore();
        preferences.putLevel("logs.file.level", Level.FINE);

        var handler = new FileHandler(preferences);

        assertEquals(Level.FINE, handler.getLevel());
    }

    @Test
    void testConstructorUsesConfiguredAliasLevel() {
        var preferences = createPreferencesStore();
        preferences.put("logs.file.level", "ERROR");

        var handler = new FileHandler(preferences);

        assertEquals(Level.SEVERE, handler.getLevel());
    }

    @Test
    void testDefaultConstructorUsesUserPreferences() {
        var preferencesDirectory = tempDir.resolve("user");
        FilePreferencesStore.at(preferencesDirectory).putLevel("logs.file.level", Level.FINE);
        System.setProperty("prefs.path", preferencesDirectory.toString());

        var handler = new FileHandler();

        assertEquals(Level.FINE, handler.getLevel());
    }

    @Test
    void testConstructorWithPreferencesRejectsNullPreferences() {
        assertThrows(NullPointerException.class, () -> new FileHandler(null));
    }

    @Test
    void testPublishIgnoresNullRecord() {
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);

        var handler = new FileHandler(preferences);
        handler.publish(null);

        assertFalse(Files.exists(logFile));
    }

    @Test
    void testPublishUsesAppLogForConfiguredDirectory() throws Exception {
        var directory = tempDir.resolve("logs");
        Files.createDirectories(directory);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", directory);

        var handler = new FileHandler(preferences);
        handler.publish(new LogRecord(Level.INFO, "Test message"));

        assertTrue(Files.exists(directory.resolve(APP_LOG)));
    }

    @Test
    void testPublishIgnoresRecordBelowConfiguredLevel() {
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        preferences.putLevel("logs.file.level", Level.INFO);

        var record = new LogRecord(Level.FINE, "Test message below level");
        record.setLoggerName("Test.Logger");

        var handler = new FileHandler(preferences);
        handler.publish(record);

        assertFalse(Files.exists(logFile));
    }

    @Test
    void testPublishRotatesFileAtConfiguredSize() throws Exception {
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        preferences.putInt("logs.file.size", 10);
        preferences.putInt("logs.file.rotate", 2);
        Files.writeString(logFile, "12345678901234567890");

        var record = new LogRecord(Level.INFO, "Test message after rotation");
        record.setLoggerName("Test.Logger");

        var handler = new FileHandler(preferences);
        handler.publish(record);

        var current = Files.readString(logFile);
        var rotated = Files.readString(tempDir.resolve(APP_LOG + ".1"));

        assertTrue(current.contains("Test message after rotation"));
        assertFalse(rotated.isBlank());
        assertTrue(Files.exists(logFile));
        assertTrue(Files.exists(tempDir.resolve(APP_LOG + ".1")));
    }

    @Test
    void testPublishRotatesFileBeforeRecordExceedsConfiguredSize() throws Exception {
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        preferences.putInt("logs.file.size", 100);
        preferences.putInt("logs.file.rotate", 1);
        Files.writeString(logFile, "1234567890".repeat(9));

        var handler = new FileHandler(preferences);
        handler.publish(new LogRecord(Level.INFO, "Test message"));

        assertTrue(Files.exists(tempDir.resolve(APP_LOG + ".1")));
        assertTrue(Files.readString(logFile).contains("Test message"));
    }

    @Test
    void testPublishTruncatesLogWhenRotationIsDisabled() throws Exception {
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        preferences.putInt("logs.file.size", 10);
        preferences.putInt("logs.file.rotate", 0);
        Files.writeString(logFile, "12345678901234567890");
        var record = new LogRecord(Level.INFO, "Current message");

        var handler = new FileHandler(preferences);
        handler.publish(record);

        assertTrue(Files.readString(logFile).contains("Current message"));
        assertFalse(Files.exists(tempDir.resolve(APP_LOG + ".1")));
    }

    @Test
    void testCloseIsIdempotent() {
        var handler = new FileHandler(createPreferencesStore());

        handler.close();
        handler.close();
    }

    @Test
    void testPublishIgnoresRecordsAfterClose() {
        var logFile = tempDir.resolve(APP_LOG);
        var preferences = createPreferencesStore();
        preferences.putPath("logs.file.path", logFile);
        var handler = new FileHandler(preferences);
        handler.setFilter(record -> {
            throw new AssertionError("Filter must not be invoked after close");
        });

        handler.close();
        handler.publish(new LogRecord(Level.INFO, "Ignored message"));

        assertFalse(Files.exists(logFile));
    }

    private FilePreferencesStore createPreferencesStore() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
