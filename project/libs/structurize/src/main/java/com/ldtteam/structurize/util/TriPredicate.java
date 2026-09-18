package com.ldtteam.structurize.util;

/**
 * A predicate accepting three arguments.
 *
 * <p>Forge exposes this small functional interface, so the port keeps the
 * same shape locally instead of coupling placement code to a loader API.</p>
 */
@FunctionalInterface
public interface TriPredicate<A, B, C>
{
    boolean test(A first, B second, C third);
}
