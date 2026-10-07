package cz.cdcargo.javascaffold.infra.db.sqlite.message;

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
 * Replaces SQLite messages with the predefined sample data and returns them in
 * insertion order.
 *
 * The operation checks for cancellation before resetting the table and while
 * rows are prepared and loaded. It publishes progress for every loaded message
 * and replaces the {0} placeholder with that message's one-based number.
 * Database and runtime failures are reported as LoadAllMessageFailedException.
 */
public final class LoadAllMessageAdapter implements LoadAllMessagePort {

    private static final List<String> MESSAGE_TITLES = List.of("Hello World!", "Hello Czechia!", "Hello Slovakia!",
            "Hello Poland!", "Hello Austria!", "Hello Germany!");
    private final Supplier<String> busyMessageSupplier;
    private final DataSource dataSource;

    /**
     * Creates an adapter that uses a SQLite data source.
     *
     * @param busyMessageSupplier supplier of progress messages that can contain
     *                            the {0} placeholder
     * @param dataSource          source of SQLite connections
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
                    var resultSet = statement.executeQuery("SELECT id, title FROM message ORDER BY rowid")) {
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
                statement.execute("CREATE TABLE message (id TEXT PRIMARY KEY, title TEXT NOT NULL)");
            }
            try (var insert = connection.prepareStatement("INSERT INTO message (id, title) VALUES (?, ?)")) {
                for (var title : MESSAGE_TITLES) {
                    CancellationToken.throwIfCancelled();
                    insert.setString(1, UUID.randomUUID().toString());
                    insert.setString(2, title);
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
