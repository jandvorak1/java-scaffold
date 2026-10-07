package cz.cdcargo.javascaffold.infra.db.duckdb.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Provides the shared DuckDB connection pool for this process.
 *
 * The first caller creates the pool and initializes the schema-version
 * table. Subsequent callers receive that pool until it is shut down.
 */
public final class DataSourceProvider {

    private static final String POOL_NAME = "java-pool";
    private static volatile HikariDataSource instance;
    private static boolean shutdownHookRegistered;

    private DataSourceProvider() {
    }

    /**
     * Returns the shared data source, creating and initializing it when needed.
     *
     * @param resolver resolver for the database location
     * @return shared DuckDB data source
     * @throws NullPointerException if resolver is null
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
     * Closes and removes the current shared data source.
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
     * Creates a data source and initializes its schema-version table.
     *
     * @param resolver resolver for the database file
     * @return initialized connection pool
     * @throws NullPointerException if resolver is null
     * @throws IllegalStateException if pool or schema initialization fails
     */
    protected static HikariDataSource createSingleton(PathResolver resolver) {
        Objects.requireNonNull(resolver, "Resolver must not be null");
        ensureDatabaseDirectoryExists(resolver);
        ensureTemporaryDirectoryExists(resolver);
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
     * Builds pool settings for the resolved DuckDB database file.
     *
     * @param resolver resolver for the database file
     * @return configured Hikari settings
     * @throws NullPointerException if resolver is null
     */
    protected static HikariConfig createConfig(PathResolver resolver) {
        Objects.requireNonNull(resolver, "Resolver must not be null");
        var config = new HikariConfig();

        config.setPoolName(POOL_NAME);
        config.setJdbcUrl("jdbc:duckdb:" + resolver.resolveDatabaseFile().toString());
        config.addDataSourceProperty("temp_directory", resolver.resolveTempPath().toAbsolutePath().toString());
        config.setAutoCommit(false);
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30_000);
        config.setInitializationFailTimeout(5_000);
        return config;
    }

    /**
     * Creates a Hikari data source from pool settings.
     *
     * @param config Hikari pool settings
     * @return initialized Hikari data source
     * @throws NullPointerException if config is null
     * @throws RuntimeException if Hikari cannot initialize the pool
     */
    protected static HikariDataSource createDataSource(HikariConfig config) {
        return new HikariDataSource(Objects.requireNonNull(config, "Configuration must not be null"));
    }

    private static void registerShutdownHook() {
        synchronized (DataSourceProvider.class) {
            if (!shutdownHookRegistered) {
                Runtime.getRuntime().addShutdownHook(
                        new Thread(DataSourceProvider::shutdown, "duckdb-data-source-shutdown"));
                shutdownHookRegistered = true;
            }
        }
    }

    /**
     * Ensures that the directory for the database file exists.
     *
     * @param resolver resolver for the database directory
     * @throws NullPointerException if resolver is null
     * @throws IllegalStateException if the directory cannot be created
     */
    protected static void ensureDatabaseDirectoryExists(PathResolver resolver) {
        Objects.requireNonNull(resolver, "Resolver must not be null");
        try {
            Files.createDirectories(resolver.resolveDatabasePath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create DuckDB database directory", e);
        }
    }

    /**
     * Ensures that the directory configured for DuckDB temporary data exists.
     *
     * @param resolver resolver for the temporary-data directory
     * @throws NullPointerException if resolver is null
     * @throws IllegalStateException if the directory cannot be created
     */
    protected static void ensureTemporaryDirectoryExists(PathResolver resolver) {
        Objects.requireNonNull(resolver, "Resolver must not be null");
        try {
            Files.createDirectories(resolver.resolveTempPath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create DuckDB temporary directory", e);
        }
    }

    /**
     * Creates the schema-version table and its initial row when absent.
     *
     * @param dataSource pool used to initialize the schema
     * @throws NullPointerException if dataSource is null
     * @throws IllegalStateException if schema initialization fails
     */
    protected static void ensureSchemaVersionTableExists(HikariDataSource dataSource) {
        Objects.requireNonNull(dataSource, "Data source must not be null");
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS version (
                        id INTEGER PRIMARY KEY CHECK (id = 1),
                        version INTEGER NOT NULL,
                        updated_at TIMESTAMP NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO version(id, version, updated_at)
                    SELECT 1, 0, CURRENT_TIMESTAMP
                    WHERE NOT EXISTS (SELECT 1 FROM version WHERE id = 1)
                    """);
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Creating schema version table failed", e);
        }
    }
}
