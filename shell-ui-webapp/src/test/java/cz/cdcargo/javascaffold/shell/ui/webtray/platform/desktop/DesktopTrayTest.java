package cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;

class DesktopTrayTest {

    @Test
    void testTryInstallWithNullUri() {
        var exception = assertThrows(NullPointerException.class,
                () -> DesktopTray.tryInstall(null, null, null, null, null, null, null, null, () -> {
                }, () -> {
                }));

        assertEquals("URI must not be null", exception.getMessage());
    }

    @Test
    void testTryInstallWithNullOpenAction() {
        var exception = assertThrows(NullPointerException.class,
                () -> DesktopTray.tryInstall(null, null, null, null, null, null, null, URI.create("https://localhost"),
                        null, () -> {
                        }));

        assertEquals("Open action must not be null", exception.getMessage());
    }

    @Test
    void testTryInstallWithNullShutdownAction() {
        var exception = assertThrows(NullPointerException.class,
                () -> DesktopTray.tryInstall(null, null, null, null, null, null, null, URI.create("https://localhost"),
                        () -> {
                        }, null));

        assertEquals("Shutdown action must not be null", exception.getMessage());
    }
}
