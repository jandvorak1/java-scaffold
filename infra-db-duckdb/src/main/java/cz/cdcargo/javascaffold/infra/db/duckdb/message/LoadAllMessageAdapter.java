package cz.cdcargo.javascaffold.infra.db.duckdb.message;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import javax.sql.DataSource;

import cz.cdcargo.javascaffold.core.application.message.LoadAllMessagePort;
import cz.cdcargo.javascaffold.core.domain.message.Message;
import cz.cdcargo.javascaffold.core.domain.message.MessageID;
import cz.cdcargo.javascaffold.core.domain.message.MessageTitle;
import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;
import cz.cdcargo.javascaffold.core.platform.status.TextStatusChannel;

/**
 * Replaces all messages with the predefined sample data.
 *
 * The adapter returns the created messages in their defined order and publishes
 * a progress message for each one. Cancellation is checked before the reset
 * and while data is prepared and read. Database and runtime failures are
 * reported as LoadAllMessageFailedException.
 */
public final class LoadAllMessageAdapter implements LoadAllMessagePort {

    private static final List<String> MESSAGE_TITLES = List.of("Hello World!", "Hello Czechia!", "Hello Slovakia!",
            "Hello Poland!", "Hello Austria!", "Hello Germany!");
    private final Supplier<String> busyMessageSupplier;
    private final DataSource dataSource;

    /**
     * Creates an adapter using the supplied DuckDB data source.
     *
     * @param busyMessageSupplier supplier of progress messages with an optional
     *                            {0} placeholder
     * @param dataSource          source of DuckDB connections
     * @throws NullPointerException if busyMessageSupplier or dataSource is null
     */
    public LoadAllMessageAdapter(Supplier<String> busyMessageSupplier, DataSource dataSource) {
        this.busyMessageSupplier = Objects.requireNonNull(busyMessageSupplier,
                "Busy message supplier must not be null");
        this.dataSource = Objects.requireNonNull(dataSource, "Data source must not be null");
    }

    @Override
    public List<Message> execute() {
        var messages = new ArrayList<Message>(MESSAGE_TITLES.size());
        try (var connection = dataSource.getConnection()) {
            CancellationToken.throwIfCancelled();
            resetMessages(connection);

            try (var statement = connection.createStatement();
                    var resultSet = statement.executeQuery("SELECT id, title FROM message ORDER BY display_order")) {
                while (resultSet.next()) {
                    CancellationToken.throwIfCancelled();
                    var id = new MessageID(UUID.fromString(resultSet.getString("id")));
                    var title = new MessageTitle(resultSet.getString("title"));
                    messages.add(new Message(id, title));

                    var busyMessage = Objects.requireNonNull(busyMessageSupplier.get(),
                            "Busy message must not be null");
                    TextStatusChannel.send(busyMessage.replace("{0}", Integer.toString(messages.size())));
                }
            }
            return messages;
        } catch (SQLException | RuntimeException e) {
            throw new LoadAllMessageFailedException(e);
        }
    }

    private static void resetMessages(Connection connection) throws SQLException {
        try {
            try (var statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS message");
                statement.execute(
                        "CREATE TABLE message (id UUID PRIMARY KEY, title VARCHAR NOT NULL, display_order INTEGER NOT NULL UNIQUE)");
            }
            try (var insert = connection
                    .prepareStatement("INSERT INTO message (id, title, display_order) VALUES (?, ?, ?)")) {
                for (var index = 0; index < MESSAGE_TITLES.size(); index++) {
                    CancellationToken.throwIfCancelled();
                    insert.setString(1, UUID.randomUUID().toString());
                    insert.setString(2, MESSAGE_TITLES.get(index));
                    insert.setInt(3, index);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackFailure) {
                e.addSuppressed(rollbackFailure);
            }
            throw e;
        }
    }
}
