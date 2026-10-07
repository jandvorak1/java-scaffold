package cz.cdcargo.javascaffold.shell.ui.webapp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import cz.cdcargo.javascaffold.core.platform.build.BuildMetadata;
import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.shell.ui.webapp.bootstrap.MessageBootstrap;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message.GenerateGreetingMessageEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message.ReadAllMessageEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors.AuthenticationErrorHandler;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors.AuthorizationErrorHandler;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors.BusinessErrorHandler;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors.HttpErrorHandler;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.errors.UnexpectedErrorHandler;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.formats.LoadDateFormatEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.formats.LoadDateTimeFormatEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.formats.LoadDecimalFormatEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.formats.LoadTimeFormatEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs.CancelJobEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs.DeleteJobEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.jobs.StatusJobEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.locales.LoadLocaleEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.preferences.LoadPreferencesEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.preferences.SavePreferencesEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.timezones.LoadTimeZoneEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.webtray.ShutdownEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.webtray.StatusEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.page.favicon.FaviconEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.page.home.HomePageEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop.DesktopTray;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthenticationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity.LocalIdentity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebCors;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import io.helidon.http.HttpException;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.ClasspathHandlerConfig;
import io.helidon.webserver.staticcontent.StaticContentFeature;

/**
 * Starts the webapp desktop application and its embedded HTTP server.
 *
 * The application resolves runtime settings, detects an existing instance,
 * creates shared services, registers endpoints and error handlers, installs
 * the desktop tray, and schedules opening the user interface in a browser.
 */
public final class Application {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
    private final WebServer server;

    /**
     * Creates and starts the application from the supplied preference store.
     *
     * Message commands share one security policy and require a valid CSRF token
     * and trusted request origin before their workers can be started.
     *
     * @param preferences source of server, localization, security, and desktop
     *                    settings
     * @throws NullPointerException if preferences is null
     * @throws RuntimeException     if configuration or server startup fails
     */
    public Application(PreferencesStore preferences) {
        Objects.requireNonNull(preferences, "Preferences store must not be null");

        var host = preferences.get("server.host", "localhost", BuildMetadata.envPrefix() + "_SERVER_HOST",
                "server.host");
        var port = preferences.getInt("server.port", 3000, BuildMetadata.envPrefix() + "_SERVER_PORT",
                "server.port");
        var timeout = preferences.getInt("server.timeout", 5000, BuildMetadata.envPrefix() + "_SERVER_TIMEOUT",
                "server.timeout");
        var uri = URI.create("http://" + host + ":" + port);

        var allowedOrigins = preferences.get("server.origins.allowed", "",
                BuildMetadata.envPrefix() + "_SERVER_ORIGINS_ALLOWED", "server.origins.allowed");
        var sameOrigins = preferences.get("server.origins.same", uri.toString(),
                BuildMetadata.envPrefix() + "_SERVER_ORIGINS_SAME", "server.origins.same");
        var locale = preferences.getLocale("settings.locale", Locales.DEFAULT);

        var identity = new LocalIdentity();
        var registry = new BackgroundWorkerRegistry();
        var engine = TemplateEngine.createPrecompiled(ContentType.Html);
        var security = new WebSecurity(sameOrigins);
        var cors = new WebCors(allowedOrigins);
        var writer = new ResponseWriter(security);
        var errorWriter = new ErrorResponseWriter(writer);
        var messages = new Messages(Locales.resolve(locale));

        checkExistingInstance(host, port, timeout, messages);

        var messageBootstrap = new MessageBootstrap(preferences);
        var generateGreetingMessageEndpoint = new GenerateGreetingMessageEndpoint(identity, security,
                messageBootstrap.buildGenerateGreetingMessageUseCase(), writer, registry);
        var readAllMessageEndpoint = new ReadAllMessageEndpoint(identity, security,
                messageBootstrap.buildReadAllMessageUseCase(), writer, registry);

        var loadDateFormatEndpoint = new LoadDateFormatEndpoint(preferences, writer);
        var loadDateTimeFormatEndpoint = new LoadDateTimeFormatEndpoint(preferences, writer);
        var loadDecimalFormatEndpoint = new LoadDecimalFormatEndpoint(preferences, writer);
        var loadTimeFormatEndpoint = new LoadTimeFormatEndpoint(preferences, writer);
        var cancelJobEndpoint = new CancelJobEndpoint(security, writer, registry);
        var deleteJobEndpoint = new DeleteJobEndpoint(security, writer, registry);
        var statusJobEndpoint = new StatusJobEndpoint(preferences, writer, registry);
        var loadLocaleEndpoint = new LoadLocaleEndpoint(preferences, writer);
        var loadPreferencesEndpoint = new LoadPreferencesEndpoint(preferences, writer);
        var savePreferencesEndpoint = new SavePreferencesEndpoint(security, preferences, writer);
        var loadTimeZoneEndpoint = new LoadTimeZoneEndpoint(writer);

        var statusEndpoint = new StatusEndpoint(writer);
        var shutdownEndpoint = new ShutdownEndpoint(security, writer, this::shutdown);

        var faviconEndpoint = new FaviconEndpoint();
        var homePageEndpoint = new HomePageEndpoint(identity, preferences, engine, writer, security);

        var authenticationErrorHandler = new AuthenticationErrorHandler(preferences, errorWriter);
        var authorizationErrorHandler = new AuthorizationErrorHandler(preferences, errorWriter);
        var businessErrorHandler = new BusinessErrorHandler(preferences, errorWriter);
        var httpErrorHandler = new HttpErrorHandler(preferences, errorWriter);
        var unexpectedErrorHandler = new UnexpectedErrorHandler(preferences, errorWriter);

        var builder = WebServer.builder()
                .host(host)
                .port(port)
                .routing(build -> build
                        .post("/api/message/greeting", generateGreetingMessageEndpoint::execute)
                        .post("/api/message/read", readAllMessageEndpoint::execute)

                        .get("/api/system/formats/date", loadDateFormatEndpoint::execute)
                        .get("/api/system/formats/datetime", loadDateTimeFormatEndpoint::execute)
                        .get("/api/system/formats/decimal", loadDecimalFormatEndpoint::execute)
                        .get("/api/system/formats/time", loadTimeFormatEndpoint::execute)
                        .get("/api/system/jobs/{token}", statusJobEndpoint::execute)
                        .delete("/api/system/jobs/{token}", deleteJobEndpoint::execute)
                        .post("/api/system/jobs/{token}/cancel", cancelJobEndpoint::execute)
                        .get("/api/system/locales", loadLocaleEndpoint::execute)
                        .get("/api/system/preferences", loadPreferencesEndpoint::execute)
                        .post("/api/system/preferences", savePreferencesEndpoint::execute)
                        .get("/api/system/timezones", loadTimeZoneEndpoint::execute)

                        .get("/api/webtray/status", statusEndpoint::execute)
                        .post("/api/webtray/shutdown", shutdownEndpoint::execute)

                        .get("/favicon.ico", faviconEndpoint::execute)
                        .get("/{path:(?!api(?:/|$)|bundle(?:/|$)).*}", homePageEndpoint::execute)

                        .error(AuthenticationException.class, authenticationErrorHandler::execute)
                        .error(AuthorizationException.class, authorizationErrorHandler::execute)
                        .error(AbstractBusinessException.class, businessErrorHandler::execute)
                        .error(HttpException.class, httpErrorHandler::execute)
                        .error(RuntimeException.class, unexpectedErrorHandler::execute));

        cors.create().ifPresent(builder::addFeature);

        server = builder.addFeature(StaticContentFeature.create(b -> b
                .addClasspath(ClasspathHandlerConfig.builder()
                        .context("/bundle")
                        .location("/bundle")
                        .build())))
                .build()
                .start();

        DesktopTray.tryInstall(messages.get("application.tray.tooltip", BuildMetadata.name()),
                messages.get("application.tray.menu.open"), messages.get("application.tray.menu.about"),
                messages.get("application.tray.menu.copy"), messages.get("application.tray.menu.shutdown"),
                messages.get("application.tray.dialog.title"),
                messages.get("application.tray.dialog.message", BuildMetadata.name(), BuildMetadata.version()), uri,
                () -> openBrowser(preferences, uri), this::shutdown);
        System.out.println("Server is running on port " + port + "...");
        Thread.ofVirtual().name("browser-launcher").start(() -> openBrowser(preferences, uri));
    }

    private void shutdown() {
        try {
            server.stop();
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Failed to stop web server", exception);
        } finally {
            System.exit(0);
        }
    }

    private static void checkExistingInstance(String host, int port, int timeout, Messages messages) {
        try (var probe = new Socket()) {
            probe.connect(new InetSocketAddress(host, port), timeout);
            showExistingInstanceWarning(messages);
            System.exit(0);
        } catch (SocketTimeoutException exception) {
            LOGGER.log(Level.WARNING, "Existing instance did not respond within the configured timeout", exception);
            showExistingInstanceWarning(messages);
            System.exit(0);
        } catch (IOException ignored) {
            // Port is available.
        }
    }

    private static void showExistingInstanceWarning(Messages messages) {
        var message = messages.get("application.error.instance");
        LOGGER.warning(message);
        JOptionPane.showMessageDialog(null, message, messages.get("application.warning.title"),
                JOptionPane.WARNING_MESSAGE);
    }

    private static void openBrowser(PreferencesStore preferences, URI uri) {
        var messages = new Messages(Locales.resolve(preferences.getLocale("settings.locale", Locales.DEFAULT)));
        try {
            if (!sleep(500)) {
                return;
            }
            var process = createOpenBrowserProcess(uri);
            var exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.warning(messages.get("application.error.browser", exitCode, uri));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, messages.get("application.error.browser", -1, uri), e);
        }
    }

    private static Process createOpenBrowserProcess(URI uri) throws IOException {
        var os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        switch (os) {
            case String s when s.contains("mac") || s.contains("darwin"):
                return new ProcessBuilder("open", uri.toString()).start();
            case String s when s.contains("win"):
                return new ProcessBuilder("cmd.exe", "/C", "start", "", uri.toString()).start();
            default:
                return new ProcessBuilder("xdg-open", uri.toString()).start();
        }
    }

    private static boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
