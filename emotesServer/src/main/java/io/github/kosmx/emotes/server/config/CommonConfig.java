package io.github.kosmx.emotes.server.config;

import io.github.kosmx.emotes.common.SerializableConfig;

public class CommonConfig extends SerializableConfig {
    /**
     * changelog
     * 2 - PlayerSafetyOption
     * 3 - Kale Ko changed a build-in emote, EmoteFixer to keep bound
     * 4 - using Uuids instead of hash.
     */
    public final static int staticConfigVersion = 4;

    // public final ConfigEntry<Boolean> showDebug = new ConfigEntry<>("debug", "showDebug", true, false, expert);
    public final ConfigEntry<Boolean> validateEmote = new ConfigEntry<>("validate", false, true, expert);

    // public final ConfigEntry<Float> validThreshold = new FloatConfigEntry("validationThreshold", "validThreshold", 8f, true, expert, 0.2f, 16f);

    public final ConfigEntry<Boolean> loadBuiltinEmotes = new ConfigEntry<>("loadbuiltin", "loadBuiltin", true, true, basics);
    public final ConfigEntry<Boolean> loadEmotesServerSide = new ConfigEntry<>("emotesFolderOnLogicalServer", false, true, expert, true);
    public final ConfigEntry<Boolean> enableQuark = new ConfigEntry<>("quark", "enablequark", false, true, basics);

    public final ConfigEntry<String> emotesDir = new ConfigEntry<>("emotesDirectory", "emotes", false, expert, true);

    // public final ConfigEntry<Boolean> autoFixEmoteStop = new ConfigEntry<>("autoFixEmoteStop", true, true, expert, false);

    public CommonConfig() {
        this.loadEmotesServerSide.set(true);
    }
}
