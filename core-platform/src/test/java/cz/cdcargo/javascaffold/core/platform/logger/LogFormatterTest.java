package cz.cdcargo.javascaffold.core.platform.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    void testFormat() {
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
}
