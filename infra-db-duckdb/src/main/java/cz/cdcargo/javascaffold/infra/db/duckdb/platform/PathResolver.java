package cz.cdcargo.javascaffold.infra.db.duckdb.platform;

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
 * Resolves filesystem locations used by the DuckDB infrastructure.
 *
 * Stored preferences, environment variables, and system properties can
 * override the operating-system-specific defaults.
 */
public final class PathResolver {

    private static final String DATABASE_DIRECTORY = "duckdb";
    private static final String DATABASE_FILE = "app.db";
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final AtomicLong LAST_BACKUP_TIMESTAMP = new AtomicLong();
    private final PreferencesStore preferences;

    /**
     * Creates a resolver using the supplied location preferences.
     *
     * @param preferences source of location settings
     * @throws NullPointerException if preferences is null
     */
    public PathResolver(PreferencesStore preferences) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences must not be null");
    }

    /**
     * Resolves the directory containing the persistent database.
     *
     * @return configured directory or the operating-system-specific default
     */
    public Path resolveDatabasePath() {
        return preferences.getPath("duckdb.database.path", resolveDefaultDatabasePath(),
                BuildMetadata.envPrefix() + "_DUCKDB_DATABASE_PATH", "duckdb.database.path");
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
        return preferences.getPath("duckdb.backup.path", resolveDefaultBackupPath(),
                BuildMetadata.envPrefix() + "_DUCKDB_BACKUP_PATH", "duckdb.backup.path");
    }

    /**
     * Resolves a unique timestamped filename for a database backup.
     *
     * @return path for a new backup file
     */
    public Path resolveBackupFile() {
        var currentTimestamp = System.currentTimeMillis();
        var uniqueTimestamp = LAST_BACKUP_TIMESTAMP.updateAndGet(
                previousTimestamp -> Math.max(currentTimestamp, previousTimestamp + 1));
        var dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(uniqueTimestamp), ZoneId.systemDefault());
        return resolveBackupPath().resolve("backup_" + dateTime.format(BACKUP_TIMESTAMP) + ".db");
    }

    /**
     * Resolves the directory available for DuckDB temporary data.
     *
     * @return configured directory or the system temporary directory
     */
    public Path resolveTempPath() {
        return preferences.getPath("duckdb.temp.path", resolveDefaultTempPath(),
                BuildMetadata.envPrefix() + "_DUCKDB_TEMP_PATH", "duckdb.temp.path");
    }

    private static Path resolveDefaultDatabasePath() {
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

    private static Path resolveDefaultBackupPath() {
        return resolveDefaultDatabasePath();
    }

    private static Path resolveDefaultTempPath() {
        var temporaryDirectory = System.getProperty("java.io.tmpdir");
        return temporaryDirectory == null ? Paths.get(".") : Paths.get(temporaryDirectory);
    }
}
