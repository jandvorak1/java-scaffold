package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class BackgroundWorkerTest {

    @Test
    void testConstructor() {
        // Arrange + Act + Assert
        var worker = assertDoesNotThrow(() -> new NoOpWorker("test-token"));
        assertNotNull(worker);
    }

    @Test
    void testConstructorNull() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new NoOpWorker(null));
    }

    @Test
    void testExecute() throws Exception {
        // Arrange
        var doneLatch = new CountDownLatch(1);
        var resultRef = new AtomicReference<String>();
        var worker = new BackgroundWorker<String, String>("test-token") {

            @Override
            protected String doInBackground() {
                return "done";
            }

            @Override
            protected void done(String result) {
                resultRef.set(result);
                doneLatch.countDown();
            }
        };

        // Act
        worker.execute();

        // Assert
        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));
        assertEquals("done", resultRef.get());
        assertEquals(BackgroundWorkerState.DONE, worker.state());
    }

    @Test
    void testCancel() throws Exception {
        // Arrange
        var startedLatch = new CountDownLatch(1);
        var cancelledLatch = new CountDownLatch(1);
        var worker = new BackgroundWorker<Void, String>("test-token") {

            @Override
            protected Void doInBackground() throws Exception {
                startedLatch.countDown();
                Thread.sleep(10_000);
                return null;
            }

            @Override
            protected void cancelled() {
                cancelledLatch.countDown();
            }
        };

        // Act
        worker.execute();

        // Assert
        assertTrue(startedLatch.await(2, TimeUnit.SECONDS));
        worker.cancel();
        assertTrue(cancelledLatch.await(2, TimeUnit.SECONDS));
        assertEquals(BackgroundWorkerState.CANCELLED, worker.state());
    }

    @Test
    void testToken() {
        // Arrange
        var worker = new NoOpWorker("test-token");

        // Act + Assert
        assertEquals("test-token", worker.token());
    }

    @Test
    void testState() {
        // Arrange
        var worker = new NoOpWorker("test-token");

        // Act + Assert
        assertEquals(BackgroundWorkerState.READY, worker.state());
    }

    @Test
    void testRunning() throws Exception {
        // Arrange
        var startedLatch = new CountDownLatch(1);
        var worker = new BackgroundWorker<Void, String>("test-token") {

            @Override
            protected Void doInBackground() throws Exception {
                startedLatch.countDown();
                Thread.sleep(10_000);
                return null;
            }
        };

        // Act
        worker.execute();

        // Assert
        assertTrue(startedLatch.await(2, TimeUnit.SECONDS));
        assertTrue(worker.running());
        worker.cancel();
    }

    @Test
    void testFinished() throws Exception {
        // Arrange
        var doneLatch = new CountDownLatch(1);
        var worker = new BackgroundWorker<Void, String>("test-token") {

            @Override
            protected Void doInBackground() {
                return null;
            }

            @Override
            protected void done(Void result) {
                doneLatch.countDown();
            }
        };

        // Act
        worker.execute();

        // Assert
        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));
        assertTrue(worker.finished());
    }

    @Test
    void testSnapshot() throws Exception {
        // Arrange
        var doneLatch = new CountDownLatch(1);
        var worker = new BackgroundWorker<Void, String>("test-token") {

            @Override
            protected Void doInBackground() {
                publish("first");
                publish("second");
                return null;
            }

            @Override
            protected void done(Void result) {
                doneLatch.countDown();
            }
        };

        // Act
        worker.execute();

        // Assert
        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));
        var snapshot = worker.snapshot(1);
        assertEquals("test-token", snapshot.token());
        assertEquals(BackgroundWorkerState.DONE, snapshot.state());
        assertNotNull(snapshot.createdAt());
        assertNotNull(snapshot.finishedAt());
        assertEquals(2, snapshot.messageOffset());
        assertEquals(List.of("second"), snapshot.messages());
    }

    private static class NoOpWorker extends BackgroundWorker<Void, String> {

        private NoOpWorker(String token) {
            super(token);
        }

        @Override
        protected Void doInBackground() {
            return null;
        }
    }
}
