package com.minecolonies.fabric.util;

@FunctionalInterface
public interface TriPredicate<A, B, C> extends com.ldtteam.structurize.util.TriPredicate<A, B, C>
{
    boolean test(A a, B b, C c);

    default TriPredicate<A, B, C> and(final TriPredicate<? super A, ? super B, ? super C> other)
    {
        return (a, b, c) -> test(a, b, c) && other.test(a, b, c);
    }

    default TriPredicate<A, B, C> or(final TriPredicate<? super A, ? super B, ? super C> other)
    {
        return (a, b, c) -> test(a, b, c) || other.test(a, b, c);
    }
}
