package io.github.kosmx.emotes.forge.executor.types;

import io.github.kosmx.emotes.executor.dataTypes.IGetters;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class GettersImpl implements IGetters {
    @Override
    public IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        return (IEmotePlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(uuid);
    }
}
