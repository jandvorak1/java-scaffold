package cz.cdcargo.javascaffold.core.platform.logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ErrorManager;
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
    void testPublishWritesFormattedLogRecord() {
        var record = new LogRecord(Level.INFO, "Test message");
        record.setLoggerName("Test.Logger");
        var handler = new ConsoleHandler(createPreferencesStore());
        handler.publish(record);
        var output = content.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Test message"));
        assertTrue(output.contains("Test.Logger"));
        assertTrue(output.contains("[INFO]"));
    }

    @Test
    void testConstructorUsesDefaultLevel() {
        var handler = new ConsoleHandler(createPreferencesStore());

        assertEquals(Level.INFO, handler.getLevel());
    }

    @Test
    void testConstructorUsesConfiguredLevel() {
        var preferences = createPreferencesStore();
        preferences.putLevel("logs.console.level", Level.FINE);

        var handler = new ConsoleHandler(preferences);

        assertEquals(Level.FINE, handler.getLevel());
    }

    @Test
    void testConstructorUsesConfiguredAliasLevel() {
        var preferences = createPreferencesStore();
        preferences.put("logs.console.level", "DEBUG");

        var handler = new ConsoleHandler(preferences);

        assertEquals(Level.FINE, handler.getLevel());
    }

    @Test
    void testDefaultConstructorUsesUserPreferences() {
        var preferencesDirectory = tempDir.resolve("preferences");
        FilePreferencesStore.at(preferencesDirectory).putLevel("logs.console.level", Level.FINE);
        System.setProperty("prefs.path", preferencesDirectory.toString());

        var handler = new ConsoleHandler();

        assertEquals(Level.FINE, handler.getLevel());
    }

    @Test
    void testConstructorRejectsNullPreferences() {
        assertThrows(NullPointerException.class, () -> new ConsoleHandler(null));
    }

    @Test
    void testPublishIgnoresNullRecord() {
        var handler = new ConsoleHandler(createPreferencesStore());
        handler.publish(null);
        var output = content.toString(StandardCharsets.UTF_8);

        assertTrue(output.isEmpty());
    }

    @Test
    void testPublishIgnoresRecordBelowConfiguredLevel() {
        var preferences = createPreferencesStore();
        preferences.putLevel("logs.console.level", Level.INFO);
        var record = new LogRecord(Level.FINE, "Test message");
        record.setLoggerName("Test.Logger");
        var handler = new ConsoleHandler(preferences);
        handler.publish(record);
        var output = content.toString(StandardCharsets.UTF_8);

        assertFalse(output.contains("Test message"));
    }

    @Test
    void testPublishReportsOutputFailure() {
        var failures = new AtomicInteger();
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw new IOException("Output failure");
            }
        }, true, StandardCharsets.UTF_8));
        var handler = new ConsoleHandler(createPreferencesStore());
        handler.setErrorManager(new ErrorManager() {
            @Override
            public void error(String message, Exception exception, int code) {
                failures.incrementAndGet();
            }
        });

        handler.publish(new LogRecord(Level.INFO, "Test message"));

        assertEquals(1, failures.get());
    }

    @Test
    void testFlushDoesNotFail() {
        var handler = new ConsoleHandler(createPreferencesStore());

        assertDoesNotThrow(handler::flush);
    }

    @Test
    void testCloseIsIdempotent() {
        var handler = new ConsoleHandler(createPreferencesStore());

        assertDoesNotThrow(handler::close);
        assertDoesNotThrow(handler::close);
    }

    @Test
    void testClosePreventsFurtherPublishing() {
        var handler = new ConsoleHandler(createPreferencesStore());
        var record = new LogRecord(Level.INFO, "Ignored message");

        handler.close();
        handler.publish(record);

        assertTrue(content.toString(StandardCharsets.UTF_8).isEmpty());
    }

    private FilePreferencesStore createPreferencesStore() {
        return FilePreferencesStore.at(tempDir.resolve("preferences"));
    }
}
