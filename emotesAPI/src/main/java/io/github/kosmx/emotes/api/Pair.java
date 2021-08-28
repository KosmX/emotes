package io.github.kosmx.emotes.api;

//I Didn't found any pair in Java common... so here is it

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * Pair, stores two objects.
 * @param <L> Left object
 * @param <R> Right object
 */
@Immutable
public class Pair <L, R> {
    final L left;
    final R right;

    /**
     * Creates a pair from two values
     * @param left
     * @param right
     */
    public Pair(L left, R right){
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }
    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Pair){
            Pair o2 = (Pair) o;
            return Objects.equals(this.left, o2.left) && Objects.equals(right, o2.right);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (left == null ? 0 : left.hashCode());
        hash = hash * 31 + (right == null ? 0 : right.hashCode());
        return hash;
    }
}
