package cz.cdcargo.javascaffold.infra.db.sqlite.platform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import com.zaxxer.hikari.HikariDataSource;

class DatabaseMaintenanceTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        DataSourceProvider.shutdown();
    }

    @Test
    void testValidate() {
        var file = tempDir.resolve("missing.db");
        assertThrows(IllegalStateException.class, () -> DatabaseMaintenance.validate(file));
    }

    @Test
    void testValidateEmpty() throws Exception {
        var file = tempDir.resolve("empty.db");
        Files.createFile(file);
        assertThrows(IllegalStateException.class, () -> DatabaseMaintenance.validate(file));
    }

    @Test
    void testValidateNonEmpty() throws Exception {
        var file = tempDir.resolve("empty.db");
        Files.writeString(file, "dummy");
        assertDoesNotThrow(() -> DatabaseMaintenance.validate(file));
    }

    @Test
    void testVerify() throws Exception {
        var file = tempDir.resolve("test.db");
        try (var conn = DriverManager.getConnection("jdbc:sqlite:" + file.toString());
                var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE test (id INTEGER PRIMARY KEY, name TEXT NOT NULL);");
            stmt.execute("INSERT INTO test(name) VALUES ('hello');");
        }
        assertDoesNotThrow(() -> DatabaseMaintenance.verify(file));
    }

    @Test
    void testRestore() throws SQLException, IOException {
        var preferences = createPreferences();
        var resolver = new PathResolver(preferences);
        var backup = resolver.resolveBackupFile();
        Files.createDirectories(backup.getParent());
        try (var conn = DriverManager.getConnection("jdbc:sqlite:" + backup.toString());
                var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE message(id INTEGER PRIMARY KEY, name TEXT NOT NULL);");
            stmt.execute("INSERT INTO message(name) values ('from-backup');");
        }

        new DatabaseMaintenance(preferences).restore(backup);

        assertTrue(Files.exists(resolver.resolveDatabaseFile()));
        try (var conn = DriverManager.getConnection("jdbc:sqlite:" + resolver.resolveDatabaseFile().toString());
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM message;")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void testBackup() throws SQLException {
        var preferences = createPreferences();
        var resolver = new PathResolver(preferences);
        var dataSource = DataSourceProvider.getInstance(resolver);
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE message(id INTEGER PRIMARY KEY, name TEXT NOT NULL)");
            stmt.execute("INSERT INTO message(name) VALUES ('live')");
            conn.commit();
        }

        var backup = new DatabaseMaintenance(preferences).backup();

        assertTrue(Files.isRegularFile(backup));
        try (var conn = DriverManager.getConnection("jdbc:sqlite:" + backup);
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT name FROM message")) {
            assertTrue(rs.next());
            assertEquals("live", rs.getString(1));
        }
    }

    @Test
    void testRestoreKeepsOriginalDatabaseWhenBackupIsCorrupt() throws Exception {
        var preferences = createPreferences();
        var resolver = new PathResolver(preferences);
        Files.createDirectories(resolver.resolveDatabasePath());
        try (var conn = DriverManager.getConnection("jdbc:sqlite:" + resolver.resolveDatabaseFile());
                var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE message(name TEXT NOT NULL)");
            stmt.execute("INSERT INTO message(name) VALUES ('original')");
        }
        var dataSource = (HikariDataSource) DataSourceProvider.getInstance(resolver);
        var corruptBackup = tempDir.resolve("corrupt.db");
        Files.writeString(corruptBackup, "not a database");

        assertThrows(IllegalStateException.class,
                () -> new DatabaseMaintenance(preferences).restore(corruptBackup));
        assertFalse(dataSource.isClosed());
        try (var conn = DriverManager.getConnection("jdbc:sqlite:" + resolver.resolveDatabaseFile());
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT name FROM message")) {
            assertTrue(rs.next());
            assertEquals("original", rs.getString(1));
        }
    }

    private FilePreferencesStore createPreferences() {
        var preferences = FilePreferencesStore.at(tempDir.resolve("preferences"));
        preferences.putPath("sqlite.database.path", tempDir.resolve("sqlite"));
        preferences.putPath("sqlite.backup.path", tempDir.resolve("backup"));
        return preferences;
    }
}
