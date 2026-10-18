package cz.cdcargo.javascaffold.shell.ui.webapp;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cdcargo.javascaffold.core.platform.build.BuildMetadata;
import cz.cdcargo.javascaffold.core.platform.exception.AbstractBusinessException;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.core.platform.status.BackgroundWorkerRegistry;
import cz.cdcargo.javascaffold.shell.ui.webapp.bootstrap.MessageBootstrap;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message.GenerateGreetingMessageEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.message.ReadAllMessageEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.auth.LoginEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.auth.LogoutEndpoint;
import cz.cdcargo.javascaffold.shell.ui.webapp.endpoint.system.auth.SessionEndpoint;
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
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.account.InMemoryUserAccountStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthenticationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.exception.AuthorizationException;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ErrorResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity.SessionCookieIdentity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.password.Argon2PasswordHasher;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.password.Argon2PasswordVerifier;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.password.InMemoryPasswordCredentialStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.password.PasswordAuthenticator;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebCors;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.session.SessionCookie;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.session.SessionManager;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.AccessTokenService;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.token.InMemoryAccessTokenStore;
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
        var uri = URI.create("http://" + host + ":" + port);

        var allowedOrigins = preferences.get("server.origins.allowed", "",
                BuildMetadata.envPrefix() + "_SERVER_ORIGINS_ALLOWED", "server.origins.allowed");
        var sameOrigins = preferences.get("server.origins.same", uri.toString(),
                BuildMetadata.envPrefix() + "_SERVER_ORIGINS_SAME", "server.origins.same");

        var accountStore = new InMemoryUserAccountStore();
        var credentialStore = new InMemoryPasswordCredentialStore();
        var tokenStore = new InMemoryAccessTokenStore();
        var token = new AccessTokenService(tokenStore, Clock.systemUTC(), Duration.ofMinutes(20),
                Duration.ofMinutes(20));
        var cookie = new SessionCookie("JSESSIONID", true);
        var session = new SessionManager(token, cookie, Duration.ofMinutes(20));
        var verifier = new Argon2PasswordVerifier();
        var hasher = new Argon2PasswordHasher();
        var authenticator = new PasswordAuthenticator(accountStore, credentialStore, verifier);

        var identity = new SessionCookieIdentity(token, cookie);
        var registry = new BackgroundWorkerRegistry();
        var engine = TemplateEngine.createPrecompiled(ContentType.Html);
        var security = new WebSecurity(sameOrigins);
        var cors = new WebCors(allowedOrigins);
        var writer = new ResponseWriter(security);
        var errorWriter = new ErrorResponseWriter(writer);

        var messageBootstrap = new MessageBootstrap(preferences);
        var generateGreetingMessageEndpoint = new GenerateGreetingMessageEndpoint(identity, security,
                messageBootstrap.buildGenerateGreetingMessageUseCase(), writer, registry);
        var readAllMessageEndpoint = new ReadAllMessageEndpoint(identity, security,
                messageBootstrap.buildReadAllMessageUseCase(), writer, registry);

        var loginEndpoint = new LoginEndpoint(security, writer, authenticator, session);
        var logoutEndpoint = new LogoutEndpoint(security, writer, session);
        var sessionEndpoint = new SessionEndpoint(security, writer);
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

                        .post("/api/system/auth/login", loginEndpoint::execute)
                        .post("/api/system/auth/logout", logoutEndpoint::execute)
                        .get("/api/system/auth/session", sessionEndpoint::execute)
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
}
