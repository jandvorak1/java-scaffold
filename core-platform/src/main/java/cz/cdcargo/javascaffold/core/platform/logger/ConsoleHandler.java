package cz.cdcargo.javascaffold.core.platform.logger;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import cz.cdcargo.javascaffold.core.platform.build.BuildMetadata;
import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;

/**
 * Publishes formatted log records to the standard output stream.
 *
 * The configured level may use Java logging level names or application aliases.
 * Publishing and flushing are serialized for each handler instance. Records are
 * ignored after the handler is closed, and output failures are reported to the
 * configured error manager.
 */
public final class ConsoleHandler extends Handler {

    private static final String LEVEL_KEY = "logs.console.level";
    private static final String LEVEL_ENVIRONMENT_VARIABLE = BuildMetadata.envPrefix() + "_LOGS_CONSOLE_LEVEL";
    private static final String LEVEL_SYSTEM_PROPERTY = "logs.console.level";

    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean closed;

    /**
     * Creates a console handler using the production preference store.
     */
    public ConsoleHandler() {
        this(FilePreferencesStore.root());
    }

    /**
     * Creates a console handler using configuration from the supplied preference
     * store.
     *
     * @param preferences preference store that supplies the initial logging level
     * @throws NullPointerException when preferences is null
     */
    public ConsoleHandler(PreferencesStore preferences) {
        var validPreferences = Objects.requireNonNull(preferences, "Preferences must not be null");
        setFormatter(new LogFormatter());
        setLevel(resolveLevel(validPreferences));
    }

    @Override
    public void publish(LogRecord record) {
        if (closed || record == null || !isLoggable(record)) {
            return;
        }
        lock.lock();
        try {
            if (closed) {
                return;
            }
            var output = System.out;
            output.print(getFormatter().format(record));
            if (output.checkError()) {
                reportError("Error while publishing log to console", null, ErrorManager.WRITE_FAILURE);
            }
        } catch (Exception e) {
            if (!closed) {
                reportError("Error while publishing log to console", e, ErrorManager.WRITE_FAILURE);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void flush() {
        lock.lock();
        try {
            System.out.flush();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            if (closed) {
                return;
            }
            closed = true;
            flush();
        } finally {
            lock.unlock();
        }
    }

    private static Level resolveLevel(PreferencesStore preferences) {
        var value = preferences.get(LEVEL_KEY, null, LEVEL_ENVIRONMENT_VARIABLE, LEVEL_SYSTEM_PROPERTY);
        return LogLevels.parse(value, Level.INFO);
    }
}
