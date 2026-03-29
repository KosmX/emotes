package io.github.kosmx.emotes.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused") // API
public class SerializableConfig {
    private final LinkedHashMap<String, List<ConfigEntry<?>>> categories = new LinkedHashMap<>();

    @Deprecated(forRemoval = true)
    public final List<ConfigEntry<?>> basics = category("nocategory");
    @Deprecated(forRemoval = true)
    public final List<ConfigEntry<?>> expert = category("nocategory");

    /**
     * this has a different job... not a config
     */
    public int configVersion;

    protected List<ConfigEntry<?>> category(String name) {
        return categories.computeIfAbsent(name, k -> new ArrayList<>());
    }

    public Map<String, List<ConfigEntry<?>>> getCategories() {
        return Collections.unmodifiableMap(categories);
    }

    public void iterate(Consumer<ConfigEntry<?>> consumer) {
        categories.values().forEach(list -> list.forEach(consumer));
    }

    public static class ConfigEntry<T> {
        /**
         * oldconfig for the old config name
         */
        final String name, oldConfig;
        T value;
        final T defaultValue;
        final public boolean hasTooltip;
        final boolean isHidden;

        public ConfigEntry(String name, String oldconfig, T defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, boolean hidden) {
            this.name = name;
            this.oldConfig = oldconfig;
            this.hasTooltip = hasTooltip;
            defaultValue = defVal;
            value = defVal;
            collection.add(this);
            isHidden = hidden;
        }

        public ConfigEntry(String name, String oldconfig, T defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            this(name, oldconfig, defVal, hasTooltip, collection, false);
        }

        public ConfigEntry(String name, T defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            this(name, null, defVal, hasTooltip, collection);
        }

        public ConfigEntry(String name, T defVal, List<ConfigEntry<?>> collection, boolean hidden) {
            this(name, null, defVal, false, collection, hidden);
        }

        public ConfigEntry(String name, T defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, boolean hidden) {
            this(name, null, defVal, hasTooltip, collection, hidden);
        }

        public T get() {
            return value;
        }

        public void set(T newValue) {
            this.value = newValue;
        }

        public String getName() {
            return name;
        }

        public String getOldConfigName() {
            return oldConfig;
        }

        public void resetToDefault() {
            this.value = this.defaultValue;
        }

        public boolean showEntry() {
            return !isHidden;
        }
    }

    public static class FloatConfigEntry extends NumberConfigEntry<Float> {
        public FloatConfigEntry(String name, String oldconfig, Float defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, Float min, Float max) {
            super(name, oldconfig, defVal, hasTooltip, collection, min, max);
        }

        public FloatConfigEntry(String name, Float defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, Float min, Float max) {
            super(name, defVal, hasTooltip, collection, min, max);
        }

        @Override
        public Float fromDouble(double value) {
            return (float) value;
        }
    }

    public static abstract class NumberConfigEntry<T extends Number & Comparable<T>> extends ConfigEntry<T> {
        public final T min, max;

        public NumberConfigEntry(String name, String oldconfig, T defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, T min, T max) {
            super(name, oldconfig, defVal, hasTooltip, collection);

            this.min = min;
            this.max = max;
        }

        public NumberConfigEntry(String name, T defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, T min, T max) {
            this(name, null, defVal, hasTooltip, collection, min, max);
        }

        @Override
        public T get() {
            T value = super.get();
            if (value == null) return null;
            if (value.compareTo(this.min) < 0) return this.min;
            if (value.compareTo(this.max) > 0) return this.max;
            return value;
        }

        public abstract T fromDouble(double value);
    }

    @SuppressWarnings("unused")
    public static class ListConfigEntry<T> extends ConfigEntry<List<T>> {
        public ListConfigEntry(String name, String oldconfig, List<T> defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, boolean hidden) {
            super(name, oldconfig, defVal, hasTooltip, collection, hidden);
        }

        public ListConfigEntry(String name, String oldconfig, List<T> defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            super(name, oldconfig, defVal, hasTooltip, collection);
        }

        public ListConfigEntry(String name, List<T> defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            super(name, defVal, hasTooltip, collection);
        }

        public ListConfigEntry(String name, List<T> defVal, List<ConfigEntry<?>> collection, boolean hidden) {
            super(name, defVal, collection, hidden);
        }

        public ListConfigEntry(String name, List<T> defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, boolean hidden) {
            super(name, defVal, hasTooltip, collection, hidden);
        }
    }

    public static class EnumConfigEntry<E extends Enum<E>> extends ConfigEntry<E> {
        private final Class<E> enumClass;

        public EnumConfigEntry(String name, String oldconfig, E defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, boolean hidden) {
            super(name, oldconfig, defVal, hasTooltip, collection, hidden);
            this.enumClass = defVal.getDeclaringClass();
        }

        public EnumConfigEntry(String name, String oldconfig, E defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            this(name, oldconfig, defVal, hasTooltip, collection, false);
        }

        public EnumConfigEntry(String name, E defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            this(name, null, defVal, hasTooltip, collection, false);
        }

        public EnumConfigEntry(String name, E defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, boolean hidden) {
            this(name, null, defVal, hasTooltip, collection, hidden);
        }

        public EnumConfigEntry(String name, E defVal, List<ConfigEntry<?>> collection, boolean hidden) {
            this(name, null, defVal, false, collection, hidden);
        }

        public Class<E> getEnumClass() {
            return this.enumClass;
        }

        @Override
        public void set(E newValue) {
            if (newValue == null) throw new NullPointerException("Enum value cannot be null for " + name);
            if (newValue.getDeclaringClass() != enumClass) throw new IllegalArgumentException("Wrong enum type for " + name + ": " + newValue.getDeclaringClass());
            super.set(newValue);
        }
    }
}
