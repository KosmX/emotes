package io.github.kosmx.bendylibForge.impl;

import java.util.function.Consumer;

/**
 * Anything what can iterate on its positions
 * for {@link IBendable}
 * transform method doesn't create a new matrix for every position
 */
@FunctionalInterface
public interface IterableRePos {

    /**
     * Call consumer to every position
     * @param consumer do consumer.accept(pos)
     */
    void iteratePositions(Consumer<IPosWithOrigin> consumer);
    /* do something like
    {
        for(IPosWithOrigin pos:this.positions) {
            consumer.accept(pos);
        }
    }
     */
}
