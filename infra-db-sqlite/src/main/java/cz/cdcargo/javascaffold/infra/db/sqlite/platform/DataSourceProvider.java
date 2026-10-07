package cz.cdcargo.javascaffold.infra.db.sqlite.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Provides the shared SQLite connection pool for this process.
 *
 * The first resolver creates the pool lazily. Later callers receive that pool
 * until it is shut down.
 */
public final class DataSourceProvider {

    private static final String POOL_NAME = "java-pool";
    private static volatile HikariDataSource instance;
    private static boolean shutdownHookRegistered;

    private DataSourceProvider() {
    }

    /**
     * Returns the shared data source, creating and initializing it if absent.
     *
     * @param resolver location resolver used when a pool must be created
     * @return shared SQLite data source
     * @throws NullPointerException  if resolver is null
     * @throws IllegalStateException if the database location, pool, or schema
     *                               cannot be initialized
     */
    public static DataSource getInstance(PathResolver resolver) {
        Objects.requireNonNull(resolver, "Resolver must not be null");
        var result = instance;
        if (result == null) {
            synchronized (DataSourceProvider.class) {
                result = instance;
                if (result == null) {
                    result = createSingleton(resolver);
                    instance = result;
                }
            }
        }
        return result;
    }

    /**
     * Closes the current shared pool and removes it from this provider.
     */
    public static void shutdown() {
        synchronized (DataSourceProvider.class) {
            if (instance != null) {
                instance.close();
                instance = null;
            }
        }
    }

    /**
     * Creates a new pool and initializes its schema-version table.
     *
     * @param resolver location resolver for the database file
     * @return initialized connection pool
     * @throws NullPointerException  if resolver is null
     * @throws IllegalStateException if pool or schema initialization fails
     */
    protected static HikariDataSource createSingleton(PathResolver resolver) {
        Objects.requireNonNull(resolver, "Resolver must not be null");
        ensureDatabaseDirectoryExists(resolver);
        var dataSource = createDataSource(createConfig(resolver));
        try {
            ensureSchemaVersionTableExists(dataSource);
            registerShutdownHook();
            return dataSource;
        } catch (RuntimeException e) {
            dataSource.close();
            throw e;
        }
    }

    /**
     * Builds the Hikari configuration for the resolved database file.
     *
     * @param resolver location resolver for the database file
     * @return configured Hikari settings
     * @throws NullPointerException if resolver is null
     */
    protected static HikariConfig createConfig(PathResolver resolver) {
        Objects.requireNonNull(resolver, "Resolver must not be null");
        var config = new HikariConfig();

        config.setPoolName(POOL_NAME);
        config.setJdbcUrl("jdbc:sqlite:" + resolver.resolveDatabaseFile().toString());
        config.setAutoCommit(false);
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30_000);
        config.setInitializationFailTimeout(5_000);
        return config;
    }

    /**
     * Creates a Hikari data source from its configuration.
     *
     * @param config Hikari pool configuration
     * @return initialized Hikari data source
     * @throws NullPointerException if config is null
     * @throws RuntimeException     if Hikari cannot initialize the pool
     */
    protected static HikariDataSource createDataSource(HikariConfig config) {
        return new HikariDataSource(Objects.requireNonNull(config, "Configuration must not be null"));
    }

    private static void registerShutdownHook() {
        synchronized (DataSourceProvider.class) {
            if (!shutdownHookRegistered) {
                Runtime.getRuntime().addShutdownHook(
                        new Thread(DataSourceProvider::shutdown, "sqlite-data-source-shutdown"));
                shutdownHookRegistered = true;
            }
        }
    }

    /**
     * Ensures that the directory for the database file exists.
     *
     * @param resolver location resolver for the database directory
     * @throws NullPointerException  if resolver is null
     * @throws IllegalStateException if the directory cannot be created
     */
    protected static void ensureDatabaseDirectoryExists(PathResolver resolver) {
        Objects.requireNonNull(resolver, "Resolver must not be null");
        try {
            Files.createDirectories(resolver.resolveDatabasePath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create SQLite database directory", e);
        }
    }

    /**
     * Creates the schema-version table and its initial row when absent.
     *
     * @param dataSource pool used to initialize the schema
     * @throws NullPointerException  if dataSource is null
     * @throws IllegalStateException if schema initialization fails
     */
    protected static void ensureSchemaVersionTableExists(HikariDataSource dataSource) {
        Objects.requireNonNull(dataSource, "Data source must not be null");
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS version (
                        id INTEGER PRIMARY KEY CHECK (id = 1),
                        version INTEGER NOT NULL,
                        updated_at TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT OR IGNORE INTO version(id, version, updated_at)
                    VALUES (1, 0, datetime('now'))
                    """);
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Creating schema version table failed", e);
        }
    }
}
