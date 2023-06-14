package io.github.kosmx.emotes.common.tools;


import dev.kosmx.playerAnim.core.util.Pair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Bi-directional hash-map.
 *
 * Both L and R has to implement {@link Object#hashCode()} and {@link Object#equals(Object)} functions
 * it can not be checked automatically, but DON'T forget it
 * @param <L>
 * @param <R>
 */
@SuppressWarnings("unchecked")
public class BiMap<L, R> implements Collection<Pair<L, R>> {
    final HashMap<L, R> lToR = new HashMap<>();
    final HashMap<R, L> rToL = new HashMap<>();
    final HashSet<Pair<L, R>> collection = new HashSet<>();

    public R getR(L key){
        return lToR.get(key);
    }

    public L getL(R key){
        return rToL.get(key);
    }

    public boolean containsL(L l){
        return lToR.containsKey(l);
    }

    public boolean containsR(R r){
        return rToL.containsKey(r);
    }

    public Pair<L, R> put(L l, R r){
        if(l == null || r == null)throw new NullPointerException("BiMap does not allow null elements");
        L ol = null;
        R or = null;
        if(lToR.containsKey(l) || rToL.containsKey(r)){
            ol = rToL.remove(r);
            or = lToR.remove(l);
            ol = ol != null ? ol :  rToL.remove(or);
            or = or != null ? or : lToR.remove(ol);
            collection.removeIf(obj -> obj.getLeft().equals(l) || obj.getRight().equals(r));
            //collection.remove(new Pair<>(ol, or));
        }
        rToL.put(r, l);
        lToR.put(l, r);
        collection.add(new Pair<>(l, r));
        return new Pair<>(ol, or);
    }

    /**
     * Only put the item into it, if it's not already there.
     * see {@link Collection#add(Object)}
     * @param pair pair of elements
     * @return true if the map changed.
     */
    @Override
    public boolean add(Pair<L, R> pair) {
        if(pair == null || pair.getLeft() == null || pair.getRight() == null)throw new NullPointerException("BiMap does not allow null elements");
        if(collection.contains(pair))return false;
        this.put(pair.getLeft(), pair.getRight());
        return true;
    }

    /**
     * Remove pair from L key
     * @param l key
     * @return the removed R
     */
    public R removeL(L l){
        if(l == null)throw new NullPointerException("BiMap does not allow null elements");
        R r = lToR.remove(l);
        if(r != null){
            rToL.remove(r);
            collection.remove(new Pair<>(l, r));
        }
        return r;
    }

    /**
     * Remove pair from R key
     * @param r key
     * @return the removed L
     */
    public L removeR(R r){
        if(r == null)throw new NullPointerException("BiMap does not allow null elements");
        L l = rToL.remove(r);
        if(l != null){
            rToL.remove(r);
            collection.remove(new Pair<>(l, r));
        }
        return l;
    }

    @Override
    public boolean remove(Object o) {
        boolean bl = collection.remove(o);
        if(bl){
            Pair<L, R> pair = (Pair<L, R>) o;
            lToR.remove(pair.getLeft());
            rToL.remove(pair.getRight());
        }
        return bl;
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Pair<L, R>> c) {
        boolean bl = false;
        for(Pair<L, R> pair:c){
            bl |= this.add(pair);
        }
        return bl;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean bl = false;
        for(Object pair:c){
            bl |= this.remove(pair);
        }
        return bl;
    }

    /**
     * Retains only the elements in this collection that are contained in the specified collection.
     *
     * Code copied from standard lib but runs with my iterator...
     * @param c collection
     * @return was the map modified
     */
    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<Pair<L, R>> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        lToR.clear();
        rToL.clear();
        collection.clear();
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public Iterator<Pair<L, R>> iterator() {
        return new Iterator<Pair<L, R>>() {
            final Iterator<Pair<L, R>> _iter = collection.iterator();
            Pair<L, R> _var = null;
            @Override
            public boolean hasNext() {
                return _iter.hasNext();
            }

            @Override
            public Pair<L, R> next() {
                return _var = _iter.next();
            }

            @Override
            public void remove() {
                _iter.remove();
                lToR.remove(_var.getLeft());
                rToL.remove(_var.getRight());
            }
        };
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }

    private void idk(){
        this.iterator().remove();
    }

    @Override
    public int hashCode() {
        return collection.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BiMap){
            return collection.equals(((BiMap<?, ?>) obj).collection);
        }
        return false;
    }
}
