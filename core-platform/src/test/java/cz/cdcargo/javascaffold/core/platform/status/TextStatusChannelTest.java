package cz.cdcargo.javascaffold.core.platform.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testRegister() {
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
    void testClear() {
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
    void testSend() {
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
}
