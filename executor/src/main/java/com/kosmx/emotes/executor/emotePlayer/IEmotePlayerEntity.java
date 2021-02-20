package com.kosmx.emotes.executor.emotePlayer;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.tools.Vec3d;

import javax.annotation.Nullable;
import java.util.UUID;

//Every player will be IEmotePlayer
public interface IEmotePlayerEntity<T extends IEmotePlayer > {

    void playEmote(EmoteData emote);

    @Nullable
    T getEmote();

    void resetLastUpdated();

    boolean isPlayingEmote();

    void stopEmote();

    UUID getUUID();

    boolean isMainPlayer();

    boolean isNotStanding();

    Vec3d getPos();

    Vec3d getPrevPos();

}
