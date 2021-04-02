package io.github.kosmx.emotes.executor.dataTypes;

import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;

import java.util.UUID;

public interface IGetters {
    IEmotePlayerEntity getPlayerFromUUID(UUID uuid);
}
