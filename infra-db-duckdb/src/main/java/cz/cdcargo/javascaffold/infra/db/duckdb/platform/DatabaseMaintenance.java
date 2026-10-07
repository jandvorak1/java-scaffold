package cz.cdcargo.javascaffold.infra.db.duckdb.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;

/**
 * Creates backups of and restores the application's DuckDB database.
 *
 * Each operation is serialized to prevent concurrent replacement of the
 * database file. A restore verifies the candidate before closing the shared
 * data source and retains a temporary copy of the original database until the
 * replacement can be opened successfully.
 */
public final class DatabaseMaintenance {

    private static final Object MAINTENANCE_LOCK = new Object();
    private static final String BACKUP_CATALOG = "backup";
    private final PathResolver resolver;

    /**
     * Creates a maintenance service using locations resolved from preferences.
     *
     * @param preferences source of database and backup paths
     * @throws NullPointerException if preferences is null
     */
    public DatabaseMaintenance(PreferencesStore preferences) {
        this.resolver = new PathResolver(Objects.requireNonNull(preferences, "Preferences must not be null"));
    }

    /**
     * Creates a timestamped backup containing the complete DuckDB catalog.
     *
     * @return path to the created backup file
     * @throws IllegalStateException if the backup directory or database copy
     *                               cannot be created
     */
    public Path backup() {
        synchronized (MAINTENANCE_LOCK) {
            return createBackup();
        }
    }

    private Path createBackup() {
        Path backup = null;
        try {
            backup = resolver.resolveBackupFile();
            Files.createDirectories(backup.getParent());
            Files.deleteIfExists(backup);
            try (var conn = DataSourceProvider.getInstance(resolver).getConnection()) {
                conn.setAutoCommit(true);
                try (var stmt = conn.createStatement()) {
                    stmt.execute("ATTACH '" + escapeLiteral(backup) + "' AS " + BACKUP_CATALOG);
                    try {
                        stmt.execute("COPY FROM DATABASE " + quoteIdentifier(currentDatabase(stmt))
                                + " TO " + BACKUP_CATALOG);
                    } finally {
                        stmt.execute("DETACH " + BACKUP_CATALOG);
                    }
                }
                return backup;
            }
        } catch (SQLException | IOException e) {
            deleteIncompleteBackup(backup, e);
            throw new IllegalStateException("Backup database failed", e);
        }
    }

    /**
     * Replaces the current database with a verified backup.
     *
     * @param backup backup file to restore
     * @throws NullPointerException if backup is null
     * @throws IllegalStateException if the backup is invalid or the restoration
     *                               fails
     */
    public void restore(Path backup) {
        Objects.requireNonNull(backup, "Backup must not be null");
        synchronized (MAINTENANCE_LOCK) {
            restoreBackup(backup);
        }
    }

    private void restoreBackup(Path backup) {
        try {
            var target = resolver.resolveDatabaseFile();
            var safety = target.resolveSibling(target.getFileName() + ".bak");
            validate(backup);
            verify(backup);
            DataSourceProvider.shutdown();
            Files.createDirectories(target.getParent());
            var hadOriginal = Files.exists(target);
            if (hadOriginal) {
                Files.copy(target, safety, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.copy(backup, target, StandardCopyOption.REPLACE_EXISTING);
                verify(target);
                Files.deleteIfExists(safety);
            } catch (IOException | RuntimeException e) {
                try {
                    if (hadOriginal) {
                        Files.copy(safety, target, StandardCopyOption.REPLACE_EXISTING);
                        Files.deleteIfExists(safety);
                    } else {
                        Files.deleteIfExists(target);
                    }
                } catch (IOException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw new IllegalStateException("Restore database failed: " + backup, e);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Restore database failed: " + backup, e);
        }
    }

    /**
     * Verifies that a backup candidate is a readable, non-empty regular file.
     *
     * @param file candidate backup file
     * @throws IllegalStateException if the file is missing, unreadable, empty,
     *                               or not regular
     */
    protected static void validate(Path file) {
        try {
            if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
                throw new IllegalStateException("Backup file must be a readable regular file: " + file);
            }
            if (Files.size(file) == 0) {
                throw new IllegalStateException("Backup file is empty: " + file);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to validate backup file: " + file, e);
        }
    }

    /**
     * Verifies that DuckDB can open a database file without modifying it.
     *
     * @param file database file to check
     * @throws IllegalStateException if DuckDB cannot open or query the file
     */
    protected static void verify(Path file) {
        var properties = new Properties();
        properties.setProperty("duckdb.read_only", "true");
        var url = "jdbc:duckdb:" + file;
        try (var conn = java.sql.DriverManager.getConnection(url, properties);
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT 1")) {
            if (!rs.next()) {
                throw new IllegalStateException("Database integrity check failed: " + file);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database verification failed: " + file, e);
        }
    }

    private static String currentDatabase(java.sql.Statement statement) throws SQLException {
        try (var result = statement.executeQuery("SELECT current_database()")) {
            if (!result.next()) {
                throw new SQLException("Current DuckDB database is unavailable");
            }
            return result.getString(1);
        }
    }

    private static String escapeLiteral(Path path) {
        return path.toAbsolutePath().toString().replace("'", "''");
    }

    private static String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private static void deleteIncompleteBackup(Path backup, Exception failure) {
        if (backup == null) {
            return;
        }
        try {
            Files.deleteIfExists(backup);
        } catch (IOException deleteFailure) {
            failure.addSuppressed(deleteFailure);
        }
    }
}
