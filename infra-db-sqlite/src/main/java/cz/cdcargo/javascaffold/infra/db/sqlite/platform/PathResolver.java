package cz.cdcargo.javascaffold.infra.db.sqlite.platform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import cz.cdcargo.javascaffold.core.platform.build.BuildMetadata;
import cz.cdcargo.javascaffold.core.platform.naming.Texts;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;

/**
 * Resolves the filesystem locations used by SQLite infrastructure.
 *
 * Environment variables and system properties can override stored values. An
 * operating-system-specific location is used when no value is configured.
 */
public final class PathResolver {

    private static final String DATABASE_DIRECTORY = "sqlite";
    private static final String DATABASE_FILE = "app.db";
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final AtomicLong LAST_BACKUP_TIMESTAMP = new AtomicLong();
    private final PreferencesStore preferences;

    /**
     * Creates a resolver that reads SQLite locations from preferences.
     *
     * @param preferences source of location settings
     * @throws NullPointerException if preferences is null
     */
    public PathResolver(PreferencesStore preferences) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences must not be null");
    }

    /**
     * Resolves the directory that contains the persistent database.
     *
     * @return configured directory or the operating-system-specific default
     */
    public Path resolveDatabasePath() {
        return preferences.getPath("sqlite.database.path", resolveDefaultDatabasePath(),
                BuildMetadata.slug() + "_SQLITE_DATABASE_PATH", "sqlite.database.path");
    }

    /**
     * Resolves the persistent database file.
     *
     * @return database directory with the standard database filename
     */
    public Path resolveDatabaseFile() {
        return resolveDatabasePath().resolve(DATABASE_FILE);
    }

    /**
     * Resolves the directory that stores database backups.
     *
     * @return configured directory or the operating-system-specific default
     */
    public Path resolveBackupPath() {
        return preferences.getPath("sqlite.backup.path", resolveDefaultBackupPath(),
                BuildMetadata.slug() + "_SQLITE_BACKUP_PATH", "sqlite.backup.path");
    }

    /**
     * Resolves a new backup filename containing a unique timestamp.
     *
     * Names remain unique when several paths are resolved during the same
     * millisecond.
     *
     * @return path of a new backup file
     */
    public Path resolveBackupFile() {
        var currentTimestamp = System.currentTimeMillis();
        var uniqueTimestamp = LAST_BACKUP_TIMESTAMP.updateAndGet(
                previousTimestamp -> Math.max(currentTimestamp, previousTimestamp + 1));
        var dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(uniqueTimestamp), ZoneId.systemDefault());
        return resolveBackupPath().resolve("backup_" + dateTime.format(BACKUP_TIMESTAMP) + ".db");
    }

    private static Path resolveDefaultBackupPath() {
        var os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return switch (os) {
            case String s when s.contains("win") -> {
                var appData = System.getenv("APPDATA");
                if (appData == null || appData.isBlank()) {
                    appData = System.getProperty("user.home");
                }
                yield Paths.get(appData, Texts.toPascalCase(BuildMetadata.slug()), DATABASE_DIRECTORY);
            }
            case String s when s.contains("mac") ->
                Paths.get(System.getProperty("user.home"), "Library", "Application Support",
                        Texts.toPascalCase(BuildMetadata.slug()), DATABASE_DIRECTORY);
            default -> {
                var xdg = System.getenv("XDG_DATA_HOME");
                var base = xdg != null && !xdg.isBlank()
                        ? Paths.get(xdg)
                        : Paths.get(System.getProperty("user.home"), ".local", "share");
                yield base.resolve(BuildMetadata.slug()).resolve(DATABASE_DIRECTORY);
            }
        };
    }

    private static Path resolveDefaultDatabasePath() {
        return resolveDefaultBackupPath();
    }
}
