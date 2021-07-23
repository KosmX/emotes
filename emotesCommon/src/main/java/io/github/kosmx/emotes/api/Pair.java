package io.github.kosmx.emotes.api;

//I Didn't found any pair in Java common... so here is it

/**
 * Pair, stores two objects.
 * @param <L> Left object
 * @param <R> Right object
 */
public class Pair <L, R> {
    L left;
    R right;

    /**
     * Creates an empty pair
     */
    public Pair(){
        this.left = null;
        this.right = null;
    }

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

    public Pair<L, R> setLeft(L left){
        this.left = left;
        return this;
    }
    public Pair<L, R> setRitht(R right){
        this.right = right;
        return this;
    }
}
