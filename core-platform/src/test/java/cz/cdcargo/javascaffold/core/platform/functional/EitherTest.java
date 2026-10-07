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
        var output = Either.<String, Integer>right(42);

        assertTrue(output.isRight());
        assertFalse(output.isLeft());
        assertEquals(42, output.getRight());
    }

    @Test
    void testLeftCreatesLeftBranch() {
        var output = Either.<String, String>left("Error");

        assertTrue(output.isLeft());
        assertFalse(output.isRight());
        assertEquals("Error", output.getLeft());
    }

    @Test
    void testLeftRejectsNullValue() {
        assertThrows(NullPointerException.class, () -> Either.left(null));
    }

    @Test
    void testRightRejectsNullValue() {
        assertThrows(NullPointerException.class, () -> Either.right(null));
    }

    @Test
    void testGetRightRejectsLeftBranch() {
        var left = Either.<String, Integer>left("Error");

        var exception = assertThrows(NoSuchElementException.class, left::getRight);

        assertEquals("Right value is not available", exception.getMessage());
    }

    @Test
    void testGetLeftRejectsRightBranch() {
        var right = Either.<String, Integer>right(7);

        var exception = assertThrows(NoSuchElementException.class, right::getLeft);

        assertEquals("Left value is not available", exception.getMessage());
    }

    @Test
    void testMapTransformsRightValue() {
        var input = Either.<String, Integer>right(5);
        Either<String, String> output = input.map(number -> "v=" + number);

        assertTrue(output.isRight());
        assertEquals("v=5", output.getRight());
    }

    @Test
    void testMapPropagatesLeftValueWithoutInvokingMapper() {
        var input = Either.<String, Integer>left("Error");

        var output = input.map(value -> {
            throw new AssertionError("Mapper must not be invoked");
        });

        assertEquals(Either.left("Error"), output);
    }

    @Test
    void testMapRejectsNullMapper() {
        var left = Either.<String, Integer>left("Error");
        var right = Either.<String, Integer>right(5);

        assertThrows(NullPointerException.class, () -> left.map(null));
        assertThrows(NullPointerException.class, () -> right.map(null));
    }

    @Test
    void testMapRejectsNullResult() {
        var right = Either.<String, Integer>right(5);

        assertThrows(NullPointerException.class, () -> right.map(value -> null));
    }

    @Test
    void testMapLeftTransformsLeftValue() {
        var input = Either.<String, Integer>left("E1");
        Either<Integer, Integer> output = input.mapLeft(String::length);

        assertTrue(output.isLeft());
        assertEquals(2, output.getLeft());
    }

    @Test
    void testMapLeftPropagatesRightValueWithoutInvokingMapper() {
        var input = Either.<String, Integer>right(5);

        var output = input.mapLeft(value -> {
            throw new AssertionError("Mapper must not be invoked");
        });

        assertEquals(Either.right(5), output);
    }

    @Test
    void testMapLeftRejectsNullMapper() {
        var left = Either.<String, Integer>left("Error");
        var right = Either.<String, Integer>right(5);

        assertThrows(NullPointerException.class, () -> left.mapLeft(null));
        assertThrows(NullPointerException.class, () -> right.mapLeft(null));
    }

    @Test
    void testMapLeftRejectsNullResult() {
        var left = Either.<String, Integer>left("Error");

        assertThrows(NullPointerException.class, () -> left.mapLeft(value -> null));
    }

    @Test
    void testFlatMapTransformsRightValueToEitherBranch() {
        var input = Either.<String, Integer>right(3);
        Either<String, String> outputRight = input.flatMap(n -> Either.right("V" + n));
        Either<String, String> outputLeft = input.flatMap(n -> Either.left("E" + n));

        assertTrue(outputRight.isRight());
        assertEquals("V3", outputRight.getRight());
        assertTrue(outputLeft.isLeft());
        assertEquals("E3", outputLeft.getLeft());
    }

    @Test
    void testFlatMapPropagatesLeftValueWithoutInvokingMapper() {
        var input = Either.<String, Integer>left("Error");

        var output = input.flatMap(value -> {
            throw new AssertionError("Mapper must not be invoked");
        });

        assertEquals(Either.left("Error"), output);
    }

    @Test
    void testFlatMapRejectsNullMapper() {
        var left = Either.<String, Integer>left("Error");
        var right = Either.<String, Integer>right(3);

        assertThrows(NullPointerException.class, () -> left.flatMap(null));
        assertThrows(NullPointerException.class, () -> right.flatMap(null));
    }

    @Test
    void testFlatMapRejectsNullResult() {
        var right = Either.<String, Integer>right(3);

        assertThrows(NullPointerException.class, () -> right.flatMap(value -> null));
    }

    @Test
    void testFoldInvokesOnlyFunctionForPresentBranch() {
        var left = Either.<String, Integer>left("Oops");
        var right = Either.<String, Integer>right(10);
        var leftCalls = new AtomicInteger();
        var rightCalls = new AtomicInteger();

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

        assertEquals(4, leftResult);
        assertEquals(30, rightResult);
        assertEquals(1, leftCalls.get());
        assertEquals(1, rightCalls.get());
    }

    @Test
    void testFoldRejectsNullFunctions() {
        var left = Either.<String, Integer>left("Error");
        var right = Either.<String, Integer>right(10);

        assertThrows(NullPointerException.class, () -> left.fold(null, value -> value));
        assertThrows(NullPointerException.class, () -> right.fold(String::length, null));
    }

    @Test
    void testToOptionalContainsOnlyRightValue() {
        var right = Either.<String, Integer>right(8);
        var left = Either.<String, Integer>left("E");

        assertEquals(8, right.toOptional().orElseThrow());
        assertTrue(left.toOptional().isEmpty());
    }

    @Test
    void testGetOrElseThrowReturnsRightValue() {
        var right = Either.<String, Integer>right(15);

        assertEquals(15, right.getOrElseThrow(NoSuchElementException::new));
    }

    @Test
    void testGetOrElseThrowMapsLeftValueToException() {
        var left = Either.<String, Integer>left("Error occurred");
        Function<Object, IllegalArgumentException> exceptionMapper = value -> new IllegalArgumentException(
                value.toString());

        var exception = assertThrows(IllegalArgumentException.class,
                () -> left.getOrElseThrow(exceptionMapper));

        assertEquals("Error occurred", exception.getMessage());
    }

    @Test
    void testGetOrElseThrowRejectsNullSupplier() {
        var left = Either.<String, Integer>left("Error");

        assertThrows(NullPointerException.class, () -> left.getOrElseThrow(null));
    }

    @Test
    void testGetOrElseThrowRejectsNullSupplierResult() {
        var left = Either.<String, Integer>left("Error");

        assertThrows(NullPointerException.class, () -> left.getOrElseThrow(value -> null));
    }
}
