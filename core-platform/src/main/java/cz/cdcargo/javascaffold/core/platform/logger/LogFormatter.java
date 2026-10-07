package cz.cdcargo.javascaffold.core.platform.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Formats log records as single text entries.
 *
 * Each entry contains a local timestamp, normalized severity, logger name, and
 * localized message. A thrown exception is appended as a stack trace. Entries
 * always end with the platform line separator.
 */
public final class LogFormatter extends Formatter {

    private static final String UNKNOWN_LOGGER_NAME = "Unknown";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.systemDefault());

    @Override
    public String format(LogRecord record) {
        Objects.requireNonNull(record, "Record must not be null");
        var timestamp = TIMESTAMP_FORMATTER.format(record.getInstant());
        var level = mapLevel(record.getLevel());
        var loggerName = resolveLoggerName(record);
        var line = new StringBuilder().append(timestamp).append(" [").append(level).append("] ").append(loggerName)
                .append(" - ").append(formatMessage(record));
        var thrown = record.getThrown();
        if (thrown != null) {
            var writer = new StringWriter();
            thrown.printStackTrace(new PrintWriter(writer));
            line.append(System.lineSeparator()).append(writer.toString().stripTrailing());
        }
        return line.append(System.lineSeparator()).toString();
    }

    private static String mapLevel(Level level) {
        Objects.requireNonNull(level, "Level must not be null");
        var value = level.intValue();
        if (value >= Level.SEVERE.intValue()) {
            return "ERROR";
        }
        if (value >= Level.WARNING.intValue()) {
            return "WARN";
        }
        if (value >= Level.INFO.intValue()) {
            return "INFO";
        }
        if (value >= Level.FINE.intValue()) {
            return "DEBUG";
        }
        return "TRACE";
    }

    private static String resolveLoggerName(LogRecord record) {
        var loggerName = record.getLoggerName();
        return loggerName == null || loggerName.isBlank()
                ? UNKNOWN_LOGGER_NAME
                : loggerName.strip();
    }
}
