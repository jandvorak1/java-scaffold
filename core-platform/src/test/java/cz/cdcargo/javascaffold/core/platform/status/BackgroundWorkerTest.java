package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class BackgroundWorkerTest {

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
    void testExecuteRejectsSecondInvocation() {
        // Arrange
        var worker = new NoOpWorker("test-token");

        // Act
        worker.execute();

        // Assert
        assertThrows(IllegalStateException.class, worker::execute);
    }

    @Test
    void testExecuteRecordsFailure() throws Exception {
        // Arrange
        var failedLatch = new CountDownLatch(1);
        var failure = new IllegalStateException("Failure");
        var worker = new BackgroundWorker<Void, String>("test-token") {

            @Override
            protected Void doInBackground() {
                throw failure;
            }

            @Override
            protected void failed(Exception cause) {
                failedLatch.countDown();
            }
        };

        // Act
        worker.execute();

        // Assert
        assertTrue(failedLatch.await(2, TimeUnit.SECONDS));
        var snapshot = worker.snapshot(0);
        assertEquals(BackgroundWorkerState.FAILED, snapshot.state());
        assertSame(failure, snapshot.error());
        assertTrue(worker.finished());
        assertFalse(worker.running());
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
    void testCancelInvokesCallbacksInOrder() throws Exception {
        // Arrange
        var startedLatch = new CountDownLatch(1);
        var callbacks = new ArrayList<String>();
        var worker = new BackgroundWorker<Void, String>("test-token") {

            @Override
            protected Void doInBackground() throws Exception {
                startedLatch.countDown();
                Thread.sleep(10_000);
                return null;
            }

            @Override
            protected void onCancelRequested() {
                callbacks.add("requested");
            }

            @Override
            protected void cancelled() {
                callbacks.add("cancelled");
            }
        };

        // Act
        worker.execute();

        // Assert
        assertTrue(startedLatch.await(2, TimeUnit.SECONDS));
        worker.cancel();
        assertEquals(List.of("requested", "cancelled"), callbacks);
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

    @Test
    void testPublishForwardsImmutableBatch() throws Exception {
        // Arrange
        var doneLatch = new CountDownLatch(1);
        var processedMessages = new AtomicReference<List<String>>();
        var worker = new BackgroundWorker<Void, String>("test-token") {

            @Override
            protected Void doInBackground() {
                publish(List.of("first", "second"));
                return null;
            }

            @Override
            protected void process(List<String> messages) {
                processedMessages.set(messages);
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
        assertEquals(List.of("first", "second"), processedMessages.get());
        assertThrows(UnsupportedOperationException.class, () -> processedMessages.get().add("third"));
    }

    @Test
    void testSnapshotRejectsNegativeOffset() {
        // Arrange
        var worker = new NoOpWorker("test-token");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> worker.snapshot(-1));
    }

    @Test
    void testConstructorRejectsMissingToken() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new NoOpWorker(null));
    }

    @Test
    void testConstructorRejectsBlankToken() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new NoOpWorker(" \t"));
    }

    @Test
    void testState() {
        // Arrange
        var worker = new NoOpWorker("test-token");

        // Act + Assert
        assertEquals(BackgroundWorkerState.READY, worker.state());
    }

    @Test
    void testToken() {
        // Arrange
        var worker = new NoOpWorker("test-token");

        // Act + Assert
        assertEquals("test-token", worker.token());
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
