package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class BackgroundWorkerRegistryTest {

    @Test
    void testConstructorRejectsNullClock() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new BackgroundWorkerRegistry(null, Duration.ZERO));
    }

    @Test
    void testConstructorRejectsNullRetention() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> new BackgroundWorkerRegistry(Clock.systemUTC(), null));
    }

    @Test
    void testConstructorRejectsNegativeRetention() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> new BackgroundWorkerRegistry(Clock.systemUTC(), Duration.ofSeconds(-1)));
    }

    @Test
    void testAdd() {
        // Arrange
        var registry = new BackgroundWorkerRegistry();
        var worker = new NoOpWorker("worker-token");

        // Act
        registry.add(worker);

        // Assert
        assertSame(worker, registry.get("worker-token"));
    }

    @Test
    void testAddRejectsNullWorker() {
        // Arrange
        var registry = new BackgroundWorkerRegistry();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> registry.add(null));
    }

    @Test
    void testAddRejectsDuplicateWorker() {
        // Arrange
        var registry = new BackgroundWorkerRegistry();

        // Act
        registry.add(new NoOpWorker("worker-token"));

        // Assert
        assertThrows(IllegalStateException.class, () -> registry.add(new NoOpWorker("worker-token")));
    }

    @Test
    void testCancel() throws Exception {
        // Arrange
        var registry = new BackgroundWorkerRegistry();
        var startedLatch = new CountDownLatch(1);
        var cancelledLatch = new CountDownLatch(1);
        var worker = new BackgroundWorker<Void, String>("worker-token") {

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
        registry.add(worker);
        worker.execute();

        // Assert
        assertTrue(startedLatch.await(2, TimeUnit.SECONDS));
        registry.cancel("worker-token");
        assertTrue(cancelledLatch.await(2, TimeUnit.SECONDS));
        assertEquals(BackgroundWorkerState.CANCELLED, worker.state());
    }

    @Test
    void testGet() {
        // Arrange
        var registry = new BackgroundWorkerRegistry();
        var worker = new NoOpWorker("worker-token");

        // Act
        registry.add(worker);
        var result = registry.get("worker-token");

        // Assert
        assertSame(worker, result);
    }

    @Test
    void testGetRejectsNullToken() {
        // Arrange
        var registry = new BackgroundWorkerRegistry();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> registry.get(null));
    }

    @Test
    void testCancelRejectsNullToken() {
        // Arrange
        var registry = new BackgroundWorkerRegistry();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> registry.cancel(null));
    }

    @Test
    void testRemoveRejectsNullToken() {
        // Arrange
        var registry = new BackgroundWorkerRegistry();

        // Act + Assert
        assertThrows(NullPointerException.class, () -> registry.remove(null));
    }

    @Test
    void testRemove() {
        // Arrange
        var registry = new BackgroundWorkerRegistry();
        var worker = new NoOpWorker("worker-token");

        // Act
        registry.add(worker);
        registry.remove("worker-token");

        // Assert
        assertThrows(NoSuchElementException.class, () -> registry.get("worker-token"));
    }

    @Test
    void testRemoveExpired() throws Exception {
        // Arrange
        var workerInstant = Instant.parse("2026-08-17T10:00:00Z");
        var workerClock = Clock.fixed(workerInstant, ZoneOffset.UTC);
        var registryClock = Clock.fixed(workerInstant.plusSeconds(1), ZoneOffset.UTC);
        var registry = new BackgroundWorkerRegistry(registryClock, Duration.ZERO);
        var doneLatch = new CountDownLatch(1);
        var worker = new BackgroundWorker<Void, String>("worker-token", workerClock) {

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
        registry.add(worker);
        worker.execute();

        // Assert
        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));
        registry.removeExpired();
        assertThrows(NoSuchElementException.class, () -> registry.get("worker-token"));
    }

    @Test
    void testRemoveExpiredHandlesRetentionLongerThanInstantRange() throws Exception {
        // Arrange
        var instant = Instant.parse("2026-08-17T10:00:00Z");
        var clock = Clock.fixed(instant, ZoneOffset.UTC);
        var registry = new BackgroundWorkerRegistry(clock, Duration.ofSeconds(Long.MAX_VALUE));
        var doneLatch = new CountDownLatch(1);
        var worker = new BackgroundWorker<Void, String>("worker-token", clock) {

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
        registry.add(worker);
        worker.execute();

        // Assert
        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));
        registry.removeExpired();
        assertSame(worker, registry.get("worker-token"));
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
