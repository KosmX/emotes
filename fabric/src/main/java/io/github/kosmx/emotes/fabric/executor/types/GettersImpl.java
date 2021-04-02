package io.github.kosmx.emotes.fabric.executor.types;

import io.github.kosmx.emotes.executor.dataTypes.IGetters;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class GettersImpl implements IGetters {
    @Override
    public IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        return (IEmotePlayerEntity) MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
    }
}
