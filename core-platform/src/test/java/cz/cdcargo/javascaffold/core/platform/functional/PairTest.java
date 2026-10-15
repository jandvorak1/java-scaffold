package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PairTest {

    @Test
    void testAppendCreatesTripleInComponentOrder() {
        // Arrange
        var output = Pair.of(1, "a").append(true);

        // Act + Assert
        assertEquals(1, output._1());
        assertEquals("a", output._2());
        assertTrue(output._3());
    }

    @Test
    void testAppendAcceptsNullThirdComponent() {
        // Arrange
        var output = Pair.of(1, "a").append(null);

        // Act + Assert
        assertNull(output._3());
    }

    @Test
    void testBimapTransformsBothComponents() {
        // Arrange
        var output = Pair.of(3, "a").bimap(x -> x + 1, s -> s + "!");

        // Act + Assert
        assertEquals(4, output._1());
        assertEquals("a!", output._2());
    }

    @Test
    void testBimapRejectsNullMappers() {
        // Arrange
        var pair = Pair.of(3, "a");

        // Act + Assert
        assertThrows(NullPointerException.class, () -> pair.bimap(null, value -> value));
        assertThrows(NullPointerException.class, () -> pair.bimap(value -> value, null));
    }

    @Test
    void testFoldCombinesBothComponents() {
        // Arrange
        var output = Pair.of(2, 5).fold((a, b) -> a * b);

        // Act + Assert
        assertEquals(10, output);
    }

    @Test
    void testFoldRejectsNullFunction() {
        // Arrange
        var pair = Pair.of(2, 5);

        // Act + Assert
        assertThrows(NullPointerException.class, () -> pair.fold(null));
    }

    @Test
    void testMap1TransformsFirstComponent() {
        // Arrange
        var output = Pair.of(10, "x").map1(x -> x * 2);

        // Act + Assert
        assertEquals(20, output._1());
        assertEquals("x", output._2());
    }

    @Test
    void testMap1RejectsNullMapper() {
        // Arrange
        var pair = Pair.of(10, "x");

        // Act + Assert
        assertThrows(NullPointerException.class, () -> pair.map1(null));
    }

    @Test
    void testMap2TransformsSecondComponent() {
        // Arrange
        var output = Pair.of(10, "x").map2(String::toUpperCase);

        // Act + Assert
        assertEquals(10, output._1());
        assertEquals("X", output._2());
    }

    @Test
    void testMap2RejectsNullMapper() {
        // Arrange
        var pair = Pair.of(10, "x");

        // Act + Assert
        assertThrows(NullPointerException.class, () -> pair.map2(null));
    }

    @Test
    void testOfStoresComponents() {
        // Arrange
        var output = Pair.of(200, "OK");

        // Act + Assert
        assertEquals(200, output._1());
        assertEquals("OK", output._2());
    }

    @Test
    void testOfAcceptsNullComponents() {
        // Arrange
        var output = Pair.of(null, null);

        // Act + Assert
        assertNull(output._1());
        assertNull(output._2());
    }

    @Test
    void testSwapReversesComponentOrder() {
        // Arrange
        var output = Pair.of(1, "a").swap();

        // Act + Assert
        assertEquals("a", output._1());
        assertEquals(1, output._2());
    }
}
