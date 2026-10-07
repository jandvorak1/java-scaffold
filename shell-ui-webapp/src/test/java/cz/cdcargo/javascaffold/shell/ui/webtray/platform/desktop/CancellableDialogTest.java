package cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import cz.cdcargo.javascaffold.core.platform.status.CancellationToken;

class CancellableDialogTest {

    @Test
    void testRunOutsideEventDispatchThread() {
        var exception = assertThrows(IllegalStateException.class,
                () -> CancellableDialog.run(null, null, null, null, null, null));

        assertEquals("CancellableDialog must be used on the Swing event dispatch thread", exception.getMessage());
    }

    @Test
    void testRunWithNullCancellationToken() {
        var exception = assertThrows(InvocationTargetException.class,
                () -> SwingUtilities.invokeAndWait(
                        () -> CancellableDialog.run(null, null, null, null, null, null)));

        var cause = assertInstanceOf(NullPointerException.class, exception.getCause());
        assertEquals("Cancellation token must not be null", cause.getMessage());
    }

    @Test
    void testRunWithNullWorker() {
        var token = new CancellationToken();
        var exception = assertThrows(InvocationTargetException.class,
                () -> SwingUtilities.invokeAndWait(
                        () -> CancellableDialog.run(null, null, null, null, token, null)));

        var cause = assertInstanceOf(NullPointerException.class, exception.getCause());
        assertEquals("Worker must not be null", cause.getMessage());
    }
}
