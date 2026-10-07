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
        assertThrows(NullPointerException.class, () -> new BackgroundWorkerRegistry(null, Duration.ZERO));
    }

    @Test
    void testConstructorRejectsNullRetention() {
        assertThrows(NullPointerException.class, () -> new BackgroundWorkerRegistry(Clock.systemUTC(), null));
    }

    @Test
    void testConstructorRejectsNegativeRetention() {
        assertThrows(IllegalArgumentException.class,
                () -> new BackgroundWorkerRegistry(Clock.systemUTC(), Duration.ofSeconds(-1)));
    }

    @Test
    void testAdd() {
        var registry = new BackgroundWorkerRegistry();
        var worker = new NoOpWorker("worker-token");
        registry.add(worker);

        assertSame(worker, registry.get("worker-token"));
    }

    @Test
    void testAddRejectsNullWorker() {
        var registry = new BackgroundWorkerRegistry();

        assertThrows(NullPointerException.class, () -> registry.add(null));
    }

    @Test
    void testAddRejectsDuplicateWorker() {
        var registry = new BackgroundWorkerRegistry();
        registry.add(new NoOpWorker("worker-token"));

        assertThrows(IllegalStateException.class, () -> registry.add(new NoOpWorker("worker-token")));
    }

    @Test
    void testCancel() throws Exception {
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
        registry.add(worker);
        worker.execute();

        assertTrue(startedLatch.await(2, TimeUnit.SECONDS));

        registry.cancel("worker-token");

        assertTrue(cancelledLatch.await(2, TimeUnit.SECONDS));
        assertEquals(BackgroundWorkerState.CANCELLED, worker.state());
    }

    @Test
    void testGet() {
        var registry = new BackgroundWorkerRegistry();
        var worker = new NoOpWorker("worker-token");
        registry.add(worker);
        var result = registry.get("worker-token");

        assertSame(worker, result);
    }

    @Test
    void testGetRejectsNullToken() {
        var registry = new BackgroundWorkerRegistry();

        assertThrows(NullPointerException.class, () -> registry.get(null));
    }

    @Test
    void testCancelRejectsNullToken() {
        var registry = new BackgroundWorkerRegistry();

        assertThrows(NullPointerException.class, () -> registry.cancel(null));
    }

    @Test
    void testRemoveRejectsNullToken() {
        var registry = new BackgroundWorkerRegistry();

        assertThrows(NullPointerException.class, () -> registry.remove(null));
    }

    @Test
    void testRemove() {
        var registry = new BackgroundWorkerRegistry();
        var worker = new NoOpWorker("worker-token");
        registry.add(worker);
        registry.remove("worker-token");

        assertThrows(NoSuchElementException.class, () -> registry.get("worker-token"));
    }

    @Test
    void testRemoveExpired() throws Exception {
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
        registry.add(worker);
        worker.execute();

        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));

        registry.removeExpired();

        assertThrows(NoSuchElementException.class, () -> registry.get("worker-token"));
    }

    @Test
    void testRemoveExpiredHandlesRetentionLongerThanInstantRange() throws Exception {
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
        registry.add(worker);
        worker.execute();

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
