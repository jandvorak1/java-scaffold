package cz.cdcargo.javascaffold.core.platform.functional;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents three ordered values.
 *
 * All components may be null.
 *
 * @param <A> first component type
 * @param <B> second component type
 * @param <C> third component type
 * @param _1  first component
 * @param _2  second component
 * @param _3  third component
 */
public record Triple<A, B, C>(A _1, B _2, C _3) {

    /**
     * Creates a triple with components in their declared order.
     *
     * @param <A> first component type
     * @param <B> second component type
     * @param <C> third component type
     * @param _1  first component
     * @param _2  second component
     * @param _3  third component
     * @return a triple containing the supplied components
     */
    public static <A, B, C> Triple<A, B, C> of(A _1, B _2, C _3) {
        return new Triple<>(_1, _2, _3);
    }

    /**
     * Maps the first component and preserves the remaining components.
     *
     * @param <A2>   mapped first component type
     * @param mapper function applied to the first component
     * @return a triple containing the mapped first component
     * @throws NullPointerException if mapper is null
     */
    public <A2> Triple<A2, B, C> map1(Function<? super A, ? extends A2> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return new Triple<>(mapper.apply(_1), _2, _3);
    }

    /**
     * Maps the second component and preserves the remaining components.
     *
     * @param <B2>   mapped second component type
     * @param mapper function applied to the second component
     * @return a triple containing the mapped second component
     * @throws NullPointerException if mapper is null
     */
    public <B2> Triple<A, B2, C> map2(Function<? super B, ? extends B2> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return new Triple<>(_1, mapper.apply(_2), _3);
    }

    /**
     * Maps the third component and preserves the remaining components.
     *
     * @param <C2>   mapped third component type
     * @param mapper function applied to the third component
     * @return a triple containing the mapped third component
     * @throws NullPointerException if mapper is null
     */
    public <C2> Triple<A, B, C2> map3(Function<? super C, ? extends C2> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return new Triple<>(_1, _2, mapper.apply(_3));
    }

    /**
     * Maps all components with independent functions.
     *
     * @param <A2>         mapped first component type
     * @param <B2>         mapped second component type
     * @param <C2>         mapped third component type
     * @param firstMapper  function applied to the first component
     * @param secondMapper function applied to the second component
     * @param thirdMapper  function applied to the third component
     * @return a triple containing all mapped components
     * @throws NullPointerException if any mapper is null
     */
    public <A2, B2, C2> Triple<A2, B2, C2> trimap(Function<? super A, ? extends A2> firstMapper,
            Function<? super B, ? extends B2> secondMapper, Function<? super C, ? extends C2> thirdMapper) {
        Objects.requireNonNull(firstMapper, "First mapper must not be null");
        Objects.requireNonNull(secondMapper, "Second mapper must not be null");
        Objects.requireNonNull(thirdMapper, "Third mapper must not be null");
        return new Triple<>(firstMapper.apply(_1), secondMapper.apply(_2),
                thirdMapper.apply(_3));
    }
}
