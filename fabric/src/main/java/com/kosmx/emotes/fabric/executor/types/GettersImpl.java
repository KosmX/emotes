package com.kosmx.emotes.fabric.executor.types;

import com.kosmx.emotes.executor.dataTypes.IGetters;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class GettersImpl implements IGetters {
    @Override
    public IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        return (IEmotePlayerEntity) MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
    }
}
