package com.kosmx.emotes.executor.dataTypes;

import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;

import java.util.UUID;

public interface IGetters {
    IEmotePlayerEntity getPlayerFromUUID(UUID uuid);
}
