package io.github.kosmx.emotes.main.mixinFunctions;

import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

import net.minecraft.client.resources.sounds.SoundInstance;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface IPlayerEntity {
    /*CameraType FPPerspective = CameraType.FIRST_PERSON;
    Supplier<CameraType> TPBPerspective = () -> (PlatformTools.getConfig().frontAsTPPerspective.get() ? CameraType.THIRD_PERSON_FRONT : CameraType.THIRD_PERSON_BACK);

    default void initEmotePerspective(EmotePlayer emotePlayer){
        if(PlatformTools.getConfig().enablePerspective.get() && isMainPlayer() && PlatformTools.getPerspective() == FPPerspective) {
            emotePlayer.perspective = 1;
            PlatformTools.setPerspective(TPBPerspective.get());
        }
    }*/

    default void emotecraft$playEmote(Animation emote, float tick, boolean isForced) {
        throw new NotImplementedException();
    }

    default @NonNull EmotePlayer emotecraft$getEmote() {
        throw new NotImplementedException();
    }

    default boolean isPlayingEmote(){
        return EmotePlayer.isRunningEmote(this.emotecraft$getEmote());
    }

    default boolean isMainPlayer() {
        return ClientUtil.getClientPlayer() == this;
    }

    /**
     * Use this ONLY for the main player
     */
    default void stopEmote() {
        emotecraft$getEmote().stop();
    }

    default void stopEmote(UUID emoteID) {
        Animation animation = emotecraft$getEmote().getData();
        if (animation != null &&animation.uuid().equals(emoteID)) {
            stopEmote();
        }
    }

    default boolean emotecraft$isForcedEmote() {
        throw new NotImplementedException();
    }

    default void emotecraft$playerEntersInvalidPose() {
        if (!isPlayingEmote() || emotecraft$isForcedEmote()) {
            return;
        }

        if (PlatformTools.getConfig().checkPose.get()) {
            ClientEmotePlay.clientStopLocalEmote(emotecraft$getEmote().getData());
        }
    }

    default void emotecraft$playRawSound(SoundInstance instance, boolean distanceDelay) {
        throw new NotImplementedException();
    }
}
