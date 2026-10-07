package cz.cdcargo.javascaffold.infra.db.sqlite.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

class DataSourceProviderTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        DataSourceProvider.shutdown();
    }

    @Test
    void testCreateConfig() {
        var resolver = new PathResolver(createPreferences());
        var config = DataSourceProvider.createConfig(resolver);

        assertEquals("jdbc:sqlite:" + resolver.resolveDatabaseFile(), config.getJdbcUrl());
        assertFalse(config.isAutoCommit());
        assertEquals(2, config.getMaximumPoolSize());
        assertEquals(1, config.getMinimumIdle());
    }

    @Test
    void testCreateConfigRejectsNullResolver() {
        var exception = assertThrows(NullPointerException.class, () -> DataSourceProvider.createConfig(null));

        assertEquals("Resolver must not be null", exception.getMessage());
    }

    @Test
    void testCreateDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite::memory:");
        config.setInitializationFailTimeout(0);
        var dataSource = DataSourceProvider.createDataSource(config);

        assertNotNull(dataSource);
        assertInstanceOf(HikariDataSource.class, dataSource);
        dataSource.close();
    }

    @Test
    void testCreateDataSourceRejectsNullConfiguration() {
        var exception = assertThrows(NullPointerException.class, () -> DataSourceProvider.createDataSource(null));

        assertEquals("Configuration must not be null", exception.getMessage());
    }

    @Test
    void testEnsureDatabaseDirectoryExists() {
        var resolver = new PathResolver(createPreferences());
        DataSourceProvider.ensureDatabaseDirectoryExists(resolver);
        var path = resolver.resolveDatabasePath();

        assertTrue(Files.exists(path));
        assertTrue(Files.isDirectory(path));
    }

    @Test
    void testGetInstance() {
        var resolver = new PathResolver(createPreferences());
        var result = DataSourceProvider.getInstance(resolver);

        assertNotNull(result);
        assertInstanceOf(HikariDataSource.class, result);
    }

    @Test
    void testGetInstanceRejectsNullResolver() {
        var exception = assertThrows(NullPointerException.class, () -> DataSourceProvider.getInstance(null));

        assertEquals("Resolver must not be null", exception.getMessage());
    }

    @Test
    void testGetInstanceSingleton() {
        var resolver = new PathResolver(createPreferences());
        var first = DataSourceProvider.getInstance(resolver);
        var second = DataSourceProvider.getInstance(resolver);

        assertSame(first, second);
    }

    @Test
    void testGetInstanceConnect() throws Exception {
        var resolver = new PathResolver(createPreferences());
        var dataSource = (HikariDataSource) DataSourceProvider.getInstance(resolver);
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS message (id INTEGER PRIMARY KEY, text VARCHAR(255) NOT NULL);");
            statement.executeUpdate("INSERT INTO message(text) VALUES ('hello');");
            connection.commit();

            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM message;")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    @Test
    void testShutdown() {
        var resolver = new PathResolver(createPreferences());
        var first = (HikariDataSource) DataSourceProvider.getInstance(resolver);
        DataSourceProvider.shutdown();
        assertTrue(first.isClosed());
        var second = (HikariDataSource) DataSourceProvider.getInstance(resolver);
        assertNotSame(first, second);
        assertFalse(second.isClosed());
    }

    private FilePreferencesStore createPreferences() {
        var preferences = FilePreferencesStore.at(tempDir.resolve("preferences"));
        preferences.putPath("sqlite.database.path", tempDir.resolve("sqlite"));
        preferences.putPath("sqlite.backup.path", tempDir.resolve("backup"));
        return preferences;
    }
}
