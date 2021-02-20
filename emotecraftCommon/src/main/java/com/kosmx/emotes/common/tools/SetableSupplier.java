package com.kosmx.emotes.common.tools;

import java.util.function.Supplier;

/*
 * I'll use this...
 */
public class SetableSupplier<T> implements Supplier<T> {
    T object;

    /**
     * :D
     * @param object T
     */
    public void set(T object) {
        this.object = object;
    }

    @Override
    public T get() {
        return this.object;
    }
}
