package cz.cdcargo.javascaffold.shell.ui.webapp.bootstrap;

import java.util.Objects;

import cz.cdcargo.javascaffold.core.application.message.GenerateGreetingMessageService;
import cz.cdcargo.javascaffold.core.application.message.GenerateGreetingMessageUseCase;
import cz.cdcargo.javascaffold.core.application.message.ReadAllMessageService;
import cz.cdcargo.javascaffold.core.application.message.ReadAllMessageUseCase;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.infra.db.duckdb.message.LoadAllMessageAdapter;
import cz.cdcargo.javascaffold.infra.db.duckdb.platform.DataSourceProvider;
import cz.cdcargo.javascaffold.infra.db.duckdb.platform.PathResolver;

import javax.sql.DataSource;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;

/**
 * Assembles message use cases with their required infrastructure adapters.
 * User preferences determine the database configuration and locale used by
 * message-loading progress updates.
 */
public final class MessageBootstrap {

    private static final String LOAD_MESSAGES_BUSY_KEY = "bootstrap.busy.load.message";

    private final PreferencesStore preferences;
    private final DataSource dataSource;
    private final PathResolver pathResolver; 

    /**
     * Creates a bootstrap for message-related application use cases.
     *
     * @param preferences application preferences used to configure infrastructure
     * @throws NullPointerException if preferences is null
     */
    public MessageBootstrap(PreferencesStore preferences) {
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");        
        this.pathResolver = new PathResolver(preferences);
        this.dataSource = DataSourceProvider.getInstance(pathResolver);        
    }

    /**
     * Creates the use case for generating greeting messages.
     *
     * @return configured greeting message generation use case
     */
    public GenerateGreetingMessageUseCase buildGenerateGreetingMessageUseCase() {
        return new GenerateGreetingMessageService();
    }

    /**
     * Creates the use case for loading all messages from the configured database.
     * The progress message is localized using the locale configured when this
     * method is called.
     *
     * @return configured message loading use case
     */
    public ReadAllMessageUseCase buildReadAllMessageUseCase() {
        var busyMessage = messages().get(LOAD_MESSAGES_BUSY_KEY);
        var loadAllMessagePort = new LoadAllMessageAdapter(() -> busyMessage, dataSource);
        return new ReadAllMessageService(loadAllMessagePort);
    }

    private Messages messages() {
        var locale = Locales.resolve(preferences.getLocale("settings.locale", Locales.DEFAULT));
        return new Messages(locale);
    }
}
