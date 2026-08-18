package io.github.kosmx.emotes.common.tools;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * HashMap but with making my life easier
 * @param <T>
 */
public class UUIDMap<T extends Supplier<UUID>> extends ConcurrentHashMap<UUID, T> implements Iterable<T> {
    public T put(T v){
        return this.put(v.get(), v);
    }

    public void addAll(Collection<T> m) {
        for (T t : m) this.put(t);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return this.values().iterator();
    }

    public void add(T value) {
        this.put(value);
    }

    public boolean contains(T value) {
        return this.containsKey(value.get());
    }

    public void removeIf(Predicate<T> predicate) {
        this.values().removeIf(predicate);
    }
}
