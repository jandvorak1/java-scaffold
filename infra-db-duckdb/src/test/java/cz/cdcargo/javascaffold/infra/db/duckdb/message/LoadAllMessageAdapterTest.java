package cz.cdcargo.javascaffold.infra.db.duckdb.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;
import cz.cdcargo.javascaffold.core.platform.status.TextStatusChannel;
import cz.cdcargo.javascaffold.infra.db.duckdb.platform.DataSourceProvider;
import cz.cdcargo.javascaffold.infra.db.duckdb.platform.PathResolver;

class LoadAllMessageAdapterTest {

    private static final List<String> MESSAGE_TITLES = List.of("Hello World!", "Hello Czechia!", "Hello Slovakia!",
            "Hello Poland!", "Hello Austria!", "Hello Germany!");

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        CancellationToken.clear();
        TextStatusChannel.clear();
        DataSourceProvider.shutdown();
    }

    @Test
    void testExecuteReturnsMessagesInDefinedOrder() throws SQLException {
        var output = new LoadAllMessageAdapter(() -> "test", createDataSource()).execute();

        assertEquals(MESSAGE_TITLES, output.stream().map(message -> message.title().value()).toList());
    }

    @Test
    void testExecutePublishesProgress() {
        var statuses = new ArrayList<String>();
        TextStatusChannel.register(statuses::add);

        new LoadAllMessageAdapter(() -> "Loading message {0}", createDataSource()).execute();

        assertEquals(List.of("Loading message 1", "Loading message 2", "Loading message 3", "Loading message 4",
                "Loading message 5", "Loading message 6"), statuses);
    }

    @Test
    void testExecuteReplacesExistingMessagesWithTestData() throws SQLException {
        var dataSource = createDataSourceWithTitles(List.of("Existing message"));

        new LoadAllMessageAdapter(() -> "test", dataSource).execute();

        assertEquals(MESSAGE_TITLES, createStoredTitles(dataSource));
    }

    @Test
    void testExecuteWrapsCancellationBeforeReset() throws SQLException {
        var dataSource = createDataSourceWithTitles(List.of("Existing message"));
        var token = new CancellationToken();
        token.cancel();
        CancellationToken.register(token);

        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> "test", dataSource).execute());

        assertInstanceOf(CancellationException.class, exception.getCause());
        assertEquals(List.of("Existing message"), createStoredTitles(dataSource));
    }

    @Test
    void testExecuteWrapsSupplierFailure() {
        var cause = new IllegalStateException("Failure");

        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> {
                    throw cause;
                }, createDataSource()).execute());

        assertSame(cause, exception.getCause());
    }

    @Test
    void testExecuteWrapsNullStatusMessage() {
        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> null, createDataSource()).execute());

        var cause = assertInstanceOf(NullPointerException.class, exception.getCause());
        assertEquals("Busy message must not be null", cause.getMessage());
    }

    @Test
    void testExecuteWrapsDatabaseFailure() {
        var dataSource = createDataSource();
        DataSourceProvider.shutdown();

        var exception = assertThrows(LoadAllMessageFailedException.class,
                () -> new LoadAllMessageAdapter(() -> "test", dataSource).execute());

        assertInstanceOf(SQLException.class, exception.getCause());
    }

    @Test
    void testConstructorRejectsNullBusyMessageSupplier() {
        var dataSource = createDataSource();

        var nullSupplier = assertThrows(NullPointerException.class,
                () -> new LoadAllMessageAdapter(null, dataSource));

        assertEquals("Busy message supplier must not be null", nullSupplier.getMessage());
    }

    @Test
    void testConstructorRejectsNullDataSource() {
        var nullDataSource = assertThrows(NullPointerException.class,
                () -> new LoadAllMessageAdapter(() -> "test", null));

        assertEquals("Data source must not be null", nullDataSource.getMessage());
    }

    private DataSource createDataSource() {
        var resolver = new PathResolver(createPreferences());
        return DataSourceProvider.getInstance(resolver);
    }

    private DataSource createDataSourceWithTitles(List<String> titles) throws SQLException {
        var dataSource = createDataSourceWithEmptyMessageTable();
        try (var connection = dataSource.getConnection();
                var insert = connection.prepareStatement(
                        "INSERT INTO message (id, title, display_order) VALUES (?, ?, ?)")) {
            for (var index = 0; index < titles.size(); index++) {
                insert.setString(1, UUID.randomUUID().toString());
                insert.setString(2, titles.get(index));
                insert.setInt(3, index);
                insert.addBatch();
            }
            insert.executeBatch();
            connection.commit();
        }
        return dataSource;
    }

    private DataSource createDataSourceWithEmptyMessageTable() throws SQLException {
        var dataSource = createDataSource();
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE message (id UUID PRIMARY KEY, title VARCHAR NOT NULL, display_order INTEGER NOT NULL UNIQUE)");
            connection.commit();
        }
        return dataSource;
    }

    private List<String> createStoredTitles(DataSource dataSource) throws SQLException {
        var messages = new ArrayList<String>();
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT title FROM message ORDER BY display_order")) {
            while (resultSet.next()) {
                messages.add(resultSet.getString("title"));
            }
        }
        return messages;
    }

    private FilePreferencesStore createPreferences() {
        var preferences = FilePreferencesStore.at(tempDir.resolve("preferences"));
        preferences.putPath("duckdb.database.path", tempDir.resolve("duckdb"));
        preferences.putPath("duckdb.backup.path", tempDir.resolve("backup"));
        preferences.putPath("duckdb.temp.path", tempDir.resolve("temporary"));
        return preferences;
    }
}
