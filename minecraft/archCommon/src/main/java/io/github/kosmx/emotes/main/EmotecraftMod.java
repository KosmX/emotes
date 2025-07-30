package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.arch.network.CommonServerNetworkHandler;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientConfigSerializer;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public abstract class EmotecraftMod {
    protected void onInitialize(boolean isClient) {
        if (isClient) {
            Serializer.INSTANCE = new Serializer<>(new ClientConfigSerializer(), ClientConfig.class);
        } else {
            Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class);
            UniversalEmoteSerializer.loadEmotes();
        }
    }

    protected void onStartTracking(Entity entity, Player player) {
        if (entity instanceof ServerPlayer tracked && player instanceof ServerPlayer tracker) {
            CommonServerNetworkHandler.getInstance().playerStartTracking(
                    CommonServerNetworkHandler.getInstance().getPlayerNetworkInstance(tracked),
                    CommonServerNetworkHandler.getInstance().getPlayerNetworkInstance(tracker)
            );
        }
    }
}
