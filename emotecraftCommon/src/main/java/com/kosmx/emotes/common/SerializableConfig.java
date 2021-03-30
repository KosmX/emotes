package com.kosmx.emotes.common;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SerializableConfig {
    public final ArrayList<ConfigEntry<?>> basics = new ArrayList<>();
    public final ArrayList<ConfigEntry<?>> expert = new ArrayList<>();

    /**
     * changelog
     * 2 - PlayerSafetyOption
     */
    public final static int staticConfigVersion = 2;


    public int configVersion; //this has a different job... not a config

    public final BooleanConfigEntry showDebug = new BooleanConfigEntry("debug", "showDebug", true, false, expert);
    public final BooleanConfigEntry validateEmote = new BooleanConfigEntry("validate", false, true, expert);

    public final FloatConfigEntry validThreshold = new FloatConfigEntry("validationThreshold", "validThreshold", 8f, true, expert);


    public int[] fastMenuHash = new int[8];

    public void iterate(Consumer<ConfigEntry<?>> consumer){
        basics.forEach(consumer);
        expert.forEach(consumer);
    }

    public void iterateGeneral(Consumer<ConfigEntry<?>> consumer){
        basics.forEach(consumer);
    }

    public void iterateExpert(Consumer<ConfigEntry<?>> consumer){
        expert.forEach(consumer);
    }

    public static abstract class ConfigEntry<T>{
        final String name, oldConfig; //oldconfig for the old config name
        T value;
        final public boolean hasTooltip;

        public ConfigEntry(String name, String oldconfig, T defVal, boolean hasTooltip, List<ConfigEntry<?>> collection){
            this.name = name;
            this.oldConfig = oldconfig;
            this.hasTooltip = hasTooltip;
            value = defVal;
            collection.add(this);
        }
        public ConfigEntry(String name, T defVal, boolean hasTooltip, List<ConfigEntry<?>> collection){
            this(name, null, defVal, hasTooltip, collection);
        }

        public T get(){
            return value;
        }

        public void set(T newValue){
            this.value = newValue;
        }

        public String getName(){
            return name;
        }
        public String getOldConfigName(){
            return oldConfig;
        }


    }

    public static class BooleanConfigEntry extends ConfigEntry<Boolean>{

        public BooleanConfigEntry(String name, String oldconfig, Boolean defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            super(name, oldconfig, defVal, hasTooltip, collection);
        }

        public BooleanConfigEntry(String name, Boolean defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            super(name, defVal, hasTooltip, collection);
        }
    }

    public static class FloatConfigEntry extends ConfigEntry<Float>{
        public FloatConfigEntry(String name, String oldconfig, Float defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            super(name, oldconfig, defVal, hasTooltip, collection);
        }

        public FloatConfigEntry(String name, Float defVal, boolean hasTooltip, List<ConfigEntry<?>> collection) {
            super(name, defVal, hasTooltip, collection);
        }
    }
}
