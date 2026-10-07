package cz.cdcargo.javascaffold.infra.db.sqlite.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Objects;

import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;

/**
 * Backs up and restores the SQLite database managed by this application.
 *
 * Restores validate the candidate before closing the shared pool. The current
 * database is retained until the copied replacement passes an integrity check.
 */
public final class DatabaseMaintenance {

    private static final Object MAINTENANCE_LOCK = new Object();
    private final PathResolver resolver;

    /**
     * Creates a maintenance service that resolves database locations from
     * preferences.
     *
     * @param preferences source of database and backup paths
     * @throws NullPointerException if preferences is null
     */
    public DatabaseMaintenance(PreferencesStore preferences) {
        this.resolver = new PathResolver(Objects.requireNonNull(preferences, "Preferences must not be null"));
    }

    /**
     * Creates a timestamped online backup by using SQLite's VACUUM INTO
     * command.
     *
     * @return created backup file
     * @throws IllegalStateException if the backup directory or database copy
     *                               cannot be created
     */
    public Path backup() {
        synchronized (MAINTENANCE_LOCK) {
            return createBackup();
        }
    }

    private Path createBackup() {
        try {
            var backup = resolver.resolveBackupFile();
            Files.createDirectories(backup.getParent());
            var escaped = backup.toString().replace("'", "''");
            try (var conn = DataSourceProvider.getInstance(resolver).getConnection()) {
                conn.setAutoCommit(true);
                try (var stmt = conn.createStatement()) {
                    stmt.execute("VACUUM INTO '" + escaped + "'");
                }
                return backup;
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Backup database failed", e);
        }
    }

    /**
     * Replaces the current database with the content of a verified backup.
     *
     * If the replacement does not pass verification, the previous database is
     * copied back.
     *
     * @param backup file to restore
     * @throws NullPointerException  if backup is null
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
     * Runs SQLite's quick integrity check on a database file.
     *
     * @param file database file to check
     * @throws IllegalStateException if SQLite cannot open the file or the check
     *                               does not return ok
     */
    protected static void verify(Path file) {
        var url = "jdbc:sqlite:" + file;
        try (var conn = java.sql.DriverManager.getConnection(url);
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("PRAGMA quick_check")) {
            if (!rs.next() || !"ok".equalsIgnoreCase(rs.getString(1))) {
                throw new IllegalStateException("Database integrity check failed: " + file);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database integrity check failed: " + file, e);
        }
    }
}
