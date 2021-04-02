package io.github.kosmx.emotes.executor.emotePlayer;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.Vec3d;

import javax.annotation.Nullable;

//Every player will be IEmotePlayer
public interface IEmotePlayerEntity<T extends IEmotePlayer > extends IPlayerEntity{

    void init();

    void playEmote(EmoteData emote, int tick);

    @Nullable
    T getEmote();

    boolean isPlayingEmote();

    void stopEmote(int emoteID);

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
}
