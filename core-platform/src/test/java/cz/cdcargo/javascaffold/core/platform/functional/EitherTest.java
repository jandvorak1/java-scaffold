package cz.cdcargo.javascaffold.core.platform.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class EitherTest {

    @Test
    void testRightCreatesRightBranch() {
        // Arrange
        var output = Either.<String, Integer>right(42);

        // Act + Assert
        assertTrue(output.isRight());
        assertFalse(output.isLeft());
        assertEquals(42, output.getRight());
    }

    @Test
    void testLeftCreatesLeftBranch() {
        // Arrange
        var output = Either.<String, String>left("Error");

        // Act + Assert
        assertTrue(output.isLeft());
        assertFalse(output.isRight());
        assertEquals("Error", output.getLeft());
    }

    @Test
    void testLeftRejectsNullValue() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> Either.left(null));
    }

    @Test
    void testRightRejectsNullValue() {
        // Arrange + Act + Assert
        assertThrows(NullPointerException.class, () -> Either.right(null));
    }

    @Test
    void testGetRightRejectsLeftBranch() {
        // Arrange
        var left = Either.<String, Integer>left("Error");

        // Act + Assert
        var exception = assertThrows(NoSuchElementException.class, left::getRight);
        assertEquals("Right value is not available", exception.getMessage());
    }

    @Test
    void testGetLeftRejectsRightBranch() {
        // Arrange
        var right = Either.<String, Integer>right(7);

        // Act + Assert
        var exception = assertThrows(NoSuchElementException.class, right::getLeft);
        assertEquals("Left value is not available", exception.getMessage());
    }

    @Test
    void testMapTransformsRightValue() {
        // Arrange
        var input = Either.<String, Integer>right(5);
        var output = input.map(number -> "v=" + number);

        // Act +Assert
        assertTrue(output.isRight());
        assertEquals("v=5", output.getRight());
    }

    @Test
    void testMapPropagatesLeftValueWithoutInvokingMapper() {
        // Arrange
        var input = Either.<String, Integer>left("Error");

        // Act + Assert
        var output = input.map(value -> {
            throw new AssertionError("Mapper must not be invoked");
        });
        assertEquals(Either.left("Error"), output);
    }

    @Test
    void testMapRejectsNullMapper() {
        // Arrange
        var left = Either.<String, Integer>left("Error");
        var right = Either.<String, Integer>right(5);

        // Act + Assert
        assertThrows(NullPointerException.class, () -> left.map(null));
        assertThrows(NullPointerException.class, () -> right.map(null));
    }

    @Test
    void testMapRejectsNullResult() {
        // Arrange
        var right = Either.<String, Integer>right(5);

        // Act + Assert
        assertThrows(NullPointerException.class, () -> right.map(value -> null));
    }

    @Test
    void testMapLeftTransformsLeftValue() {
        // Arrange
        var input = Either.<String, Integer>left("E1");
        var output = input.mapLeft(String::length);

        // Act + Assert
        assertTrue(output.isLeft());
        assertEquals(2, output.getLeft());
    }

    @Test
    void testMapLeftPropagatesRightValueWithoutInvokingMapper() {
        // Arrange
        var input = Either.<String, Integer>right(5);

        // Act + Assert
        var output = input.mapLeft(value -> {
            throw new AssertionError("Mapper must not be invoked");
        });
        assertEquals(Either.right(5), output);
    }

    @Test
    void testMapLeftRejectsNullMapper() {
        // Arrange
        var left = Either.<String, Integer>left("Error");
        var right = Either.<String, Integer>right(5);

        // Act + Assert
        assertThrows(NullPointerException.class, () -> left.mapLeft(null));
        assertThrows(NullPointerException.class, () -> right.mapLeft(null));
    }

    @Test
    void testMapLeftRejectsNullResult() {
        // Arrange
        var left = Either.<String, Integer>left("Error");

        // Act + Assert
        assertThrows(NullPointerException.class, () -> left.mapLeft(value -> null));
    }

    @Test
    void testFlatMapTransformsRightValueToEitherBranch() {
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
    void testFlatMapPropagatesLeftValueWithoutInvokingMapper() {
        // Arrange
        var input = Either.<String, Integer>left("Error");

        // Act + Assert
        var output = input.flatMap(value -> {
            throw new AssertionError("Mapper must not be invoked");
        });
        assertEquals(Either.left("Error"), output);
    }

    @Test
    void testFlatMapRejectsNullMapper() {
        // Arrange
        var left = Either.<String, Integer>left("Error");
        var right = Either.<String, Integer>right(3);

        // Act + Assert
        assertThrows(NullPointerException.class, () -> left.flatMap(null));
        assertThrows(NullPointerException.class, () -> right.flatMap(null));
    }

    @Test
    void testFlatMapRejectsNullResult() {
        // Arrange
        var right = Either.<String, Integer>right(3);

        // Act + Assert
        assertThrows(NullPointerException.class, () -> right.flatMap(value -> null));
    }

    @Test
    void testFoldInvokesOnlyFunctionForPresentBranch() {
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
    void testFoldRejectsNullFunctions() {
        // Arrange
        var left = Either.<String, Integer>left("Error");
        var right = Either.<String, Integer>right(10);

        // Act + Assert
        assertThrows(NullPointerException.class, () -> left.fold(null, value -> value));
        assertThrows(NullPointerException.class, () -> right.fold(String::length, null));
    }

    @Test
    void testToOptionalContainsOnlyRightValue() {
        // Arrange
        var right = Either.<String, Integer>right(8);
        var left = Either.<String, Integer>left("E");

        // Act + Assert
        assertEquals(8, right.toOptional().orElseThrow());
        assertTrue(left.toOptional().isEmpty());
    }

    @Test
    void testGetOrElseThrowReturnsRightValue() {
        // Arrange
        var right = Either.<String, Integer>right(15);

        // Act + Assert
        assertEquals(15, right.getOrElseThrow(NoSuchElementException::new));
    }

    @Test
    void testGetOrElseThrowMapsLeftValueToException() {
        // Arrange
        var left = Either.<String, Integer>left("Error occurred");
        Function<Object, IllegalArgumentException> exceptionMapper = value -> new IllegalArgumentException(
                value.toString());

        // Act + Assert
        var exception = assertThrows(IllegalArgumentException.class,
                () -> left.getOrElseThrow(exceptionMapper));
        assertEquals("Error occurred", exception.getMessage());
    }

    @Test
    void testGetOrElseThrowRejectsNullSupplier() {
        // Arrange
        var left = Either.<String, Integer>left("Error");

        // Act + Assert
        assertThrows(NullPointerException.class, () -> left.getOrElseThrow(null));
    }

    @Test
    void testGetOrElseThrowRejectsNullSupplierResult() {
        // Arrange
        var left = Either.<String, Integer>left("Error");

        // Act + Assert
        assertThrows(NullPointerException.class, () -> left.getOrElseThrow(value -> null));
    }
}
