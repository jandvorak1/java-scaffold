package cz.cdcargo.javascaffold.shell.ui.webapp.page.home;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import cz.cdcargo.javascaffold.core.platform.build.BuildMetadata;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.http.ResponseWriter;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDateTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedDecimalFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.SupportedTimeFormat;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.TimeZones;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.identity.Identity;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.security.WebSecurity;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import io.helidon.http.Status;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonString;
import io.helidon.json.JsonValue;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Serves the initial page of the web user interface.
 *
 * The rendered document embeds UI bootstrap settings and application
 * configuration for the authenticated principal. Unsupported stored settings
 * are replaced with the application's default values before rendering.
 */
public final class HomePageEndpoint {

    private static final String TEMPLATE = "templates/home.jte";
    private static final String DEFAULT_THEME = "sap_fiori_3";
    private static final String PREF_THEME = "settings.theme";
    private static final String PREF_TIMEZONE = "settings.timezone";
    private static final String PREF_LOCALE = "settings.locale";
    private static final String PREF_DECIMAL_FORMAT = "settings.format.decimal";
    private static final String PREF_DATE_FORMAT = "settings.format.date";
    private static final String PREF_TIME_FORMAT = "settings.format.time";
    private static final String PREF_DATETIME_FORMAT = "settings.format.datetime";
    private static final String PREF_TOUCH = "settings.touch";

    private static final Set<String> UI5_LOCALE_TAGS = Set.of("en_GB", "es_MX", "fr_CA", "pt_PT", "zh_CN", "zh_TW");
    private static final Set<String> UI5_LANGUAGE_TAGS = Set.of("ar", "bg", "ca", "cs", "cy", "da", "de", "el", "en",
            "es", "et", "fi", "fr", "hi", "hr", "hu", "in", "it", "iw", "ja", "kk", "ko", "lt", "lv", "ms", "nl", "no",
            "pl", "pt", "ro", "ru", "sh", "sk", "sl", "sv", "th", "tr", "uk", "vi");

    private final Identity identity;
    private final PreferencesStore preferences;
    private final TemplateEngine engine;
    private final ResponseWriter writer;
    private final WebSecurity security;

    /**
     * Creates an endpoint from the services that provide identity,
     * preferences, rendering, response writing, and request security.
     *
     * @param identity    resolves the principal associated with a request
     * @param preferences reads the saved interface settings
     * @param engine      renders the page template
     * @param writer      writes the completed response
     * @param security    provides the CSRF token for client configuration
     * @throws NullPointerException if any dependency is null
     */
    public HomePageEndpoint(Identity identity, PreferencesStore preferences, TemplateEngine engine,
            ResponseWriter writer, WebSecurity security) {
        this.identity = Objects.requireNonNull(identity, "Identity must not be null");
        this.preferences = Objects.requireNonNull(preferences, "Preferences store must not be null");
        this.engine = Objects.requireNonNull(engine, "Template engine must not be null");
        this.writer = Objects.requireNonNull(writer, "Response writer must not be null");
        this.security = Objects.requireNonNull(security, "Web security must not be null");
    }

    /**
     * Renders the home page for the request principal and sends a successful
     * response.
     *
     * JSON configuration is escaped before it is inserted into the document,
     * and unknown display format identifiers use their default formats.
     *
     * @param request  supplies the principal for the rendered page
     * @param response receives the rendered page
     * @throws NullPointerException if request or response is null
     */
    public void execute(ServerRequest request, ServerResponse response) {
        Objects.requireNonNull(request, "Server request must not be null");
        Objects.requireNonNull(response, "Server response must not be null");

        var theme = preferences.get(PREF_THEME, DEFAULT_THEME);
        var timeZone = TimeZones.resolve(preferences.getZoneId(PREF_TIMEZONE, TimeZones.DEFAULT));
        var locale = Locales.resolve(preferences.getLocale(PREF_LOCALE, Locales.DEFAULT));
        var language = ui5LanguageTag(locale);
        var ui5Json = JsonValue.objectBuilder().set("theme", theme).set("timezone", timeZone.getId())
                .set("language", language).build();

        var principal = identity.requirePrincipal(request);
        var roles = principal.roles().stream().map(role -> JsonString.create(role.id())).map(JsonValue.class::cast)
                .toList();
        var permissions = principal.permissions().stream().map(permission -> JsonString.create(permission.id()))
                .map(JsonValue.class::cast).toList();
        var userJson = JsonValue.objectBuilder().set("subject", principal.subject())
                .set("roles", JsonArray.create(roles)).set("permissions", JsonArray.create(permissions)).build();

        var name = BuildMetadata.name();
        var version = BuildMetadata.version();
        var build = BuildMetadata.build();
        var csrfToken = security.csrfToken();
        var supportedLocales = Locales.supported(Locale.ROOT).stream().map(HomePageEndpoint::ui5LanguageTag)
                .map(JsonString::create).map(JsonValue.class::cast).toList();
        var decimalFormat = resolveFormat(preferences.get(PREF_DECIMAL_FORMAT,
                SupportedDecimalFormat.SPACE_COMMA.id()), SupportedDecimalFormat.SPACE_COMMA,
                SupportedDecimalFormat::byId);
        var dateFormat = resolveFormat(preferences.get(PREF_DATE_FORMAT,
                SupportedDateFormat.DD_MM_YYYY_DOT.id()), SupportedDateFormat.DD_MM_YYYY_DOT,
                SupportedDateFormat::byId);
        var timeFormat = resolveFormat(preferences.get(PREF_TIME_FORMAT,
                SupportedTimeFormat.HH_MM_SS_24.id()), SupportedTimeFormat.HH_MM_SS_24,
                SupportedTimeFormat::byId);
        var dateTimeFormat = resolveFormat(preferences.get(PREF_DATETIME_FORMAT,
                SupportedDateTimeFormat.DD_MM_YYYY_DOT_HH_MM_SS.id()),
                SupportedDateTimeFormat.DD_MM_YYYY_DOT_HH_MM_SS, SupportedDateTimeFormat::byId);
        var appJson = JsonValue.objectBuilder()
                .set("name", name)
                .set("version", version)
                .set("build", build)
                .set("csrfToken", csrfToken)
                .set("locale", locale.toLanguageTag())
                .set("timeZone", timeZone.getId())
                .set("supportedLocales", JsonArray.create(supportedLocales))
                .set("decimalFormat", decimalFormat.pattern())
                .set("dateFormat", dateFormat.pattern())
                .set("timeFormat", timeFormat.pattern())
                .set("dateTimeFormat", dateTimeFormat.pattern())
                .set("user", userJson).build();

        var touch = preferences.getBoolean(PREF_TOUCH, false);
        var params = Map.<String, Object>of("name", name, "version", version, "language", language, "ui5Json",
                escapeEmbeddedJson(ui5Json), "appJson", escapeEmbeddedJson(appJson), "touch",
                touch ? "ui5-content-density-cozy" : "ui5-content-density-compact");
        var output = new StringOutput();

        engine.render(TEMPLATE, params, output);
        writer.sendHtml(response, Status.OK_200, output.toString());
    }

    private static <T> T resolveFormat(String id, T defaultValue, Function<String, T> resolver) {
        try {
            return resolver.apply(id);
        } catch (IllegalArgumentException exception) {
            return defaultValue;
        }
    }

    private static String escapeEmbeddedJson(JsonValue json) {
        return json.toString()
                .replace("&", "\\u0026")
                .replace("<", "\\u003c")
                .replace(">", "\\u003e")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029");
    }

    private static String ui5LanguageTag(Locale locale) {
        var localeTag = locale.toLanguageTag().replace('-', '_');
        if (UI5_LOCALE_TAGS.contains(localeTag)) {
            return localeTag;
        }
        if (UI5_LANGUAGE_TAGS.contains(locale.getLanguage())) {
            return locale.getLanguage();
        }
        return Locales.DEFAULT.getLanguage();
    }
}
