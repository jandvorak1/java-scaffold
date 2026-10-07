package cz.cdcargo.javascaffold.core.platform.logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import cz.cdcargo.javascaffold.core.platform.build.BuildMetadata;
import cz.cdcargo.javascaffold.core.platform.naming.Texts;
import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;

/**
 * Publishes formatted log records to a UTF-8 file and rotates the file at its
 * configured size limit.
 *
 * The configured level may use Java logging level names or application aliases.
 * Unless configured otherwise, app.log and its rotated files are placed in the
 * platform application state directory. Publishing is serialized for each
 * handler instance, and records are ignored after the handler is closed.
 */
public final class FileHandler extends Handler {

    private static final String STORE_DIRECTORY = BuildMetadata.slug();
    private static final String APP_LOG = "app.log";
    private static final String LEVEL_KEY = "logs.file.level";
    private static final String LEVEL_ENVIRONMENT_VARIABLE = BuildMetadata.envPrefix() + "_LOGS_FILE_LEVEL";
    private static final String LEVEL_SYSTEM_PROPERTY = "logs.file.level";
    private static final String PATH_KEY = "logs.file.path";
    private static final String PATH_ENVIRONMENT_VARIABLE = BuildMetadata.envPrefix() + "_LOGS_FILE_PATH";
    private static final String PATH_SYSTEM_PROPERTY = "logs.file.path";
    private static final String SIZE_KEY = "logs.file.size";
    private static final String SIZE_ENVIRONMENT_VARIABLE = BuildMetadata.envPrefix() + "_LOGS_FILE_SIZE";
    private static final String SIZE_SYSTEM_PROPERTY = "logs.file.size";
    private static final String ROTATE_KEY = "logs.file.rotate";
    private static final String ROTATE_ENVIRONMENT_VARIABLE = BuildMetadata.envPrefix() + "_LOGS_FILE_ROTATE";
    private static final String ROTATE_SYSTEM_PROPERTY = "logs.file.rotate";
    private static final int DEFAULT_MAX_FILE_SIZE = 1_024_000;
    private static final int DEFAULT_ROTATE_COUNT = 5;

    private final ReentrantLock lock = new ReentrantLock();
    private final PreferencesStore preferences;
    private volatile boolean closed;

    /**
     * Creates a file handler using the production preference store.
     */
    public FileHandler() {
        this(FilePreferencesStore.root());
    }

    /**
     * Creates a file handler using configuration from the supplied preference
     * store.
     *
     * @param preferences preference store that supplies the logging configuration
     * @throws NullPointerException when preferences is null
     */
    public FileHandler(PreferencesStore preferences) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences must not be null");
        setFormatter(new LogFormatter());
        setLevel(resolveLevel());
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
            var path = resolveLogsFilePath();
            ensureParentDirectory(path);
            var bytes = getFormatter().format(record).getBytes(StandardCharsets.UTF_8);
            rotateIfNeeded(path, resolveMaxRotateCount(), resolveMaxFileSize(), bytes.length);
            try (var channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE)) {
                var buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
            }
        } catch (ClosedByInterruptException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (ClosedChannelException e) {
            if (!closed) {
                reportError("Error while publishing log to file", e, ErrorManager.WRITE_FAILURE);
            }
        } catch (Exception e) {
            if (!closed) {
                reportError("Error while publishing log to file", e, ErrorManager.WRITE_FAILURE);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void flush() {
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

    private Level resolveLevel() {
        var value = preferences.get(LEVEL_KEY, null, LEVEL_ENVIRONMENT_VARIABLE, LEVEL_SYSTEM_PROPERTY);
        return LogLevels.parse(value, Level.INFO);
    }

    private int resolveMaxFileSize() {
        var value = preferences.getInt(SIZE_KEY, DEFAULT_MAX_FILE_SIZE, SIZE_ENVIRONMENT_VARIABLE,
                SIZE_SYSTEM_PROPERTY);
        return value > 0 ? value : DEFAULT_MAX_FILE_SIZE;
    }

    private int resolveMaxRotateCount() {
        var value = preferences.getInt(ROTATE_KEY, DEFAULT_ROTATE_COUNT, ROTATE_ENVIRONMENT_VARIABLE,
                ROTATE_SYSTEM_PROPERTY);
        return value >= 0 ? value : DEFAULT_ROTATE_COUNT;
    }

    private Path resolveLogsFilePath() {
        var override = preferences.get(PATH_KEY, null, PATH_ENVIRONMENT_VARIABLE, PATH_SYSTEM_PROPERTY);
        if (override == null) {
            return resolveDefaultLogsFilePath();
        }
        var normalized = override.trim();
        if (normalized.isEmpty()) {
            return resolveDefaultLogsFilePath();
        }
        var overridePath = Path.of(normalized);
        if (Files.isDirectory(overridePath) || normalized.endsWith("/") || normalized.endsWith("\\")) {
            return overridePath.resolve(APP_LOG);
        }
        return overridePath;
    }

    /**
     * Resolves the platform-specific default location of the application log file.
     *
     * @return path to app.log in the application state directory
     */
    static Path resolveDefaultLogsFilePath() {
        var os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        var base = switch (os) {
            case String s when s.contains("win") -> {
                var localAppData = System.getenv("LOCALAPPDATA");
                if (localAppData == null || localAppData.isBlank()) {
                    localAppData = System.getProperty("user.home");
                }
                yield Paths.get(localAppData, Texts.toPascalCase(STORE_DIRECTORY));
            }
            case String s when s.contains("mac") ->
                Paths.get(System.getProperty("user.home"), "Library", "Logs", Texts.toPascalCase(STORE_DIRECTORY));
            default -> {
                var xdg = System.getenv("XDG_STATE_HOME");
                var stateDirectory = xdg != null && !xdg.isBlank()
                        ? Paths.get(xdg)
                        : Paths.get(System.getProperty("user.home"), ".local", "state");
                yield stateDirectory.resolve(STORE_DIRECTORY);
            }
        };
        return base.resolve(APP_LOG);
    }

    private void ensureParentDirectory(Path file) throws IOException {
        var parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private void rotateIfNeeded(Path logFile, int maxRotateCount, int maxSizeBytes, int nextRecordSize)
            throws IOException {
        var size = Files.exists(logFile) ? Files.size(logFile) : 0L;
        if (size + nextRecordSize <= maxSizeBytes) {
            return;
        }
        rotateFiles(maxRotateCount, logFile);
    }

    private void rotateFiles(int maxRotateCount, Path logFile) throws IOException {
        if (maxRotateCount <= 0) {
            Files.deleteIfExists(logFile);
            return;
        }
        Files.deleteIfExists(sibling(logFile, "." + maxRotateCount));
        for (var i = maxRotateCount - 1; i >= 1; i--) {
            var source = sibling(logFile, "." + i);
            if (Files.exists(source)) {
                var destination = sibling(logFile, "." + (i + 1));
                safeMove(source, destination);
            }
        }
        var first = sibling(logFile, ".1");
        if (Files.exists(logFile) && Files.size(logFile) > 0) {
            safeMove(logFile, first);
        } else {
            Files.deleteIfExists(first);
        }
    }

    private Path sibling(Path file, String suffix) {
        return file.resolveSibling(file.getFileName().toString() + suffix);
    }

    private void safeMove(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            // Fall back when atomic moves are not supported by the file system.
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
