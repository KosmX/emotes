package io.github.kosmx.emotes.arch.executor.types;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.inline.dataTypes.IGetters;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;

import java.util.UUID;

public class GettersImpl implements IGetters {
    @Override
    public IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        return PlatformTools.getPlayerFromUUID(uuid);
    }
}
