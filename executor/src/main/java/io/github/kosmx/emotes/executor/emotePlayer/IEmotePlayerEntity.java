package io.github.kosmx.emotes.executor.emotePlayer;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.Vec3d;

import javax.annotation.Nullable;
import java.util.UUID;

//Every player will be IEmotePlayer
public interface IEmotePlayerEntity<T extends IEmotePlayer > extends IPlayerEntity{

    //void init();

    void playEmote(KeyframeAnimation emote, int tick, boolean isForced);

    AnimationProcessor getAnimation();

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

    boolean isForcedEmote();
}
