package cz.cdcargo.javascaffold.core.platform.logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
@ResourceLock(Resources.SYSTEM_OUT)
class ConsoleHandlerTest {

    @TempDir
    Path tempDir;

    private PrintStream out;
    private ByteArrayOutputStream content;
    private String originalPreferencesPath;

    @BeforeEach
    void setUp() throws Exception {
        out = System.out;
        originalPreferencesPath = System.getProperty("prefs.path");
        content = new ByteArrayOutputStream();
        System.setOut(new PrintStream(content, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(out);
        if (originalPreferencesPath == null) {
            System.clearProperty("prefs.path");
        } else {
            System.setProperty("prefs.path", originalPreferencesPath);
        }
    }

    @Test
    void testConstructor() {
        // Arrange
        var handler = new ConsoleHandler(createPreferencesStore());

        // Act + Assert
        assertEquals(Level.INFO, handler.getLevel());
    }

    @Test
    void testConstructorNull() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new ConsoleHandler(null));
    }

    @Test
    void testPublish() {
        // Arrange
        var record = new LogRecord(Level.INFO, "Test message");
        record.setLoggerName("Test.Logger");
        var handler = new ConsoleHandler(createPreferencesStore());
        handler.publish(record);

        // Act
        var output = content.toString(StandardCharsets.UTF_8);

        // Assert
        assertTrue(output.contains("Test message"));
        assertTrue(output.contains("Test.Logger"));
        assertTrue(output.contains("[INFO]"));
    }

    @Test
    void testFlush() {
        // Arrange
        var handler = new ConsoleHandler(createPreferencesStore());

        // Act + Assert
        assertDoesNotThrow(handler::flush);
    }

    @Test
    void testClose() {
        // Arrange
        var handler = new ConsoleHandler(createPreferencesStore());

        // Act + Assert
        assertDoesNotThrow(handler::close);
        assertDoesNotThrow(handler::close);
    }

    private FilePreferencesStore createPreferencesStore() {
        // Arrange + Act + Assert
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
