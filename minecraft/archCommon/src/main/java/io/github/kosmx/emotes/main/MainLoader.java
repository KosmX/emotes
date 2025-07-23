package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientConfigSerializer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.moderation.EmoteWhitelistHashManager;

/**
 * Emotecraft's loader
 */
public class MainLoader {
    private static int tick = 0;

    //The main mod-loader class
    public static void main(boolean isClient) {
        if (isClient) {
            Serializer.INSTANCE = new Serializer<>(new ClientConfigSerializer(), ClientConfig.class);
        } else {
            Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class);
            UniversalEmoteSerializer.loadEmotes();
            EmoteWhitelistHashManager.setupWhitelistConfig(true);
        }
    }

    public static void tick() {
        tick++;

        if (tick % 21 == 20) {
            ClientEmotePlay.checkQueue();
        }
    }

    public static int getTick() {
        return tick;
    }
}
