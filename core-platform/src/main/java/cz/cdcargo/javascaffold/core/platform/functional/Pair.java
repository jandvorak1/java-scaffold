package cz.cdcargo.javascaffold.core.platform.functional;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents two ordered values.
 *
 * Both components may be null.
 *
 * @param <A> first component type
 * @param <B> second component type
 * @param _1  first component
 * @param _2  second component
 */
public record Pair<A, B>(A _1, B _2) {

    /**
     * Creates a pair with components in their declared order.
     *
     * @param <A> first component type
     * @param <B> second component type
     * @param _1  first component
     * @param _2  second component
     * @return a pair containing the supplied components
     */
    public static <A, B> Pair<A, B> of(A _1, B _2) {
        return new Pair<>(_1, _2);
    }

    /**
     * Maps the first component and preserves the second component.
     *
     * @param <A2>   mapped first component type
     * @param mapper function applied to the first component
     * @return a pair containing the mapped first component and existing second
     *         component
     * @throws NullPointerException if mapper is null
     */
    public <A2> Pair<A2, B> map1(Function<? super A, ? extends A2> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return new Pair<>(mapper.apply(_1), _2);
    }

    /**
     * Maps the second component and preserves the first component.
     *
     * @param <B2>   mapped second component type
     * @param mapper function applied to the second component
     * @return a pair containing the existing first component and mapped second
     *         component
     * @throws NullPointerException if mapper is null
     */
    public <B2> Pair<A, B2> map2(Function<? super B, ? extends B2> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return new Pair<>(_1, mapper.apply(_2));
    }

    /**
     * Maps both components with independent functions.
     *
     * @param <A2>         mapped first component type
     * @param <B2>         mapped second component type
     * @param firstMapper  function applied to the first component
     * @param secondMapper function applied to the second component
     * @return a pair containing both mapped components
     * @throws NullPointerException if either mapper is null
     */
    public <A2, B2> Pair<A2, B2> bimap(Function<? super A, ? extends A2> firstMapper,
            Function<? super B, ? extends B2> secondMapper) {
        Objects.requireNonNull(firstMapper, "First mapper must not be null");
        Objects.requireNonNull(secondMapper, "Second mapper must not be null");
        return new Pair<>(firstMapper.apply(_1), secondMapper.apply(_2));
    }

    /**
     * Reverses the component order.
     *
     * @return a pair containing the second component followed by the first
     */
    public Pair<B, A> swap() {
        return new Pair<>(_2, _1);
    }

    /**
     * Adds a third component and creates a triple.
     *
     * @param <C>   third component type
     * @param value third component
     * @return a triple containing all three components in order
     */
    public <C> Triple<A, B, C> append(C value) {
        return new Triple<>(_1, _2, value);
    }

    /**
     * Combines both components into one result.
     *
     * @param <R>    result type
     * @param folder function applied to both components
     * @return result produced by the supplied function
     * @throws NullPointerException if folder is null
     */
    public <R> R fold(BiFunction<? super A, ? super B, ? extends R> folder) {
        Objects.requireNonNull(folder, "Folder must not be null");
        return folder.apply(_1, _2);
    }
}
