package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PairTest {

    @Test
    void testOf() {
        // Arrange
        var output = Pair.of(1, "a").append(true);

        // Act + Assert
        assertEquals(1, output._1());
        assertEquals("a", output._2());
        assertTrue(output._3());
    }

    @Test
    void testMap1() {
        // Arrange
        var output = Pair.of(10, "x").map1(x -> x * 2);

        // Act + Assert
        assertEquals(20, output._1());
        assertEquals("x", output._2());
    }

    @Test
    void testMap2() {
        // Arrange
        var output = Pair.of(10, "x").map2(String::toUpperCase);

        // Act + Assert
        assertEquals(10, output._1());
        assertEquals("X", output._2());
    }

    @Test
    void testBimap() {
        // Arrange
        var output = Pair.of(3, "a").bimap(x -> x + 1, s -> s + "!");

        // Act + Assert
        assertEquals(4, output._1());
        assertEquals("a!", output._2());
    }

    @Test
    void testSwap() {
        // Arrange
        var output = Pair.of(1, "a").swap();

        // Act + Assert
        assertEquals("a", output._1());
        assertEquals(1, output._2());
    }

    @Test
    void testAppend() {
        // Arrange
        var output = Pair.of(1, "a").append(null);

        // Act + Assert
        assertNull(output._3());
    }

    @Test
    void testFold() {
        // Arrange
        var output = Pair.of(2, 5).fold((a, b) -> a * b);

        // Act + Assert
        assertEquals(10, output);
    }
}
