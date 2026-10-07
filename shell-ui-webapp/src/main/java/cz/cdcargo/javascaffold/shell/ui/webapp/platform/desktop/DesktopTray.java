package cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import dorkbox.os.OS;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;

/**
 * Provides integration with the desktop system tray.
 *
 * The integration installs a platform-specific tray icon, creates the tray
 * menu, opens the application, displays application information, copies the
 * application address, and initiates application shutdown. Only one tray
 * instance can be installed during the lifetime of the process.
 */
public final class DesktopTray {

    private static final Logger LOGGER = Logger.getLogger(DesktopTray.class.getName());
    private static final String TRAY_ICON_WINDOWS_PATH = "/assets/icons/icon-128x128.png";
    private static final String TRAY_ICON_MACOS_PATH = "/assets/icons/icon-mono-128x128.png";
    private static final String TRAY_ICON_LINUX_PATH = "/assets/icons/icon-mono-128x128.png";
    private static final String APP_ICON_PATH = "/assets/icons/icon-%dx%d.png";
    private static boolean installed;

    private DesktopTray() {
    }

    /**
     * Installs the application icon and menu in the system tray when the current
     * desktop environment supports it. The open menu item invokes the supplied
     * open action. Repeated calls after a successful installation leave the
     * existing tray unchanged and return true.
     *
     * @param toolTipText      text displayed as the tray icon tooltip
     * @param openMenuText     label for the open menu item
     * @param aboutMenuText    label for the about menu item
     * @param copyMenuText     label for the copy URL menu item
     * @param shutdownMenuText label for the shutdown menu item
     * @param aboutTitle       title of the about dialog
     * @param aboutMessageText message displayed by the about action
     * @param uri              application URI copied by the copy URL action
     * @param openAction       action invoked when the tray icon is activated
     * @param shutdownAction   action invoked by the shutdown menu item
     * @return true when the tray is installed or was installed previously, or
     *         false when the environment does not support it or initialization
     *         fails
     * @throws NullPointerException if uri, openAction, or shutdownAction is null
     */
    public static synchronized boolean tryInstall(String toolTipText, String openMenuText, String aboutMenuText,
            String copyMenuText, String shutdownMenuText, String aboutTitle, String aboutMessageText, URI uri,
            Runnable openAction, Runnable shutdownAction) {
        Objects.requireNonNull(uri, "URI must not be null");
        Objects.requireNonNull(openAction, "Open action must not be null");
        Objects.requireNonNull(shutdownAction, "Shutdown action must not be null");
        if (installed) {
            return true;
        }

        try {
            var systemTray = SystemTray.get();
            if (systemTray == null) {
                return false;
            }

            var iconPath = trayIconPath();
            var trayIcon = Objects.requireNonNull(DesktopTray.class.getResource(iconPath),
                    "Missing tray icon resource: " + iconPath);
            systemTray.setTooltip(toolTipText);
            systemTray.setImage(trayIcon);
            systemTray.getMenu().add(new MenuItem(openMenuText, event -> openAction.run()));
            systemTray.getMenu().add(new JSeparator());
            systemTray.getMenu()
                    .add(new MenuItem(aboutMenuText, event -> showAboutDialog(aboutTitle, aboutMessageText)));
            systemTray.getMenu().add(new MenuItem(copyMenuText, event -> copyToClipboard(uri.toString())));
            systemTray.getMenu().add(new JSeparator());
            systemTray.getMenu().add(new MenuItem(shutdownMenuText, event -> shutdownAction.run()));
            installed = true;
            return true;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Tray initialization failed", exception);
            return false;
        }
    }

    private static String trayIconPath() {
        if (OS.INSTANCE.isWindows()) {
            return TRAY_ICON_WINDOWS_PATH;
        }
        if (OS.INSTANCE.isMacOsX()) {
            return TRAY_ICON_MACOS_PATH;
        }
        if (OS.INSTANCE.isLinux()) {
            return TRAY_ICON_LINUX_PATH;
        }
        return TRAY_ICON_WINDOWS_PATH;
    }

    private static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private static void showAboutDialog(String title, String messageText) {
        SwingUtilities.invokeLater(() -> {
            var pane = new JOptionPane(messageText, JOptionPane.INFORMATION_MESSAGE);
            var dialog = pane.createDialog(title);
            dialog.setIconImages(loadApplicationIcons());
            dialog.setVisible(true);
            dialog.dispose();
        });
    }

    private static List<Image> loadApplicationIcons() {
        try {
            var icon16 = ImageIO.read(DesktopTray.class.getResource(String.format(APP_ICON_PATH, 16, 16)));
            var icon32 = ImageIO.read(DesktopTray.class.getResource(String.format(APP_ICON_PATH, 32, 32)));
            var icon64 = ImageIO.read(DesktopTray.class.getResource(String.format(APP_ICON_PATH, 64, 64)));
            var icon128 = ImageIO.read(DesktopTray.class.getResource(String.format(APP_ICON_PATH, 128, 128)));
            return List.of(icon16, icon32, icon64, icon128);
        } catch (IOException | IllegalArgumentException ignore) {
            return List.of();
        }
    }
}
