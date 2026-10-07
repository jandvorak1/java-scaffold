package cz.cdcargo.javascaffold.infra.db.duckdb.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;

class PathResolverTest {

    @TempDir
    Path tempDir;

    @Test
    void testResolveDatabasePathUsesConfiguredValue() {
        var resolver = new PathResolver(createPreferences());

        assertEquals(tempDir.resolve("duckdb"), resolver.resolveDatabasePath());
    }

    @Test
    void testResolveDatabaseFileUsesConfiguredPath() {
        var resolver = new PathResolver(createPreferences());

        assertEquals(tempDir.resolve("duckdb/app.db"), resolver.resolveDatabaseFile());
    }

    @Test
    void testResolveBackupPathUsesConfiguredValue() {
        var resolver = new PathResolver(createPreferences());

        assertEquals(tempDir.resolve("backup"), resolver.resolveBackupPath());
    }

    @Test
    void testResolveTempPathUsesConfiguredValue() {
        var resolver = new PathResolver(createPreferences());

        assertEquals(tempDir.resolve("temporary"), resolver.resolveTempPath());
    }

    @Test
    void testResolveTimestampedBackupFile() {
        var resolver = new PathResolver(createPreferences());

        var backup = resolver.resolveBackupFile();

        assertEquals(tempDir.resolve("backup"), backup.getParent());
        assertTrue(backup.getFileName().toString().matches("backup_\\d{8}_\\d{6}_\\d{3}\\.db"));
    }

    @Test
    void testResolveBackupFileReturnsUniqueNames() {
        var resolver = new PathResolver(createPreferences());

        var first = resolver.resolveBackupFile();
        var second = resolver.resolveBackupFile();

        assertNotEquals(first, second);
    }

    @Test
    void testConstructorRejectsNullPreferences() {
        var exception = assertThrows(NullPointerException.class, () -> new PathResolver(null));

        assertEquals("Preferences must not be null", exception.getMessage());
    }

    private FilePreferencesStore createPreferences() {
        var preferences = FilePreferencesStore.at(tempDir.resolve("preferences"));
        preferences.putPath("duckdb.database.path", tempDir.resolve("duckdb"));
        preferences.putPath("duckdb.backup.path", tempDir.resolve("backup"));
        preferences.putPath("duckdb.temp.path", tempDir.resolve("temporary"));
        return preferences;
    }
}
