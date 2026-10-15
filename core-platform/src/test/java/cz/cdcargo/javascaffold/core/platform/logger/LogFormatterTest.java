package cz.cdcargo.javascaffold.core.platform.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogFormatterTest {

    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private Instant timestamp;
    private String formattedTimestamp;
    private LogFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new LogFormatter();
        timestamp = Instant.ofEpochMilli(Instant.parse("2024-01-02T03:04:05.678Z").toEpochMilli());
        formattedTimestamp = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).withZone(ZoneId.systemDefault())
                .format(timestamp);
    }

    @Test
    void testFormatCreatesCompleteLogEntry() {
        // Arrange
        var record = new LogRecord(Level.INFO, "Test message");
        record.setLoggerName("Test.Logger");
        record.setInstant(timestamp);

        // Act
        var result = formatter.format(record);

        // Assert
        assertEquals(formattedTimestamp + " [INFO] Test.Logger - Test message"
                + System.lineSeparator(), result);
    }

    @Test
    void testFormatRejectsNullRecord() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> formatter.format(null));
    }

    @Test
    void testFormatUsesUnknownForMissingLoggerName() {
        // Arrange
        var missingName = new LogRecord(Level.INFO, "Missing");
        missingName.setLoggerName(null);
        missingName.setInstant(timestamp);

        // Act
        var result = formatter.format(missingName);

        // Assert
        assertTrue(result.contains(" [INFO] Unknown - Missing"));
    }

    @Test
    void testFormatUsesUnknownForBlankLoggerName() {
        // Arrange
        var blankName = new LogRecord(Level.INFO, "Blank");
        blankName.setLoggerName(" \t");
        blankName.setInstant(timestamp);

        // Act
        var result = formatter.format(blankName);

        // Assert
        assertTrue(result.contains(" [INFO] Unknown - Blank"));
    }

    @Test
    void testFormatTrimsLoggerName() {
        // Arrange
        var record = new LogRecord(Level.INFO, "Test message");
        record.setLoggerName(" Test.Logger ");
        record.setInstant(timestamp);

        // Act
        var result = formatter.format(record);

        // Assert
        assertTrue(result.contains(" [INFO] Test.Logger - Test message"));
    }

    @Test
    void testFormatNormalizesLogLevels() {
        // Arrange
        var expectedLevels = Map.of(Level.SEVERE, "ERROR", Level.WARNING, "WARN", Level.INFO, "INFO", Level.FINE,
                "DEBUG", Level.FINEST, "TRACE");
        expectedLevels.forEach((level, expected) -> {
            var record = new LogRecord(level, "Message");
            record.setLoggerName("Logger");
            record.setInstant(timestamp);

            // Act
            var result = formatter.format(record);

            // Assert
            assertTrue(result.contains(" [" + expected + "] "));
        });
    }

    @Test
    void testFormatInterpolatesMessageParameters() {
        // Arrange
        var record = new LogRecord(Level.SEVERE, "Hello {0}, count={1}");
        record.setLoggerName("Test.Logger");
        record.setInstant(timestamp);
        record.setParameters(new Object[] { "World", 42 });

        // Act
        var result = formatter.format(record);

        // Assert
        assertTrue(result.contains(" [ERROR] Test.Logger - Hello World, count=42"));
    }

    @Test
    void testFormatAppendsThrowableStackTrace() {
        // Arrange
        var throwable = new IllegalStateException("Something went wrong");
        var record = new LogRecord(Level.SEVERE, "Test message");
        record.setLoggerName("Test.Logger");
        record.setInstant(timestamp);
        record.setThrown(throwable);

        // Act
        var result = formatter.format(record);

        // Assert
        assertTrue(result.contains(" [ERROR] Test.Logger - Test message"));
        assertTrue(result.contains("IllegalStateException"));
        assertTrue(result.contains("Something went wrong"));
        assertTrue(result.endsWith(System.lineSeparator()));
    }
}
