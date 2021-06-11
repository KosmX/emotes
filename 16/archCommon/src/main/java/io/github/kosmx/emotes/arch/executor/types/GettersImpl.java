package io.github.kosmx.emotes.arch.executor.types;

import io.github.kosmx.emotes.executor.dataTypes.IGetters;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import java.util.UUID;
import net.minecraft.client.Minecraft;

public class GettersImpl implements IGetters {
    @Override
    public IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        return (IEmotePlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(uuid);
    }
}
