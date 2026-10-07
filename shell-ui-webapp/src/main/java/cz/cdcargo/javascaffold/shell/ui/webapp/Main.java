package cz.cdcargo.javascaffold.shell.ui.webapp;

import java.io.PrintStream;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import cz.cdcargo.javascaffold.core.platform.build.BuildMetadata;
import cz.cdcargo.javascaffold.core.platform.logger.ConsoleHandler;
import cz.cdcargo.javascaffold.core.platform.logger.FileHandler;
import cz.cdcargo.javascaffold.core.platform.preferences.FilePreferencesStore;
import cz.cdcargo.javascaffold.core.platform.preferences.PreferencesStore;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop.ErrorDialog;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Locales;
import cz.cdcargo.javascaffold.shell.ui.webapp.platform.i18n.Messages;

/**
 * Starts the webapp desktop application.
 *
 * Startup configures console encoding, platform integration, localization,
 * Swing, logging, and uncaught exception handling before the embedded web
 * application is created.
 */
public final class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final PreferencesStore PREFS = FilePreferencesStore.root();

    private Main() {
    }

    /**
     * Configures and starts the application with persisted user preferences.
     *
     * @param args command-line arguments, which are currently ignored
     */
    public static void main(String[] args) {
        configureConsole();
        configurePlatform();

        var locale = PREFS.getLocale("settings.locale", Locales.DEFAULT);
        Locale.setDefault(locale);
        var messages = new Messages(locale);

        configureSwing(messages);
        configureUncaughtExceptionHandler(messages);
        configureLogging();

        new Application(PREFS);
    }

    private static void configureConsole() {
        setUtf8CodePage();
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }

    private static void configurePlatform() {
        Logger.getLogger("com.formdev.flatlaf").setLevel(Level.OFF);
        System.setProperty("flatlaf.useWindowDecorations", "true");
        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", BuildMetadata.name());
            System.setProperty("apple.awt.application.appearance", "system");
        }
    }

    private static void configureSwing(Messages messages) {
        FlatLightLaf.setup();
        UIManager.put("OptionPane.okButtonText", messages.get("main.ok.action"));
        UIManager.put("OptionPane.cancelButtonText", messages.get("main.cancel.action"));
        UIManager.put("OptionPane.yesButtonText", messages.get("main.yes.action"));
        UIManager.put("OptionPane.noButtonText", messages.get("main.no.action"));
    }

    private static void configureLogging() {
        try {
            var root = Logger.getLogger("");
            var level = PREFS.getLevel("logs.file.level", Level.INFO, BuildMetadata.envPrefix() + "_LOGS_FILE_LEVEL",
                    "logs.file.level");
            var fileHandler = new FileHandler(PREFS);
            fileHandler.setLevel(level);

            var console = PREFS.getBoolean("logs.console", false, BuildMetadata.envPrefix() + "_LOGS_CONSOLE",
                    "logs.console");
            ConsoleHandler consoleHandler = null;
            if (console) {
                consoleHandler = new ConsoleHandler(PREFS);
                consoleHandler.setLevel(level);
            }

            for (var handler : root.getHandlers()) {
                root.removeHandler(handler);
            }
            root.setLevel(level);
            root.addHandler(fileHandler);
            if (consoleHandler != null) {
                root.addHandler(consoleHandler);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure logging", e);
        }
    }

    private static void configureUncaughtExceptionHandler(Messages messages) {
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> SwingUtilities.invokeLater(() -> {
                    LOGGER.log(Level.SEVERE, "Unhandled exception", throwable);
                    var dialog = new ErrorDialog(
                            null,
                            BuildMetadata.name(),
                            throwable.getMessage(),
                            messages.get("platform.desktop.error.button.show"),
                            messages.get("platform.desktop.error.button.hide"),
                            messages.get("platform.desktop.error.button.show"),
                            messages.get("platform.desktop.error.button.close"),
                            throwable);
                    dialog.setVisible(true);
                    System.exit(1);
                }));
    }

    private static void setUtf8CodePage() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            return;
        }
        try {
            var linker = Linker.nativeLinker();
            var kernel32 = SymbolLookup.libraryLookup("kernel32", Arena.ofAuto());
            var symbol = kernel32.find("SetConsoleOutputCP").orElseThrow();
            var setConsoleOutputCP = linker.downcallHandle(symbol,
                    FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT));
            var result = (boolean) setConsoleOutputCP.invokeExact(65001);
            if (!result) {
                throw new IllegalStateException();
            }
        } catch (Throwable ignored) {
            // UTF-8 console setup is optional.
        }
    }
}
