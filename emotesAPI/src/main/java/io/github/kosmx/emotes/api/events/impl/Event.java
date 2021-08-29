package io.github.kosmx.emotes.api.events.impl;

import java.util.ArrayList;


/**
 * To register a listener, use {@link Event#register(Object)};
 * @param <T>
 */
public class Event<T> {
    final ArrayList<T> listeners = new ArrayList<>();
    final Invoker<T> _invoker;

    public Event(Class<T> clazz, Invoker<T> invoker){
        this._invoker = invoker;
    }

    /**
     * Do EVENT.invoker()./invoke(Objects...)/;
     * Only for invoking the event.
     * @return the invoker
     */
    public final T invoker(){
        return _invoker.invoker(listeners);
    }

    /**
     * Register a new event listener;
     * See the actual event documentation for return type
     * @param listener the listener.
     */
    public void register(T listener){
        if(listener == null) throw new NullPointerException("listener can not be null");
        listeners.add(listener);
    }

    /**
     * unregister the listener
     * @param listener
     */
    public void unregister(T listener){
        listeners.remove(listener);
    }

    @FunctionalInterface
    public interface Invoker<T>{
        T invoker(Iterable<T> listeners);
    }

}
