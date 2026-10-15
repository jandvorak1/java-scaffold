package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TextStatusChannelTest {

    @AfterEach
    void tearDown() {
        TextStatusChannel.clear();
    }

    @Test
    void testSendWithoutHandler() {
        // Arrange + Act + Assert
        assertDoesNotThrow(() -> TextStatusChannel.send("test"));
    }

    @Test
    void testSendWithHandlerDeliversOneMessage() {
        // Arrange
        var received = new ArrayList<String>();

        // Act
        TextStatusChannel.register(received::add);
        TextStatusChannel.send("hello");

        // Assert
        assertEquals(1, received.size());
        assertEquals("hello", received.getFirst());
    }

    @Test
    void testSendWithHandlerDeliversMultipleMessages() {
        // Arrange
        var received = new ArrayList<String>();

        // Act
        TextStatusChannel.register(received::add);
        TextStatusChannel.send("first");
        TextStatusChannel.send("second");
        TextStatusChannel.send("third");

        // Assert
        assertEquals(3, received.size());
        assertEquals("first", received.get(0));
        assertEquals("second", received.get(1));
        assertEquals("third", received.get(2));
    }

    @Test
    void testSendIsIsolatedPerThread() throws InterruptedException {
        // Arrange
        var mainReceived = new ArrayList<String>();

        // Act
        TextStatusChannel.register(mainReceived::add);
        var thread = Thread.ofVirtual().start(() -> TextStatusChannel.send("from other thread"));
        thread.join();

        // Assert
        assertTrue(mainReceived.isEmpty());
    }

    @Test
    void testClearStopsMessageDelivery() {
        // Arrange
        var received = new ArrayList<String>();

        // Act
        TextStatusChannel.register(received::add);
        TextStatusChannel.clear();
        TextStatusChannel.send("Ignored message");

        // Assert
        assertTrue(received.isEmpty());
    }

    @Test
    void testRegisterRejectsNullHandler() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> TextStatusChannel.register(null));
    }

    @Test
    void testRegisterReplacesExistingHandler() {
        // Arrange
        var first = new ArrayList<String>();
        var second = new ArrayList<String>();

        // Act
        TextStatusChannel.register(first::add);
        TextStatusChannel.register(second::add);
        TextStatusChannel.send("message");

        // Assert
        assertTrue(first.isEmpty());
        assertEquals(List.of("message"), second);
    }

    @Test
    void testSendRejectsNullMessageWithoutHandler() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> TextStatusChannel.send(null));
    }

    @Test
    void testSendRejectsNullMessageWithHandler() {
        // Arrange + Act
        TextStatusChannel.register(message -> {
        });

        // Assert
        assertThrows(NullPointerException.class, () -> TextStatusChannel.send(null));
    }
}
