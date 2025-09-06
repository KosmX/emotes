package io.github.kosmx.emotes.common;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SerializableConfig {
    public final List<ConfigEntry<?>> basics = new ArrayList<>();
    public final List<ConfigEntry<?>> expert = new ArrayList<>();

    /**
     * changelog
     * 2 - PlayerSafetyOption
     * 3 - Kale Ko changed a build-in emote, EmoteFixer to keep bound
     * 4 - using Uuids instead of hash.
     */
    public final static int staticConfigVersion = 4;


    public int configVersion; //this has a different job... not a config

    // public final ConfigEntry<Boolean> showDebug = new ConfigEntry<>("debug", "showDebug", true, false, expert);
    public final ConfigEntry<Boolean> validateEmote = new ConfigEntry<>("validate", false, true, expert);

    public final ConfigEntry<Float> validThreshold = new FloatConfigEntry("validationThreshold", "validThreshold", 8f, true, expert, "options.generic_value", 0.2f, 16f, 0f);

    public final ConfigEntry<Boolean> loadBuiltinEmotes = new ConfigEntry<>("loadbuiltin", "loadBuiltin", true, true, basics);
    public final ConfigEntry<Boolean> loadEmotesServerSide = new ConfigEntry<>("emotesFolderOnLogicalServer", false, true, expert, true);
    public final ConfigEntry<Boolean> enableQuark = new ConfigEntry<>("quark", "enablequark", false, true, basics);

    public final ConfigEntry<String> emotesDir = new ConfigEntry<>("emotesDirectory", "emotes", false, expert, true);

    // public final ConfigEntry<Boolean> autoFixEmoteStop = new ConfigEntry<>("autoFixEmoteStop", true, true, expert, false);

    public void iterate(Consumer<ConfigEntry<?>> consumer) {
        this.basics.forEach(consumer);
        this.expert.forEach(consumer);
    }

    public SerializableConfig() {
        this.loadEmotesServerSide.set(true);
    }

    public static class ConfigEntry<T> {
        final String name, oldConfig; //oldconfig for the old config name
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

    @SuppressWarnings("unused")
    public static class FloatConfigEntry extends ConfigEntry<Float> {
        private final String formatKey;
        public final float min, max, step;

        public FloatConfigEntry(String name, String oldconfig, Float defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, String formatKey, float min, float max, float step) {
            super(name, oldconfig, defVal, hasTooltip, collection);

            this.formatKey = formatKey;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        public FloatConfigEntry(String name, Float defVal, boolean hasTooltip, List<ConfigEntry<?>> collection, String formatKey, float min, float step, float max) {
            this(name, null, defVal, hasTooltip, collection, formatKey, min, max, step);
        }

        public String getFormatKey() {
            return formatKey;
        }

        public double getConfigVal() {
            return Math.sqrt(this.get());
        }

        public void setConfigVal(double newVal) {
            this.set((float) Math.pow(newVal, 2));
        }

        public double getTextVal() {
            return get();
        }
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
}
