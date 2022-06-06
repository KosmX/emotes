package io.github.kosmx.emotes.executor.emotePlayer;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.Vec3d;
import io.github.kosmx.playerAnim.impl.AnimationPlayer;
import io.github.kosmx.playerAnim.layered.IAnimation;

import javax.annotation.Nullable;
import java.util.UUID;

//Every player will be IEmotePlayer
public interface IEmotePlayerEntity<T extends IEmotePlayer > extends IPlayerEntity{

    //void init();

    void playEmote(EmoteData emote, int tick, boolean isForced);

    AnimationPlayer getAnimation();

    @Nullable
    T getEmote();

    boolean isPlayingEmote();

    void stopEmote(UUID emoteID);

    /**
     * Use this ONLY for the main player
     */
    void stopEmote();

    boolean isMainPlayer();

    boolean isNotStanding();

    Vec3d emotesGetPos();

    Vec3d getPrevPos();

    float getBodyYaw();
    float getViewYaw();
    void setBodyYaw(float newYaw);

    void emoteTick();

    default void emoteTickCallback(){}
    default void emoteStartPlayCallback(){}

    public boolean isForcedEmote();
}
