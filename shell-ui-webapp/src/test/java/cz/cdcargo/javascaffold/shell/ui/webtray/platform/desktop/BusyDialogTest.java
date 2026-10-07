package cz.cdcargo.javascaffold.shell.ui.webapp.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class BusyDialogTest {

    @Test
    void testRunOutsideEventDispatchThread() {
        var exception = assertThrows(IllegalStateException.class,
                () -> BusyDialog.run(null, null, null, null));

        assertEquals("Busy dialog must be used on the Swing event dispatch thread", exception.getMessage());
    }

    @Test
    void testRunWithNullWorker() {
        var exception = assertThrows(InvocationTargetException.class,
                () -> SwingUtilities.invokeAndWait(() -> BusyDialog.run(null, null, null, null)));

        var cause = assertInstanceOf(NullPointerException.class, exception.getCause());
        assertEquals("Worker must not be null", cause.getMessage());
    }
}
