package cz.cdcargo.javascaffold.core.platform.functional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents one of two mutually exclusive non-null values.
 *
 * The left branch typically represents a failure and the right branch typically
 * represents a successful result.
 *
 * @param <L> left value type
 * @param <R> right value type
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {

    /**
     * Creates a left branch containing a value.
     *
     * @param <L>   left value type
     * @param <R>   right value type
     * @param value value to store in the left branch
     * @return an instance containing the supplied left value
     * @throws NullPointerException if value is null
     */
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Creates a right branch containing a value.
     *
     * @param <L>   left value type
     * @param <R>   right value type
     * @param value value to store in the right branch
     * @return an instance containing the supplied right value
     * @throws NullPointerException if value is null
     */
    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    /**
     * Determines whether this instance contains a left value.
     *
     * @return true when the left branch is present, otherwise false
     */
    boolean isLeft();

    /**
     * Determines whether this instance contains a right value.
     *
     * @return true when the right branch is present, otherwise false
     */
    default boolean isRight() {
        return !isLeft();
    }

    /**
     * Returns the value stored in the left branch.
     *
     * @return stored left value
     * @throws NoSuchElementException if this instance contains a right value
     */
    L getLeft();

    /**
     * Returns the value stored in the right branch.
     *
     * @return stored right value
     * @throws NoSuchElementException if this instance contains a left value
     */
    R getRight();

    /**
     * Maps a right value while preserving a left value.
     *
     * @param <R2>   mapped right value type
     * @param mapper function applied to the right value
     * @return an instance containing the mapped right value, or the existing left
     *         value
     * @throws NullPointerException if mapper is null or produces null for a right
     *                              value
     */
    <R2> Either<L, R2> map(Function<? super R, ? extends R2> mapper);

    /**
     * Applies an Either-producing operation to a right value while preserving a
     * left value.
     *
     * @param <R2>   resulting right value type
     * @param mapper operation applied to the right value
     * @return the mapped instance, or an instance containing the existing left
     *         value
     * @throws NullPointerException if mapper is null or produces null for a right
     *                              value
     */
    <R2> Either<L, R2> flatMap(
            Function<? super R, ? extends Either<L, R2>> mapper);

    /**
     * Maps a left value while preserving a right value.
     *
     * @param <L2>   mapped left value type
     * @param mapper function applied to the left value
     * @return an instance containing the mapped left value, or the existing right
     *         value
     * @throws NullPointerException if mapper is null or produces null for a left
     *                              value
     */
    <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> mapper);

    /**
     * Converts the right value to an optional value.
     *
     * @return an optional containing the right value, or an empty optional if the
     *         left branch is present
     */
    default Optional<R> toOptional() {
        return isRight() ? Optional.of(getRight()) : Optional.empty();
    }

    /**
     * Returns the right value or throws an exception created from the left value.
     *
     * @param <X>               throwable type
     * @param exceptionSupplier function that creates a throwable from the left
     *                          value
     * @return stored right value
     * @throws X                    if this instance contains a left value
     * @throws NullPointerException if exceptionSupplier is null or produces null
     */
    default <X extends Throwable> R getOrElseThrow(
            Function<? super L, ? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier, "Exception supplier must not be null");
        if (isLeft()) {
            throw Objects.requireNonNull(exceptionSupplier.apply(getLeft()),
                    "Exception supplier result must not be null");
        }
        return getRight();
    }

    /**
     * Maps the present branch to a value with a common result type.
     *
     * @param <T>         result type
     * @param leftMapper  function applied to a left value
     * @param rightMapper function applied to a right value
     * @return result produced by the function for the present branch
     * @throws NullPointerException if either mapper is null
     */
    <T> T fold(Function<? super L, ? extends T> leftMapper,
            Function<? super R, ? extends T> rightMapper);

    /**
     * Represents the left branch of an Either.
     *
     * @param <L>   left value type
     * @param <R>   right value type
     * @param value stored left value
     */
    record Left<L, R>(L value) implements Either<L, R> {

        /**
         * Creates a left branch containing a value.
         *
         * @param value value to store in the left branch
         * @throws NullPointerException if value is null
         */
        public Left {
            Objects.requireNonNull(value, "Value must not be null");
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public L getLeft() {
            return value;
        }

        @Override
        public R getRight() {
            throw new NoSuchElementException("Right value is not available");
        }

        @Override
        public <R2> Either<L, R2> map(Function<? super R, ? extends R2> mapper) {
            Objects.requireNonNull(mapper, "Mapper must not be null");
            return left(value);
        }

        @Override
        public <R2> Either<L, R2> flatMap(
                Function<? super R, ? extends Either<L, R2>> mapper) {
            Objects.requireNonNull(mapper, "Mapper must not be null");
            return left(value);
        }

        @Override
        public <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> mapper) {
            Objects.requireNonNull(mapper, "Mapper must not be null");
            return Either.left(mapper.apply(value));
        }

        @Override
        public <T> T fold(Function<? super L, ? extends T> leftMapper,
                Function<? super R, ? extends T> rightMapper) {
            Objects.requireNonNull(leftMapper, "Left mapper must not be null");
            Objects.requireNonNull(rightMapper, "Right mapper must not be null");
            return leftMapper.apply(value);
        }
    }

    /**
     * Represents the right branch of an Either.
     *
     * @param <L>   left value type
     * @param <R>   right value type
     * @param value stored right value
     */
    record Right<L, R>(R value) implements Either<L, R> {

        /**
         * Creates a right branch containing a value.
         *
         * @param value value to store in the right branch
         * @throws NullPointerException if value is null
         */
        public Right {
            Objects.requireNonNull(value, "Value must not be null");
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public L getLeft() {
            throw new NoSuchElementException("Left value is not available");
        }

        @Override
        public R getRight() {
            return value;
        }

        @Override
        public <R2> Either<L, R2> map(Function<? super R, ? extends R2> mapper) {
            Objects.requireNonNull(mapper, "Mapper must not be null");
            return right(mapper.apply(value));
        }

        @Override
        public <R2> Either<L, R2> flatMap(
                Function<? super R, ? extends Either<L, R2>> mapper) {
            Objects.requireNonNull(mapper, "Mapper must not be null");
            return Objects.requireNonNull(mapper.apply(value),
                    "Mapper result must not be null");
        }

        @Override
        public <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> mapper) {
            Objects.requireNonNull(mapper, "Mapper must not be null");
            return right(value);
        }

        @Override
        public <T> T fold(Function<? super L, ? extends T> leftMapper,
                Function<? super R, ? extends T> rightMapper) {
            Objects.requireNonNull(leftMapper, "Left mapper must not be null");
            Objects.requireNonNull(rightMapper, "Right mapper must not be null");
            return rightMapper.apply(value);
        }
    }
}
