package io.github.kosmx.emotes.arch.executor.types;

import io.github.kosmx.emotes.executor.dataTypes.IGetters;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class GettersImpl implements IGetters {
    @Override
    public IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null) return null;
        return (IEmotePlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(uuid);
    }
}
