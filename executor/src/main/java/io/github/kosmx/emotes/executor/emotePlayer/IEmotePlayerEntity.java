package io.github.kosmx.emotes.executor.emotePlayer;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3d;

import javax.annotation.Nullable;
import java.util.UUID;

//Every player will be IEmotePlayer
public interface IEmotePlayerEntity {

    //void init();

    void emotecraft$playEmote(KeyframeAnimation emote, int tick, boolean isForced);

    boolean isPlayingEmote();


    @Nullable
    IEmotePlayer emotecraft$getEmote();


    void stopEmote(UUID emoteID);

    /**
     * Use this ONLY for the main player
     */
    void stopEmote();

    boolean isMainPlayer();

    boolean emotecraft$isNotStanding();

    Vec3d emotecraft$emotesGetPos();

    Vec3d emotecraft$getPrevPos();

    float emotecraft$getBodyYaw();
    float emotecraft$getViewYaw();
    void emotecraft$setBodyYaw(float newYaw);

    void emoteTick();

    default void emoteTickCallback(){}
    default void emoteStartPlayCallback(){}

    boolean emotecraft$isForcedEmote();

    UUID emotes_getUUID();
}
