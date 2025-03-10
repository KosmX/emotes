package io.github.kosmx.emotes.main.mixinFunctions;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

import net.minecraft.client.CameraType;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import java.util.function.Supplier;


public interface IPlayerEntity {
    CameraType FPPerspective = CameraType.FIRST_PERSON;
    Supplier<CameraType> TPBPerspective = () -> (PlatformTools.getConfig().frontAsTPPerspective.get() ? CameraType.THIRD_PERSON_FRONT : CameraType.THIRD_PERSON_BACK);

    default void initEmotePerspective(EmotePlayer emotePlayer){
        if(PlatformTools.getConfig().enablePerspective.get() && isMainPlayer() && PlatformTools.getPerspective() == FPPerspective) {
            emotePlayer.perspective = 1;
            PlatformTools.setPerspective(TPBPerspective.get());
        }
    }

    default void emotecraft$playEmote(KeyframeAnimation emote, int tick, boolean isForced) {
        throw new NotImplementedException();
    }

    @Nullable
    default EmotePlayer emotecraft$getEmote() {
        throw new NotImplementedException();
    }

    default boolean isPlayingEmote(){
        return EmotePlayer.isRunningEmote(this.emotecraft$getEmote());
    }

    default boolean isMainPlayer(){
        return PlatformTools.getMainPlayer() == this;
    }

    /**
     * Use this ONLY for the main player
     */
    default void stopEmote(){
        EmotePlayer emotePlayer = emotecraft$getEmote();
        if(emotePlayer != null) {
            emotePlayer.stop();
            this.emotecraft$voidEmote();
        }
    }

    default void stopEmote(UUID emoteID){
        EmotePlayer emotePlayer = emotecraft$getEmote();
        if(emotePlayer != null && emotePlayer.getData().getUuid().equals(emoteID)){
            emotePlayer.stop();
            this.emotecraft$voidEmote();
        }
    }

    default void emotecraft$voidEmote() {
        throw new NotImplementedException();
    }

    default boolean emotecraft$isForcedEmote() {
        throw new NotImplementedException();
    }

    default void emotecraft$playerEntersInvalidPose() {
        if (!isPlayingEmote() || emotecraft$isForcedEmote()) {
            return;
        }

        EmotePlayer emotePlayer = emotecraft$getEmote();
        if (emotePlayer != null && PlatformTools.getConfig().checkPose.get()) {
            ClientEmotePlay.clientStopLocalEmote(emotePlayer.getData());
        }
    }
}
