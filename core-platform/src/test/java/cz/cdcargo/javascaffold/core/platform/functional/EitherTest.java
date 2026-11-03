package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class EitherTest {

    @Test
    void testRight() {
        // Arrange
        var output = Either.<String, Integer>right(42);

        // Act + Assert
        assertTrue(output.isRight());
        assertFalse(output.isLeft());
        assertEquals(42, output.getRight());
    }

    @Test
    void testLeft() {
        // Arrange
        var output = Either.<String, String>left("Error");

        // Act + Assert
        assertTrue(output.isLeft());
        assertFalse(output.isRight());
        assertEquals("Error", output.getLeft());
    }

    @Test
    void testMap() {
        // Arrange
        var input = Either.<String, Integer>right(5);
        var output = input.map(number -> "v=" + number);

        // Act +Assert
        assertTrue(output.isRight());
        assertEquals("v=5", output.getRight());
    }

    @Test
    void testMapLeft() {
        // Arrange
        var input = Either.<String, Integer>left("E1");
        var output = input.mapLeft(String::length);

        // Act + Assert
        assertTrue(output.isLeft());
        assertEquals(2, output.getLeft());
    }

    @Test
    void testFlatMap() {
        // Arrange
        var input = Either.<String, Integer>right(3);
        var outputRight = input.flatMap(n -> Either.right("V" + n));
        var outputLeft = input.flatMap(n -> Either.left("E" + n));

        // Act + Assert
        assertTrue(outputRight.isRight());
        assertEquals("V3", outputRight.getRight());
        assertTrue(outputLeft.isLeft());
        assertEquals("E3", outputLeft.getLeft());
    }

    @Test
    void testFold() {
        // Arrange
        var left = Either.<String, Integer>left("Oops");
        var right = Either.<String, Integer>right(10);
        var leftCalls = new AtomicInteger();
        var rightCalls = new AtomicInteger();

        // Act
        var leftResult = left.fold(lv -> {
            leftCalls.incrementAndGet();
            return lv.length();
        },
                rv -> {
                    rightCalls.incrementAndGet();
                    return rv * 2;
                });
        var rightResult = right.fold(lv -> {
            leftCalls.incrementAndGet();
            return -1;
        },
                rv -> {
                    rightCalls.incrementAndGet();
                    return rv * 3;
                });

        // Assert
        assertEquals(4, leftResult);
        assertEquals(30, rightResult);
        assertEquals(1, leftCalls.get());
        assertEquals(1, rightCalls.get());
    }

    @Test
    void testToOptional() {
        // Arrange
        var right = Either.<String, Integer>right(8);
        var left = Either.<String, Integer>left("E");

        // Act + Assert
        assertEquals(8, right.toOptional().orElseThrow());
        assertTrue(left.toOptional().isEmpty());
    }

    @Test
    void testGetOrElseThrow() {
        // Arrange
        var right = Either.<String, Integer>right(15);

        // Act + Assert
        assertEquals(15, right.getOrElseThrow(NoSuchElementException::new));
    }
}
