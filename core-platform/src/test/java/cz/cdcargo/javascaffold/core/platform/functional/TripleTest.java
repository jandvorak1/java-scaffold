package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TripleTest {

    @Test
    void testMap1TransformsFirstComponent() {
        var output = Triple.of(2, "x", 1.5).map1(n -> n + 10);

        assertEquals(12, output._1());
        assertEquals("x", output._2());
        assertEquals(1.5, output._3());
    }

    @Test
    void testMap1RejectsNullMapper() {
        var triple = Triple.of(2, "x", 1.5);

        assertThrows(NullPointerException.class, () -> triple.map1(null));
    }

    @Test
    void testMap2TransformsSecondComponent() {
        var output = Triple.of(2, "x", 1.5).map2(String::toUpperCase);

        assertEquals(2, output._1());
        assertEquals("X", output._2());
        assertEquals(1.5, output._3());
    }

    @Test
    void testMap2RejectsNullMapper() {
        var triple = Triple.of(2, "x", 1.5);

        assertThrows(NullPointerException.class, () -> triple.map2(null));
    }

    @Test
    void testMap3TransformsThirdComponent() {
        var output = Triple.of(2, "x", 1.5).map3(d -> d + 0.5);

        assertEquals(2, output._1());
        assertEquals("x", output._2());
        assertEquals(2.0, output._3());
    }

    @Test
    void testMap3RejectsNullMapper() {
        var triple = Triple.of(2, "x", 1.5);

        assertThrows(NullPointerException.class, () -> triple.map3(null));
    }

    @Test
    void testOfStoresComponents() {
        var output = Triple.of("id", 7, true);

        assertEquals("id", output._1());
        assertEquals(7, output._2());
        assertTrue(output._3());
    }

    @Test
    void testOfAcceptsNullComponents() {
        var output = Triple.of(null, null, null);

        assertNull(output._1());
        assertNull(output._2());
        assertNull(output._3());
    }

    @Test
    void testTrimapTransformsAllComponents() {
        var output = Triple.of(1, "a", 2.0).trimap(x -> x * 3, s -> s + s, d -> d / 2);

        assertEquals(3, output._1());
        assertEquals("aa", output._2());
        assertEquals(1.0, output._3());
    }

    @Test
    void testTrimapRejectsNullMappers() {
        var triple = Triple.of(1, "a", 2.0);

        assertThrows(NullPointerException.class,
                () -> triple.trimap(null, value -> value, value -> value));
        assertThrows(NullPointerException.class,
                () -> triple.trimap(value -> value, null, value -> value));
        assertThrows(NullPointerException.class,
                () -> triple.trimap(value -> value, value -> value, null));
    }
}
