package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PairTest {

    @Test
    void testAppendCreatesTripleInComponentOrder() {
        var output = Pair.of(1, "a").append(true);

        assertEquals(1, output._1());
        assertEquals("a", output._2());
        assertTrue(output._3());
    }

    @Test
    void testAppendAcceptsNullThirdComponent() {
        var output = Pair.of(1, "a").append(null);

        assertNull(output._3());
    }

    @Test
    void testBimapTransformsBothComponents() {
        var output = Pair.of(3, "a").bimap(x -> x + 1, s -> s + "!");

        assertEquals(4, output._1());
        assertEquals("a!", output._2());
    }

    @Test
    void testBimapRejectsNullMappers() {
        var pair = Pair.of(3, "a");

        assertThrows(NullPointerException.class, () -> pair.bimap(null, value -> value));
        assertThrows(NullPointerException.class, () -> pair.bimap(value -> value, null));
    }

    @Test
    void testFoldCombinesBothComponents() {
        var output = Pair.of(2, 5).fold((a, b) -> a * b);

        assertEquals(10, output);
    }

    @Test
    void testFoldRejectsNullFunction() {
        var pair = Pair.of(2, 5);

        assertThrows(NullPointerException.class, () -> pair.fold(null));
    }

    @Test
    void testMap1TransformsFirstComponent() {
        var output = Pair.of(10, "x").map1(x -> x * 2);

        assertEquals(20, output._1());
        assertEquals("x", output._2());
    }

    @Test
    void testMap1RejectsNullMapper() {
        var pair = Pair.of(10, "x");

        assertThrows(NullPointerException.class, () -> pair.map1(null));
    }

    @Test
    void testMap2TransformsSecondComponent() {
        var output = Pair.of(10, "x").map2(String::toUpperCase);

        assertEquals(10, output._1());
        assertEquals("X", output._2());
    }

    @Test
    void testMap2RejectsNullMapper() {
        var pair = Pair.of(10, "x");

        assertThrows(NullPointerException.class, () -> pair.map2(null));
    }

    @Test
    void testOfStoresComponents() {
        var output = Pair.of(200, "OK");

        assertEquals(200, output._1());
        assertEquals("OK", output._2());
    }

    @Test
    void testOfAcceptsNullComponents() {
        var output = Pair.of(null, null);

        assertNull(output._1());
        assertNull(output._2());
    }

    @Test
    void testSwapReversesComponentOrder() {
        var output = Pair.of(1, "a").swap();

        assertEquals("a", output._1());
        assertEquals(1, output._2());
    }
}
