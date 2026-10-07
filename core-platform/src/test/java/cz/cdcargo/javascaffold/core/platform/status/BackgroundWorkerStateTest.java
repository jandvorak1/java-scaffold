package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BackgroundWorkerStateTest {

    @Test
    void testIsTerminalIdentifiesLifecycleEndStates() {
        assertFalse(BackgroundWorkerState.READY.isTerminal());
        assertFalse(BackgroundWorkerState.RUNNING.isTerminal());
        assertTrue(BackgroundWorkerState.DONE.isTerminal());
        assertTrue(BackgroundWorkerState.FAILED.isTerminal());
        assertTrue(BackgroundWorkerState.CANCELLED.isTerminal());
    }
}
