package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TripleTest {

    @Test
    void testOf() {
        // Arrange
        var output = Triple.of("id", 7, true);

        // Act + Assert
        assertEquals("id", output._1());
        assertEquals(7, output._2());
        assertTrue(output._3());
    }

    @Test
    void testMap1() {
        // Arrange
        var output = Triple.of(2, "x", 1.5).map1(n -> n + 10);

        // Act + Assert
        assertEquals(12, output._1());
        assertEquals("x", output._2());
        assertEquals(1.5, output._3());
    }

    @Test
    void testMap2() {
        // Arrange
        var output = Triple.of(2, "x", 1.5).map2(String::toUpperCase);

        // Act + Assert
        assertEquals(2, output._1());
        assertEquals("X", output._2());
        assertEquals(1.5, output._3());
    }

    @Test
    void testMap3() {
        // Arrange
        var output = Triple.of(2, "x", 1.5).map3(d -> d + 0.5);

        // Act + Assert
        assertEquals(2, output._1());
        assertEquals("x", output._2());
        assertEquals(2.0, output._3());
    }

    @Test
    void testTrimap() {
        // Arrange
        var output = Triple.of(1, "a", 2.0).trimap(x -> x * 3, s -> s + s, d -> d / 2);

        // Act + Assert
        assertEquals(3, output._1());
        assertEquals("aa", output._2());
        assertEquals(1.0, output._3());
    }
}
