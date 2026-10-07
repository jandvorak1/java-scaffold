package cz.cdcargo.javascaffold.infra.db.sqlite.platform;

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
    void testResolveConfiguredPaths() {
        var resolver = new PathResolver(createPreferences());

        assertEquals(tempDir.resolve("sqlite"), resolver.resolveDatabasePath());
        assertEquals(tempDir.resolve("sqlite/app.db"), resolver.resolveDatabaseFile());
        assertEquals(tempDir.resolve("backup"), resolver.resolveBackupPath());
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
        preferences.putPath("sqlite.database.path", tempDir.resolve("sqlite"));
        preferences.putPath("sqlite.backup.path", tempDir.resolve("backup"));
        return preferences;
    }
}
